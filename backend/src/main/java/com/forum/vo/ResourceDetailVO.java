package com.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceDetailVO {

    private Long id;
    private Long userId;
    private Integer categoryId;
    private String title;
    private String description;
    private String content;
    private String cover;
    private String downloadLink;
    private String extractCode;
    private String unzipPassword;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Integer status;
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean liked;
    private Boolean collected;
    private Integer type;
}
