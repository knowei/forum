package com.forum.service.impl;

import com.forum.service.OnlineUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserServiceImpl implements OnlineUserService {

    private static final Logger log = LoggerFactory.getLogger(OnlineUserServiceImpl.class);
    private static final long EXPIRE_MS = 5 * 60 * 1000;
    private final Map<Long, Long> userMap = new ConcurrentHashMap<>();

    @Override
    public void recordActivity(Long userId) {
        userMap.put(userId, System.currentTimeMillis());
    }

    @Override
    public long getOnlineCount() {
        cleanup();
        return userMap.size();
    }

    @Override
    public Set<Long> getOnlineUserIds() {
        cleanup();
        return userMap.keySet();
    }

    @Scheduled(fixedRate = 60_000)
    public void cleanup() {
        long cutoff = System.currentTimeMillis() - EXPIRE_MS;
        userMap.values().removeIf(v -> v < cutoff);
    }
}
