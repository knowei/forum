package com.forum.service;

import com.forum.vo.ConversationVO;
import com.forum.vo.MessageVO;

import java.util.List;

public interface MessageService {

    List<ConversationVO> listConversations(Long userId);

    Long createOrGetConversation(Long userId, Long otherUserId);

    List<MessageVO> getMessages(Long userId, Long conversationId, Long offset, Integer limit);

    MessageVO sendMessage(Long userId, Long conversationId, String content);

    long getUnreadCount(Long userId);

    void markRead(Long userId, Long conversationId);
}
