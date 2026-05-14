package com.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.forum.common.ResultCode;
import com.forum.entity.Note;
import com.forum.entity.NoteComment;
import com.forum.entity.NoteLike;
import com.forum.exception.BusinessException;
import com.forum.mapper.NoteCommentMapper;
import com.forum.mapper.NoteLikeMapper;
import com.forum.mapper.NoteMapper;
import com.forum.security.LoginUser;
import com.forum.service.NoteService;
import com.forum.vo.NoteCommentVO;
import com.forum.vo.NoteVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final NoteLikeMapper noteLikeMapper;
    private final NoteCommentMapper noteCommentMapper;

    public NoteServiceImpl(NoteMapper noteMapper, NoteLikeMapper noteLikeMapper,
                           NoteCommentMapper noteCommentMapper) {
        this.noteMapper = noteMapper;
        this.noteLikeMapper = noteLikeMapper;
        this.noteCommentMapper = noteCommentMapper;
    }

    @Override
    public List<NoteVO> listPublished() {
        List<NoteVO> list = noteMapper.selectPublishedList();
        LoginUser loginUser = getCurrentLoginUserNullable();
        for (NoteVO vo : list) {
            vo.setLiked(loginUser != null && isLiked(vo.getId(), loginUser.getId()));
        }
        return list;
    }

    @Override
    public NoteVO getDetail(Long id) {
        NoteVO note = noteMapper.selectDetailById(id);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "随手记不存在");
        }
        LoginUser loginUser = getCurrentLoginUserNullable();
        if (loginUser != null) {
            note.setLiked(isLiked(note.getId(), loginUser.getId()));
        } else {
            note.setLiked(false);
        }
        return note;
    }

    @Override
    @Transactional
    public void create(String title, String content, String images) {
        LoginUser loginUser = getCurrentLoginUser();
        if (loginUser.getRole() == null || loginUser.getRole() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅站长可发布随手记");
        }
        Note note = new Note();
        note.setUserId(loginUser.getId());
        note.setTitle(title);
        note.setContent(content);
        note.setImages(images);
        note.setLikeCount(0);
        note.setCommentCount(0);
        note.setStatus(1);
        noteMapper.insert(note);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LoginUser loginUser = getCurrentLoginUser();
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "随手记不存在");
        }
        if (loginUser.getRole() == null || loginUser.getRole() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅站长可删除随手记");
        }
        noteMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void like(Long noteId) {
        LoginUser loginUser = getCurrentLoginUser();
        if (noteMapper.selectById(noteId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "随手记不存在");
        }
        long count = noteLikeMapper.selectCount(new LambdaQueryWrapper<NoteLike>()
                .eq(NoteLike::getNoteId, noteId)
                .eq(NoteLike::getUserId, loginUser.getId()));
        if (count > 0) return;

        NoteLike like = new NoteLike();
        like.setNoteId(noteId);
        like.setUserId(loginUser.getId());
        noteLikeMapper.insert(like);
        noteMapper.update(null, new LambdaUpdateWrapper<Note>()
                .eq(Note::getId, noteId)
                .setSql("like_count = like_count + 1"));
    }

    @Override
    @Transactional
    public void unlike(Long noteId) {
        LoginUser loginUser = getCurrentLoginUser();
        if (noteMapper.selectById(noteId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "随手记不存在");
        }
        int deleted = noteLikeMapper.delete(new LambdaQueryWrapper<NoteLike>()
                .eq(NoteLike::getNoteId, noteId)
                .eq(NoteLike::getUserId, loginUser.getId()));
        if (deleted > 0) {
            noteMapper.update(null, new LambdaUpdateWrapper<Note>()
                    .eq(Note::getId, noteId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
        }
    }

    @Override
    public List<NoteCommentVO> listComments(Long noteId) {
        List<NoteCommentVO> list = noteCommentMapper.selectByNoteId(noteId);
        List<NoteCommentVO> topLevel = list.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
        for (NoteCommentVO top : topLevel) {
            List<NoteCommentVO> replies = list.stream()
                    .filter(c -> top.getId().equals(c.getParentId()))
                    .collect(Collectors.toList());
            top.setReplies(replies);
        }
        return topLevel;
    }

    @Override
    @Transactional
    public void createComment(Long noteId, String content, Long parentId) {
        LoginUser loginUser = getCurrentLoginUser();
        if (noteMapper.selectById(noteId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "随手记不存在");
        }
        if (parentId != null) {
            NoteComment parent = noteCommentMapper.selectById(parentId);
            if (parent == null || !parent.getNoteId().equals(noteId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "父评论不存在");
            }
        }
        NoteComment comment = new NoteComment();
        comment.setNoteId(noteId);
        comment.setUserId(loginUser.getId());
        comment.setContent(content);
        comment.setParentId(parentId);
        noteCommentMapper.insert(comment);
        noteMapper.update(null, new LambdaUpdateWrapper<Note>()
                .eq(Note::getId, noteId)
                .setSql("comment_count = comment_count + 1"));
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        LoginUser loginUser = getCurrentLoginUser();
        NoteComment comment = noteCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        if (!comment.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的评论");
        }
        noteCommentMapper.deleteById(id);
        noteMapper.update(null, new LambdaUpdateWrapper<Note>()
                .eq(Note::getId, comment.getNoteId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
    }

    private boolean isLiked(Long noteId, Long userId) {
        return noteLikeMapper.selectCount(new LambdaQueryWrapper<NoteLike>()
                .eq(NoteLike::getNoteId, noteId)
                .eq(NoteLike::getUserId, userId)) > 0;
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
}
