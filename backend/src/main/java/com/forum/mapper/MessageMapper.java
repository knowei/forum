package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Message;
import com.forum.vo.MessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("""
            select m.id, m.sender_id as senderId, m.content, m.create_time as createTime,
                   u.nickname as senderNickname, u.avatar as senderAvatar
            from message m
            join user u on u.id = m.sender_id
            where m.conversation_id = #{conversationId}
            order by m.create_time desc
            limit #{limit} offset #{offset}
            """)
    List<MessageVO> selectMessages(@Param("conversationId") Long conversationId,
                                   @Param("offset") Long offset,
                                   @Param("limit") Integer limit);

    @Select("""
            select count(*) from message
            where conversation_id = #{conversationId}
            """)
    long countMessages(@Param("conversationId") Long conversationId);
}
