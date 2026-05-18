package com.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long id;
    private Long otherUserId;
    private String otherNickname;
    private String otherAvatar;
    private String lastContent;
    private LocalDateTime lastTime;
    private Integer unreadCount;
}
