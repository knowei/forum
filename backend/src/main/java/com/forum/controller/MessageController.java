package com.forum.controller;

import com.forum.common.Result;
import com.forum.security.LoginUser;
import com.forum.service.MessageService;
import com.forum.vo.ConversationVO;
import com.forum.vo.MessageVO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(messageService.listConversations(loginUser.getId()));
    }

    @PostMapping("/conversation/{userId}")
    public Result<Map<String, Long>> createOrGetConversation(@PathVariable Long userId) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long conversationId = messageService.createOrGetConversation(loginUser.getId(), userId);
        return Result.success(Map.of("conversationId", conversationId));
    }

    @GetMapping("/conversation/{id}/messages")
    public Result<List<MessageVO>> messages(@PathVariable Long id,
                                            @RequestParam(defaultValue = "0") Long offset,
                                            @RequestParam(defaultValue = "20") Integer limit) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(messageService.getMessages(loginUser.getId(), id, offset, limit));
    }

    @PostMapping("/send")
    public Result<MessageVO> send(@RequestBody Map<String, Object> body) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long conversationId = Long.valueOf(body.get("conversationId").toString());
        String content = (String) body.get("content");
        return Result.success(messageService.sendMessage(loginUser.getId(), conversationId, content));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(Map.of("count", messageService.getUnreadCount(loginUser.getId())));
    }

    @PutMapping("/conversation/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        messageService.markRead(loginUser.getId(), id);
        return Result.success();
    }
}
