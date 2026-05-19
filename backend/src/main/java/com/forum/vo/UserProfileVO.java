package com.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer role;
    private LocalDateTime createTime;
    private Long resourceCount;
    private Long totalViews;
}
