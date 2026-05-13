package com.forum.service.impl;

import com.forum.common.ResultCode;
import com.forum.exception.BusinessException;
import com.forum.service.VerifyCodeService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private static final long EXPIRE_MS = 5 * 60 * 1000;
    private static final long COOLDOWN_MS = 60 * 1000;
    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    @Override
    public String generateAndStore(String email) {
        CodeEntry existing = store.get(email);
        if (existing != null && System.currentTimeMillis() - existing.createTime < COOLDOWN_MS) {
            long remaining = COOLDOWN_MS - (System.currentTimeMillis() - existing.createTime);
            throw new BusinessException(ResultCode.BAD_REQUEST, "请等待 " + (remaining / 1000) + " 秒后再试");
        }

        String code = String.valueOf((int) ((Math.random() * 900000) + 100000));
        store.put(email, new CodeEntry(code, System.currentTimeMillis()));
        return code;
    }

    @Override
    public void validate(String email, String code) {
        CodeEntry entry = store.get(email);
        if (entry == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先获取验证码");
        }
        if (System.currentTimeMillis() - entry.createTime > EXPIRE_MS) {
            store.remove(email);
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码已过期，请重新获取");
        }
        if (!entry.code.equals(code)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误");
        }
        store.remove(email);
    }

    private record CodeEntry(String code, long createTime) {}
}
