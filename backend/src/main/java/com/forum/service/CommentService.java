package com.forum.service;

import com.forum.vo.CommentVO;

import java.util.List;

public interface CommentService {

    List<CommentVO> listByResource(Long resourceId);

    void create(Long resourceId, String content, Long parentId);

    void delete(Long id);
}
