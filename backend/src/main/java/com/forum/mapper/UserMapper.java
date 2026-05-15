package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where username = #{username} limit 1")
    User findByUsername(String username);

    @Select("select * from user where email = #{email} limit 1")
    User findByEmail(String email);

    @Select("""
            select u.id, u.username, u.nickname, u.avatar, u.role, u.create_time as createTime,
                   (select count(*) from resource r where r.user_id = u.id and r.status = 1) as resourceCount,
                   (select coalesce(sum(r.view_count), 0) from resource r where r.user_id = u.id and r.status = 1) as totalViews
            from user u
            where u.id = #{id}
            """)
    com.forum.vo.UserProfileVO selectProfileById(Long id);
}
