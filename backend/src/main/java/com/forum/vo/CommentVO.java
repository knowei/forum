package com.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;
    private String content;
    private Long parentId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private LocalDateTime createTime;
}
