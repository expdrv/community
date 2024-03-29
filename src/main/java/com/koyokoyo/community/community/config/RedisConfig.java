package com.koyokoyo.community.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {


    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory)
    {
        RedisTemplate<String,Object> template =new RedisTemplate<>();
        template.setConnectionFactory(factory);

        //指定key序列化的方式
        template.setKeySerializer(RedisSerializer.string());
        //设置普通value序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //hash比较特殊，其value本身就是键值对
        //hash key 序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //hash value序列化方式
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }
}
