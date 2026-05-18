package com.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forum.entity.Conversation;
import com.forum.entity.Message;
import com.forum.common.ResultCode;
import com.forum.exception.BusinessException;
import com.forum.mapper.ConversationMapper;
import com.forum.mapper.MessageMapper;
import com.forum.service.MessageService;
import com.forum.vo.ConversationVO;
import com.forum.vo.MessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    public MessageServiceImpl(ConversationMapper conversationMapper, MessageMapper messageMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public List<ConversationVO> listConversations(Long userId) {
        return conversationMapper.selectConversations(userId);
    }

    @Override
    @Transactional
    public Long createOrGetConversation(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能和自己发起会话");
        }

        // Ensure user1_id < user2_id for consistent unique key
        Long u1 = Math.min(userId, otherUserId);
        Long u2 = Math.max(userId, otherUserId);

        Conversation existing = conversationMapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUser1Id, u1)
                        .eq(Conversation::getUser2Id, u2));

        if (existing != null) {
            return existing.getId();
        }

        Conversation conversation = new Conversation();
        conversation.setUser1Id(u1);
        conversation.setUser2Id(u2);
        conversation.setUser1Unread(0);
        conversation.setUser2Unread(0);
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public List<MessageVO> getMessages(Long userId, Long conversationId, Long offset, Integer limit) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看该会话");
        }
        return messageMapper.selectMessages(conversationId, offset, limit);
    }

    @Override
    @Transactional
    public MessageVO sendMessage(Long userId, Long conversationId, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息内容不能为空");
        }

        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }

        boolean isUser1 = conversation.getUser1Id().equals(userId);
        boolean isUser2 = conversation.getUser2Id().equals(userId);
        if (!isUser1 && !isUser2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权在该会话中发送消息");
        }

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setContent(content.trim());
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // Update conversation last content and time
        conversation.setLastContent(content.trim());
        conversation.setLastTime(message.getCreateTime());

        // Increment unread for the other user
        if (isUser1) {
            conversation.setUser2Unread(conversation.getUser2Unread() + 1);
        } else {
            conversation.setUser1Unread(conversation.getUser1Unread() + 1);
        }
        conversationMapper.updateById(conversation);

        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setSenderId(userId);
        vo.setContent(message.getContent());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    @Override
    public long getUnreadCount(Long userId) {
        return conversationMapper.selectUnreadCount(userId);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) return;

        if (conversation.getUser1Id().equals(userId)) {
            conversation.setUser1Unread(0);
            conversationMapper.updateById(conversation);
        } else if (conversation.getUser2Id().equals(userId)) {
            conversation.setUser2Unread(0);
            conversationMapper.updateById(conversation);
        }
    }
}
