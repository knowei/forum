package com.forum.controller;

import com.forum.common.Result;
import com.forum.dto.ResourceAuditDTO;
import com.forum.entity.User;
import com.forum.service.ResourceService;
import com.forum.vo.AdminStatisticsVO;
import com.forum.vo.OnlineUserVO;
import com.forum.vo.ResourceListItemVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ResourceService resourceService;
    private final com.forum.mapper.UserMapper userMapper;
    private final com.forum.mapper.ResourceMapper resourceMapper;
    private final com.forum.mapper.ResourceReportMapper reportMapper;
    private final com.forum.service.ReportService reportService;
    private final com.forum.service.OnlineUserService onlineUserService;

    public AdminController(ResourceService resourceService, com.forum.mapper.UserMapper userMapper,
                           com.forum.mapper.ResourceMapper resourceMapper,
                           com.forum.mapper.ResourceReportMapper reportMapper,
                           com.forum.service.ReportService reportService,
                           com.forum.service.OnlineUserService onlineUserService) {
        this.resourceService = resourceService;
        this.userMapper = userMapper;
        this.resourceMapper = resourceMapper;
        this.reportMapper = reportMapper;
        this.reportService = reportService;
        this.onlineUserService = onlineUserService;
    }

    @GetMapping("/statistics")
    public Result<AdminStatisticsVO> statistics() {
        long userCount = userMapper.selectCount(null);
        long resourceCount = resourceMapper.selectCount(null);
        long pendingResourceCount = resourceMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.forum.entity.Resource>()
                .eq(com.forum.entity.Resource::getStatus, 0));
        long onlineCount = onlineUserService.getOnlineCount();
        return Result.success(new AdminStatisticsVO(userCount, resourceCount, pendingResourceCount, onlineCount));
    }

    @GetMapping("/online-users")
    public Result<List<OnlineUserVO>> onlineUsers() {
        Set<Long> userIds = onlineUserService.getOnlineUserIds();
        if (userIds.isEmpty()) {
            return Result.success(List.of());
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        List<OnlineUserVO> list = users.stream().map(u -> {
            OnlineUserVO vo = new OnlineUserVO();
            vo.setId(u.getId());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
            return vo;
        }).toList();
        return Result.success(list);
    }

    @GetMapping("/resource/pending")
    public Result<List<ResourceListItemVO>> pendingResources() {
        return Result.success(resourceService.listPending());
    }

    @GetMapping("/resource/list")
    public Result<List<ResourceListItemVO>> listAll(@RequestParam(required = false) Integer status) {
        return Result.success(resourceService.listAll(status));
    }

    @PutMapping("/resource/{id}/status")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody ResourceAuditDTO resourceAuditDTO) {
        resourceService.audit(id, resourceAuditDTO);
        return Result.success();
    }

    @GetMapping("/report/list")
    public Result<List<com.forum.entity.ResourceReport>> reportList() {
        return Result.success(reportService.listAll());
    }

    @PutMapping("/report/{id}/status")
    public Result<Void> handleReport(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        String handleNote = (String) body.get("handleNote");
        reportService.handle(id, status, handleNote);
        return Result.success();
    }

    @GetMapping("/user/list")
    public Result<List<User>> userList() {
        return Result.success(userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .orderByDesc(User::getCreateTime)));
    }

    @PutMapping("/user/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.failure(com.forum.common.ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(body.get("status"));
        userMapper.updateById(user);
        return Result.success();
    }
}
