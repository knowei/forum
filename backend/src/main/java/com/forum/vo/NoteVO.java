package com.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteVO {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String images;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String authorNickname;
    private String authorAvatar;
    private Boolean liked;
}
