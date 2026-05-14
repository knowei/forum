package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Note;
import com.forum.vo.NoteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Select("""
            select n.id, n.user_id as userId, n.title, n.content, n.images,
                   n.like_count as likeCount, n.comment_count as commentCount,
                   n.create_time as createTime, n.update_time as updateTime,
                   u.nickname as authorNickname, u.avatar as authorAvatar
            from note n
            join user u on u.id = n.user_id
            where n.status = 1
            order by n.create_time desc
            """)
    List<NoteVO> selectPublishedList();

    @Select("""
            select n.id, n.user_id as userId, n.title, n.content, n.images,
                   n.like_count as likeCount, n.comment_count as commentCount,
                   n.create_time as createTime, n.update_time as updateTime,
                   u.nickname as authorNickname, u.avatar as authorAvatar
            from note n
            join user u on u.id = n.user_id
            where n.id = #{id}
            """)
    NoteVO selectDetailById(@Param("id") Long id);
}
