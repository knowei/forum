package com.forum.service;

import com.forum.entity.Notification;

import java.util.List;

public interface NotificationService {

    void create(Long userId, String type, String title, String content, Long relatedId);

    List<Notification> listByUser(Long userId);

    long unreadCount(Long userId);

    void markRead(Long userId, Long notificationId);

    void markAllRead(Long userId);
}
