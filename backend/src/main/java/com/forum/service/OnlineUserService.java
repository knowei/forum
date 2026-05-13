package com.forum.service;

import java.util.Set;

public interface OnlineUserService {

    void recordActivity(Long userId);

    long getOnlineCount();

    Set<Long> getOnlineUserIds();
}
