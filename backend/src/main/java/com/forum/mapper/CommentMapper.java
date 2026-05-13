package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Comment;
import com.forum.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("""
            select c.id, c.content, c.parent_id as parentId, c.create_time as createTime,
                   u.id as userId, u.nickname as userNickname, u.avatar as userAvatar
            from comment c
            join user u on u.id = c.user_id
            where c.resource_id = #{resourceId}
            order by c.create_time asc
            """)
    List<CommentVO> selectByResourceId(@Param("resourceId") Long resourceId);
}
