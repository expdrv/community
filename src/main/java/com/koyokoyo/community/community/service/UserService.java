package com.koyokoyo.community.community.service;

import com.koyokoyo.community.community.dao.UserMapper;
import com.koyokoyo.community.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired(required = false)
    private UserMapper userMapper;

    public User finUserById(int id)
    {
        return userMapper.selectById(id);
    }
}
