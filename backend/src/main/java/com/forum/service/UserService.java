package com.forum.service;

import com.forum.vo.UserVO;

public interface UserService {

    UserVO getCurrentUser();

    void updateAvatar(String avatarUrl);
}
