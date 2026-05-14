package com.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteCommentVO {

    private Long id;
    private Long noteId;
    private Long userId;
    private String content;
    private Long parentId;
    private LocalDateTime createTime;
    private String userNickname;
    private String userAvatar;
    private List<NoteCommentVO> replies;
}
