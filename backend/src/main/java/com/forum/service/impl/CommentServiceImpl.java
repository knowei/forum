package com.forum.service.impl;

import com.forum.common.ResultCode;
import com.forum.entity.Comment;
import com.forum.entity.Resource;
import com.forum.exception.BusinessException;
import com.forum.mapper.CommentMapper;
import com.forum.mapper.ResourceMapper;
import com.forum.security.LoginUser;
import com.forum.service.CommentService;
import com.forum.vo.CommentVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ResourceMapper resourceMapper;

    public CommentServiceImpl(CommentMapper commentMapper, ResourceMapper resourceMapper) {
        this.commentMapper = commentMapper;
        this.resourceMapper = resourceMapper;
    }

    @Override
    public List<CommentVO> listByResource(Long resourceId) {
        return commentMapper.selectByResourceId(resourceId);
    }

    @Override
    @Transactional
    public void create(Long resourceId, String content, Long parentId) {
        LoginUser loginUser = getLoginUser();

        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }

        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null || !parent.getResourceId().equals(resourceId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "父评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setResourceId(resourceId);
        comment.setUserId(loginUser.getId());
        comment.setContent(content);
        comment.setParentId(parentId);
        commentMapper.insert(comment);

        resourceMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Resource>()
                .eq(Resource::getId, resourceId)
                .setSql("comment_count = comment_count + 1"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LoginUser loginUser = getLoginUser();
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        if (!comment.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的评论");
        }
        commentMapper.deleteById(id);
        resourceMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Resource>()
                .eq(Resource::getId, comment.getResourceId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }
}
