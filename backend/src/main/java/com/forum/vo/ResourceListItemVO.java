package com.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceListItemVO {

    private Long id;
    private String title;
    private String description;
    private String cover;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    private LocalDateTime createTime;
    private Integer status;
    private LocalDateTime updateTime;
    private Integer type;
    private Integer reportCount;
}
