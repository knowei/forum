package com.forum.controller;

import com.forum.common.Result;
import com.forum.dto.PasswordUpdateDTO;
import com.forum.dto.UserUpdateDTO;
import com.forum.entity.Notification;
import com.forum.security.LoginUser;
import com.forum.service.NotificationService;
import com.forum.service.UserService;
import com.forum.vo.UserProfileVO;
import com.forum.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;

    public UserController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/info")
    public Result<UserVO> getInfo() {
        return Result.success(userService.getCurrentUser());
    }

    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> body) {
        userService.updateAvatar(body.get("avatar"));
        return Result.success();
    }

    @PutMapping("/info")
    public Result<Void> updateInfo(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateInfo(dto.getNickname(), dto.getEmail());
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @GetMapping("/{id}/profile")
    public Result<UserProfileVO> getProfile(@PathVariable Long id) {
        return Result.success(userService.getProfile(id));
    }

    @GetMapping("/notifications")
    public Result<List<Notification>> notifications() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(notificationService.listByUser(loginUser.getId()));
    }

    @GetMapping("/notifications/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(Map.of("count", notificationService.unreadCount(loginUser.getId())));
    }

    @PutMapping("/notifications/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationService.markRead(loginUser.getId(), id);
        return Result.success();
    }

    @PutMapping("/notifications/read-all")
    public Result<Void> markAllRead() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationService.markAllRead(loginUser.getId());
        return Result.success();
    }
}
