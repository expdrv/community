package com.koyokoyo.community.community.util;

import com.koyokoyo.community.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users=new ThreadLocal<>();

    public void setUser(User user)
    {
        users.set(user);
    }

    public User getUser()
    {
        return users.get();
    }

    public void clear()
    {
        users.remove();
    }
}