package com.forum.service.impl;

import com.forum.common.ResultCode;
import com.forum.entity.User;
import com.forum.exception.BusinessException;
import com.forum.mapper.UserMapper;
import com.forum.security.LoginUser;
import com.forum.service.UserService;
import com.forum.vo.UserVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setEmail(user.getEmail());
        userVO.setAvatar(user.getAvatar());
        userVO.setRole(user.getRole());
        return userVO;
    }

    private LoginUser getCurrentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    @Override
    @Transactional
    public void updateAvatar(String avatarUrl) {
        LoginUser loginUser = getCurrentLoginUser();
        User user = new User();
        user.setId(loginUser.getId());
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updateInfo(String nickname, String email) {
        LoginUser loginUser = getCurrentLoginUser();
        User user = new User();
        user.setId(loginUser.getId());
        user.setNickname(nickname);
        user.setEmail(email);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        LoginUser loginUser = getCurrentLoginUser();
        User user = userMapper.selectById(loginUser.getId());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "原密码错误");
        }
        User update = new User();
        update.setId(loginUser.getId());
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
    }
}
