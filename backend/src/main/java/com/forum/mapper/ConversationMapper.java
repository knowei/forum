package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Conversation;
import com.forum.vo.ConversationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("""
            select c.id,
                   c.last_content as lastContent, c.last_time as lastTime,
                   case when c.user1_id = #{userId} then c.user1_unread else c.user2_unread end as unreadCount,
                   case when c.user1_id = #{userId} then c.user2_id else c.user1_id end as otherUserId,
                   case when c.user1_id = #{userId} then u2.nickname else u1.nickname end as otherNickname,
                   case when c.user1_id = #{userId} then u2.avatar else u1.avatar end as otherAvatar
            from conversation c
            join user u1 on u1.id = c.user1_id
            join user u2 on u2.id = c.user2_id
            where c.user1_id = #{userId} or c.user2_id = #{userId}
            order by c.last_time desc
            """)
    List<ConversationVO> selectConversations(@Param("userId") Long userId);

    @Select("""
            select coalesce(sum(case when user1_id = #{userId} then user1_unread else user2_unread end), 0)
            from conversation
            where user1_id = #{userId} or user2_id = #{userId}
            """)
    long selectUnreadCount(@Param("userId") Long userId);
}
