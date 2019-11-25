package com.koyokoyo.community.community;

import com.koyokoyo.community.community.dao.DiscussPostMapper;
import com.koyokoyo.community.community.dao.UserMapper;
import com.koyokoyo.community.community.entity.DiscussPost;
import com.koyokoyo.community.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)
public class MapperTests {

    @Autowired(required = false)
    private  UserMapper userMapper;
    @Autowired(required = false)
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser()
    {
        User user=userMapper.selectByName("Mobu");
        System.out.println(user);
    }

    @Test
    public void testInsertUser()
    {
        User user=new User();
        user.setActivationCode("");
        user.setEmail("drcuibe@163.com");
        user.setUsername("DRC");
        user.setPassword("123456");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int rows_effected= userMapper.insertUser(user);
        System.out.println(rows_effected);
    }

    @Test
    public void updateStatusandTypeTest()
    {
        userMapper.updateStatus(152,1);
    }

    @Test
    public void testSelectPosts()
    {
        List<DiscussPost> list=discussPostMapper.selectDiscussPosts(0,0,10);
        for(DiscussPost post:list)
            System.out.println(post);

        int rows=discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);

    }
}
