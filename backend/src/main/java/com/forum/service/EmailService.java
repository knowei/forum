package com.forum.service;

public interface EmailService {

    void sendVerifyCode(String to, String code);
}
