package com.koyokoyo.community.community.service;

import com.koyokoyo.community.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId,int entityType,int entityId,int entityUserId)
    {

 /**       boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        if(isMember)
        {
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        }
        else
        {
            redisTemplate.opsForSet().add(entityLikeKey,userId);
        }*/
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey=RedisKeyUtil.getUserLikeKey(entityUserId);

                boolean isMember=operations.opsForSet().isMember(entityLikeKey,userId);
                if(isMember)
                {
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }
                else
                {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                operations.multi();

                return operations.exec();
            }
        });
    }
    public long findEntityLikeCount(int entityType,int entityId)
    {
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    public int findEntityLikeStatus(int userId,int entityType,int entityId)
    {
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
    }

    public int findUserLikeCount(int userId)
    {
        String userLikeKey=RedisKeyUtil.getUserLikeKey(userId);
        Integer count=(Integer) redisTemplate.opsForValue().get(userLikeKey);
        if(count!=null) return count.intValue();
        else return 0;
    }
}
