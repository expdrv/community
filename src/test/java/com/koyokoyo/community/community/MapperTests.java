package com.koyokoyo.community.community;

import com.koyokoyo.community.community.dao.DiscussPostMapper;
import com.koyokoyo.community.community.dao.LoginTicketMapper;
import com.koyokoyo.community.community.dao.MessageMapper;
import com.koyokoyo.community.community.dao.UserMapper;
import com.koyokoyo.community.community.entity.DiscussPost;
import com.koyokoyo.community.community.entity.LoginTicket;
import com.koyokoyo.community.community.entity.Message;
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
    private MessageMapper messageMapper;

    @Autowired(required = false)
    private  UserMapper userMapper;
    @Autowired(required = false)
    private DiscussPostMapper discussPostMapper;
    @Autowired(required = false)
    private LoginTicketMapper loginTicketMapper;
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
    @Test
    public void testInsertLoginTicket()
    {
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(1111);
        loginTicket.setTicket("hape");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelect()
    {
        LoginTicket loginTicket=loginTicketMapper.selectByTicket("hape");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("hape",5);

        loginTicket=loginTicketMapper.selectByTicket("hape");
        System.out.println(loginTicket);
    }

    @Test
    public void testMessage()
    {
        List<Message> list = messageMapper.selectConversations(111,0,20);
        for( Message message:list)
            System.out.println(message);

        int count=messageMapper.selectConversationCount(111);
            System.out.println(count);

            list =messageMapper.selectLetters("111_112",0,20);
        for( Message message:list)
            System.out.println(message);

        count=messageMapper.selectLetterUnreadCount(131,"111_131");
            System.out.println(count);

    }
}
