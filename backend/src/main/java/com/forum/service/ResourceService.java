package com.forum.service;

import com.forum.common.PageResult;
import com.forum.dto.PageQueryDTO;
import com.forum.dto.ResourceAuditDTO;
import com.forum.dto.ResourceCreateDTO;
import com.forum.vo.ResourceDetailVO;
import com.forum.vo.ResourceListItemVO;

import java.util.List;

public interface ResourceService {

    void create(ResourceCreateDTO resourceCreateDTO);

    void update(Long id, ResourceCreateDTO resourceCreateDTO);

    PageResult<ResourceListItemVO> listPublished(PageQueryDTO pageQueryDTO);

    ResourceDetailVO getDetail(Long id);

    List<ResourceListItemVO> listMyResources();

    List<ResourceListItemVO> listPending();

    List<ResourceListItemVO> listAll(Integer status);

    void audit(Long id, ResourceAuditDTO resourceAuditDTO);

    void like(Long resourceId);

    void unlike(Long resourceId);

    void collect(Long resourceId);

    void uncollect(Long resourceId);

    void deleteById(Long id);

    PageResult<ResourceListItemVO> listByUser(Long userId, PageQueryDTO pageQueryDTO);

    List<ResourceListItemVO> listMyCollections();
}
