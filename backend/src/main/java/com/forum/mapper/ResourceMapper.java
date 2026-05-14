package com.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forum.entity.Resource;
import com.forum.vo.ResourceDetailVO;
import com.forum.vo.ResourceListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Select("""
            <script>
            select r.id, r.title, r.description, r.cover, r.tags, r.view_count as viewCount,
                   r.like_count as likeCount, r.collect_count as collectCount, r.comment_count as commentCount,
                   u.id as authorId, u.nickname as authorNickname, u.avatar as authorAvatar, r.create_time as createTime,
                   r.status, r.update_time as updateTime, r.type
            from resource r
            join user u on u.id = r.user_id
            where r.status = 1
            <if test='categoryId != null'>
              and r.category_id = #{categoryId}
            </if>
            <if test='keyword != null and keyword != ""'>
              and (r.title like concat('%', #{keyword}, '%') or ifnull(r.tags, '') like concat('%', #{keyword}, '%'))
            </if>
            <choose>
              <when test='orderBy == "views"'>order by r.view_count desc</when>
              <when test='orderBy == "likes"'>order by r.like_count desc</when>
              <otherwise>order by r.create_time desc</otherwise>
            </choose>
            limit #{offset}, #{size}
            </script>
            """)
    List<ResourceListItemVO> selectPublishedList(@Param("categoryId") Integer categoryId, @Param("keyword") String keyword,
                                                 @Param("orderBy") String orderBy,
                                                 @Param("offset") long offset, @Param("size") long size);

    @Select("""
            <script>
            select count(*) from resource r
            where r.status = 1
            <if test='categoryId != null'>
              and r.category_id = #{categoryId}
            </if>
            <if test='keyword != null and keyword != ""'>
              and (r.title like concat('%', #{keyword}, '%') or ifnull(r.tags, '') like concat('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countPublished(@Param("categoryId") Integer categoryId, @Param("keyword") String keyword);

    @Select("""
            select r.id, r.user_id as userId, r.category_id as categoryId, r.title, r.description, r.content,
                   r.cover, r.download_link as downloadLink, r.extract_code as extractCode, r.unzip_password as unzipPassword,
                   r.tags, r.view_count as viewCount, r.like_count as likeCount, r.collect_count as collectCount,
                   r.comment_count as commentCount, r.status, r.type, r.create_time as createTime, r.update_time as updateTime,
                   u.id as authorId, u.nickname as authorNickname, u.avatar as authorAvatar
            from resource r
            join user u on u.id = r.user_id
            where r.id = #{id}
            limit 1
            """)
    ResourceDetailVO selectDetailById(@Param("id") Long id);

    @Select("""
            select r.id, r.title, r.description, r.cover, r.tags, r.view_count as viewCount,
                   r.like_count as likeCount, r.collect_count as collectCount, r.comment_count as commentCount,
                   u.id as authorId, u.nickname as authorNickname, u.avatar as authorAvatar, r.create_time as createTime,
                   r.status, r.update_time as updateTime, r.type
            from resource r
            join user u on u.id = r.user_id
            where r.status = 0
            order by r.create_time asc
            """)
    List<ResourceListItemVO> selectPendingList();

    @Select("""
            select r.id, r.title, r.description, r.cover, r.tags, r.view_count as viewCount,
                   r.like_count as likeCount, r.collect_count as collectCount, r.comment_count as commentCount,
                   u.id as authorId, u.nickname as authorNickname, u.avatar as authorAvatar, r.create_time as createTime,
                   r.status, r.update_time as updateTime, r.type,
                   coalesce(rp.report_count, 0) as reportCount
            from resource r
            join user u on u.id = r.user_id
            left join (select resource_id, count(*) as report_count from resource_report where status = 0 group by resource_id) rp on rp.resource_id = r.id
            where r.user_id = #{userId}
            order by r.create_time desc
            """)
    List<ResourceListItemVO> selectMyResources(@Param("userId") Long userId);

    @Select("""
            <script>
            select r.id, r.title, r.description, r.cover, r.tags, r.view_count as viewCount,
                   r.like_count as likeCount, r.collect_count as collectCount, r.comment_count as commentCount,
                   u.id as authorId, u.nickname as authorNickname, u.avatar as authorAvatar, r.create_time as createTime,
                   r.status, r.update_time as updateTime, r.type
            from resource r
            join user u on u.id = r.user_id
            <where>
              r.status != 3
              <if test='status != null'>
                and r.status = #{status}
              </if>
            </where>
            order by r.create_time desc
            </script>
            """)
    List<ResourceListItemVO> selectAdminList(@Param("status") Integer status);

    @Select("""
            <script>
            select r.id, r.title, r.description, r.cover, r.tags, r.view_count as viewCount,
                   r.like_count as likeCount, r.collect_count as collectCount, r.comment_count as commentCount,
                   u.id as authorId, u.nickname as authorNickname, u.avatar as authorAvatar, r.create_time as createTime,
                   r.status, r.update_time as updateTime, r.type
            from resource r
            join user u on u.id = r.user_id
            where r.id in
            <foreach item='id' collection='ids' open='(' separator=',' close=')'>
              #{id}
            </foreach>
            order by r.create_time desc
            </script>
            """)
    List<ResourceListItemVO> selectListByIds(@Param("ids") List<Long> ids);
}
