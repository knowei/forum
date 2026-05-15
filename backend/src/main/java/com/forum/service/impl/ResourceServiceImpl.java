package com.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.forum.common.PageResult;
import com.forum.common.ResultCode;
import com.forum.dto.PageQueryDTO;
import com.forum.dto.ResourceAuditDTO;
import com.forum.dto.ResourceCreateDTO;
import com.forum.entity.Category;
import com.forum.entity.Resource;
import com.forum.entity.UserCollect;
import com.forum.entity.UserLike;
import com.forum.exception.BusinessException;
import com.forum.mapper.CategoryMapper;
import com.forum.mapper.ResourceMapper;
import com.forum.mapper.UserCollectMapper;
import com.forum.mapper.UserLikeMapper;
import com.forum.security.LoginUser;
import com.forum.service.ResourceService;
import com.forum.vo.ResourceDetailVO;
import com.forum.vo.ResourceListItemVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final CategoryMapper categoryMapper;
    private final UserLikeMapper userLikeMapper;
    private final UserCollectMapper userCollectMapper;
    private final com.forum.service.NotificationService notificationService;

    public ResourceServiceImpl(ResourceMapper resourceMapper, CategoryMapper categoryMapper,
                               UserLikeMapper userLikeMapper, UserCollectMapper userCollectMapper,
                               com.forum.service.NotificationService notificationService) {
        this.resourceMapper = resourceMapper;
        this.categoryMapper = categoryMapper;
        this.userLikeMapper = userLikeMapper;
        this.userCollectMapper = userCollectMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void update(Long id, ResourceCreateDTO dto) {
        LoginUser loginUser = getCurrentLoginUser();
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }
        if (!resource.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能编辑自己的资源");
        }

        boolean isArticle = dto.getType() != null && dto.getType() == 1;
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "正文内容不能为空");
        }
        if (!isArticle && (dto.getDownloadLink() == null || dto.getDownloadLink().isBlank())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "下载链接不能为空");
        }

        resource.setCategoryId(dto.getCategoryId());
        resource.setTitle(dto.getTitle());
        resource.setDescription(dto.getDescription());
        resource.setContent(dto.getContent());
        resource.setCover(dto.getCover());
        resource.setDownloadLink(dto.getDownloadLink());
        resource.setExtractCode(dto.getExtractCode());
        resource.setUnzipPassword(dto.getUnzipPassword());
        resource.setTags(dto.getTags());
        resource.setType(dto.getType() != null ? dto.getType() : 0);
        resource.setStatus(0);
        resourceMapper.updateById(resource);
    }

    @Override
    @Transactional
    public void create(ResourceCreateDTO resourceCreateDTO) {
        LoginUser loginUser = getCurrentLoginUser();
        Category category = categoryMapper.selectById(resourceCreateDTO.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "分类不存在或不可用");
        }

        boolean isDraft = resourceCreateDTO.getStatus() != null && resourceCreateDTO.getStatus() == 3;
        boolean isArticle = resourceCreateDTO.getType() != null && resourceCreateDTO.getType() == 1;
        if (!isDraft) {
            if (resourceCreateDTO.getContent() == null || resourceCreateDTO.getContent().isBlank()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "正文内容不能为空");
            }
            if (!isArticle && (resourceCreateDTO.getDownloadLink() == null || resourceCreateDTO.getDownloadLink().isBlank())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "下载链接不能为空");
            }
        }

        Resource resource = new Resource();
        resource.setUserId(loginUser.getId());
        resource.setCategoryId(resourceCreateDTO.getCategoryId());
        resource.setTitle(resourceCreateDTO.getTitle());
        resource.setDescription(resourceCreateDTO.getDescription());
        resource.setContent(resourceCreateDTO.getContent());
        resource.setCover(resourceCreateDTO.getCover());
        resource.setDownloadLink(resourceCreateDTO.getDownloadLink());
        resource.setExtractCode(resourceCreateDTO.getExtractCode());
        resource.setUnzipPassword(resourceCreateDTO.getUnzipPassword());
        resource.setTags(resourceCreateDTO.getTags());
        resource.setViewCount(0);
        resource.setLikeCount(0);
        resource.setCollectCount(0);
        resource.setCommentCount(0);
        resource.setStatus(resourceCreateDTO.getStatus() != null ? resourceCreateDTO.getStatus() : 0);
        resource.setType(resourceCreateDTO.getType() != null ? resourceCreateDTO.getType() : 0);
        resourceMapper.insert(resource);
    }

    @Override
    public PageResult<ResourceListItemVO> listPublished(PageQueryDTO pageQueryDTO) {
        long page = pageQueryDTO.getPage() == null ? 1L : pageQueryDTO.getPage();
        long size = pageQueryDTO.getSize() == null ? 10L : pageQueryDTO.getSize();
        long offset = (page - 1) * size;
        List<ResourceListItemVO> list = resourceMapper.selectPublishedList(pageQueryDTO.getCategoryId(), pageQueryDTO.getKeyword(), pageQueryDTO.getOrderBy(), offset, size);
        long total = resourceMapper.countPublished(pageQueryDTO.getCategoryId(), pageQueryDTO.getKeyword());
        return new PageResult<>(total, list);
    }

    @Override
    @Transactional
    public ResourceDetailVO getDetail(Long id) {
        ResourceDetailVO detail = resourceMapper.selectDetailById(id);
        if (detail == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }

        LoginUser loginUser = getCurrentLoginUserNullable();
        boolean isOwner = loginUser != null && detail.getUserId().equals(loginUser.getId());
        boolean isAdmin = loginUser != null && loginUser.getRole() != null && loginUser.getRole() == 1;
        boolean published = detail.getStatus() != null && detail.getStatus() == 1;
        if (!published && !isOwner && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN, "资源未发布");
        }

        resourceMapper.updateById(buildViewCountResource(detail.getId(), detail.getViewCount() == null ? 1 : detail.getViewCount() + 1));
        detail.setViewCount(detail.getViewCount() == null ? 1 : detail.getViewCount() + 1);

        if (loginUser != null) {
            boolean liked = userLikeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                    .eq(UserLike::getUserId, loginUser.getId())
                    .eq(UserLike::getResourceId, id)) > 0;
            boolean collected = userCollectMapper.selectCount(new LambdaQueryWrapper<UserCollect>()
                    .eq(UserCollect::getUserId, loginUser.getId())
                    .eq(UserCollect::getResourceId, id)) > 0;
            detail.setLiked(liked);
            detail.setCollected(collected);
        } else {
            detail.setLiked(false);
            detail.setCollected(false);
        }

        return detail;
    }

    @Override
    public List<ResourceListItemVO> listPending() {
        ensureAdmin();
        return resourceMapper.selectPendingList();
    }

    @Override
    public List<ResourceListItemVO> listMyResources() {
        LoginUser loginUser = getCurrentLoginUser();
        return resourceMapper.selectMyResources(loginUser.getId());
    }

    @Override
    public List<ResourceListItemVO> listAll(Integer status) {
        ensureAdmin();
        return resourceMapper.selectAdminList(status);
    }

    @Override
    @Transactional
    public void audit(Long id, ResourceAuditDTO resourceAuditDTO) {
        ensureAdmin();
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }
        resource.setStatus(resourceAuditDTO.getStatus());
        resourceMapper.updateById(resource);

        String statusText = resourceAuditDTO.getStatus() == 1 ? "已通过" : "未通过";
        String reason = resourceAuditDTO.getStatus() == 2 && resourceAuditDTO.getRejectReason() != null
                ? "，原因：" + resourceAuditDTO.getRejectReason() : "";
        notificationService.create(resource.getUserId(), "RESOURCE_AUDITED",
                "资源审核" + statusText,
                "您的资源「" + resource.getTitle() + "」审核" + statusText + reason,
                resource.getId());
    }

    private Resource buildViewCountResource(Long id, Integer viewCount) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setViewCount(viewCount);
        return resource;
    }

    private void ensureAdmin() {
        LoginUser loginUser = getCurrentLoginUser();
        if (loginUser.getRole() == null || loginUser.getRole() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private LoginUser getCurrentLoginUser() {
        LoginUser loginUser = getCurrentLoginUserNullable();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    private LoginUser getCurrentLoginUserNullable() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }

    @Override
    @Transactional
    public void like(Long resourceId) {
        LoginUser loginUser = getCurrentLoginUser();
        if (resourceMapper.selectById(resourceId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }
        Long count = userLikeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, loginUser.getId())
                .eq(UserLike::getResourceId, resourceId));
        if (count > 0) {
            return;
        }
        UserLike userLike = new UserLike();
        userLike.setUserId(loginUser.getId());
        userLike.setResourceId(resourceId);
        userLikeMapper.insert(userLike);
        resourceMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Resource>()
                .eq(Resource::getId, resourceId)
                .setSql("like_count = like_count + 1"));
    }

    @Override
    @Transactional
    public void unlike(Long resourceId) {
        LoginUser loginUser = getCurrentLoginUser();
        if (resourceMapper.selectById(resourceId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }
        int deleted = userLikeMapper.delete(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, loginUser.getId())
                .eq(UserLike::getResourceId, resourceId));
        if (deleted > 0) {
            resourceMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Resource>()
                    .eq(Resource::getId, resourceId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
        }
    }

    @Override
    @Transactional
    public void collect(Long resourceId) {
        LoginUser loginUser = getCurrentLoginUser();
        if (resourceMapper.selectById(resourceId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }
        Long count = userCollectMapper.selectCount(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, loginUser.getId())
                .eq(UserCollect::getResourceId, resourceId));
        if (count > 0) {
            return;
        }
        UserCollect userCollect = new UserCollect();
        userCollect.setUserId(loginUser.getId());
        userCollect.setResourceId(resourceId);
        userCollectMapper.insert(userCollect);
        resourceMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Resource>()
                .eq(Resource::getId, resourceId)
                .setSql("collect_count = collect_count + 1"));
    }

    @Override
    @Transactional
    public void uncollect(Long resourceId) {
        LoginUser loginUser = getCurrentLoginUser();
        if (resourceMapper.selectById(resourceId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }
        int deleted = userCollectMapper.delete(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, loginUser.getId())
                .eq(UserCollect::getResourceId, resourceId));
        if (deleted > 0) {
            resourceMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Resource>()
                    .eq(Resource::getId, resourceId)
                    .setSql("collect_count = GREATEST(collect_count - 1, 0)"));
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        LoginUser loginUser = getCurrentLoginUser();
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }
        if (!resource.getUserId().equals(loginUser.getId()) && !(loginUser.getRole() != null && loginUser.getRole() == 1)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此资源");
        }
        resourceMapper.deleteById(id);
    }

    @Override
    public PageResult<ResourceListItemVO> listByUser(Long userId, PageQueryDTO pageQueryDTO) {
        long page = pageQueryDTO.getPage() == null ? 1L : pageQueryDTO.getPage();
        long size = pageQueryDTO.getSize() == null ? 10L : pageQueryDTO.getSize();
        long offset = (page - 1) * size;
        List<ResourceListItemVO> list = resourceMapper.selectUserPublishedList(userId, offset, size);
        long total = resourceMapper.countUserPublished(userId);
        return new PageResult<>(total, list);
    }

    @Override
    public List<ResourceListItemVO> listMyCollections() {
        LoginUser loginUser = getCurrentLoginUser();
        List<Long> resourceIds = userCollectMapper.selectList(
                new LambdaQueryWrapper<UserCollect>()
                        .eq(UserCollect::getUserId, loginUser.getId())
                        .orderByDesc(UserCollect::getCreateTime))
                .stream()
                .map(UserCollect::getResourceId)
                .collect(Collectors.toList());
        if (resourceIds.isEmpty()) {
            return List.of();
        }
        return resourceMapper.selectListByIds(resourceIds);
    }
}
