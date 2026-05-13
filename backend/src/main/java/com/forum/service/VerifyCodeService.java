package com.forum.service;

public interface VerifyCodeService {

    String generateAndStore(String email);

    void validate(String email, String code);
}
