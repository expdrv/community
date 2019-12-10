package com.koyokoyo.community.community.dao;

import com.koyokoyo.community.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentBtEntity(int entityType, int entityId,int offset,int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);
}
