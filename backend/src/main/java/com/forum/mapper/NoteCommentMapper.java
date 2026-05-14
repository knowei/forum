package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.NoteComment;
import com.forum.vo.NoteCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteCommentMapper extends BaseMapper<NoteComment> {

    @Select("""
            select nc.id, nc.note_id as noteId, nc.user_id as userId, nc.content,
                   nc.parent_id as parentId, nc.create_time as createTime,
                   u.nickname as userNickname, u.avatar as userAvatar
            from note_comment nc
            join user u on u.id = nc.user_id
            where nc.note_id = #{noteId}
            order by nc.create_time asc
            """)
    List<NoteCommentVO> selectByNoteId(@Param("noteId") Long noteId);
}
