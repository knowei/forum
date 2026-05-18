package com.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private Long senderId;
    private String content;
    private LocalDateTime createTime;
    private String senderNickname;
    private String senderAvatar;
}
