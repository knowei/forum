package com.forum.service;

import com.forum.vo.NoteCommentVO;
import com.forum.vo.NoteVO;

import java.util.List;

public interface NoteService {

    List<NoteVO> listPublished();

    NoteVO getDetail(Long id);

    void create(String title, String content, String images);

    void delete(Long id);

    void like(Long noteId);

    void unlike(Long noteId);

    List<NoteCommentVO> listComments(Long noteId);

    void createComment(Long noteId, String content, Long parentId);

    void deleteComment(Long id);
}
