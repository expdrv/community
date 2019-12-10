package com.koyokoyo.community.community.dao;

import com.koyokoyo.community.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    // 如果在<if>里使用唯一参数，则必须使用@Param别名
    int selectDiscussPostRows(@Param("userId")int userId);

    int insertDiscussPost(DiscussPost discussPost);


    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

}
