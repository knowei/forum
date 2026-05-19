package com.forum.service;

import com.forum.vo.UserVO;

public interface UserService {

    UserVO getCurrentUser();

    void updateAvatar(String avatarUrl);

    void updateInfo(String nickname, String email, String bio);

    void updatePassword(String oldPassword, String newPassword);

    void updateBgImage(String bgImageUrl);

    com.forum.vo.UserProfileVO getProfile(Long id);

    java.util.List<com.forum.vo.UserProfileVO> searchUsers(String keyword);
}
