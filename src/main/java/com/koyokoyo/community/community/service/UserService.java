package com.koyokoyo.community.community.service;

import com.koyokoyo.community.community.dao.LoginTicketMapper;
import com.koyokoyo.community.community.dao.UserMapper;
import com.koyokoyo.community.community.entity.LoginTicket;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.util.CommunityConstant;
import com.koyokoyo.community.community.util.CommunityUtil;
import com.koyokoyo.community.community.util.MailClient;
import com.koyokoyo.community.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

 //   @Autowired(required = false)
 //   private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User finUserById(int id)
    {
        User user=getCache(id);
        if(user==null)
            user=initCache(id);
        return user;
    }

    public User findUserByName(String username)
    {
        return userMapper.selectByName(username);
    }
    public Map<String,Object> register(User user)
    {
        Map<String,Object> map=new HashMap<>();

        //判断空值
        if(user==null)
            throw new IllegalArgumentException("参数不能为空");

        if(StringUtils.isBlank(user.getUsername()))
        {
            map.put("usernameMsg","账号不能为空");
            return map;
        }


        if(StringUtils.isBlank(user.getPassword() ))
        {
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getEmail() ))
        {
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //验证是否已存在
        User u=userMapper.selectByName(user.getUsername());
        if(u!=null)
        {
            map.put("usernameMsg","此账号已存在");
            return map;
        }

        u=userMapper.selectByEmail(user.getEmail());
        if(u!=null)
        {
            map.put("emailMsg","此邮箱已被注册");
            return map;
        }

        //注册

        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        userMapper.insertUser(user);

        //激活邮件
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        // http://localhost/community/activation/101/code
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content=templateEngine.process("mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活",content);

        return map;
    }

    public int activation(int userId,String code)
    {
        User user=userMapper.selectById(userId);
        if(user.getStatus()==1)
            return ACTIVATION_REPEAT;
        if(user.getActivationCode().equals(code))
        {
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }
        else return ACTIVATION_FAILURE;

    }

    public Map<String,Object> login(String username,String password, int expiredSeconds)
    {
        Map<String,Object> map=new HashMap<>();

        //null judge
        if(StringUtils.isBlank(username))
        {
            map.put("userMsg","账号不能为空");
            return map;
        }

        if(StringUtils.isBlank(password))
        {
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        User user=userMapper.selectByName(username);
        if(user==null)
        {
            map.put("usernameMsg","账号不存在");
            return map;
        }

        if(user.getStatus()==0)
        {
            map.put("usernameMsg","账号未激活");
            return map;
        }

        password=CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password))
        {
            map.put("passwordMsg","密码错误");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * expiredSeconds));
    //    loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey= RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;

    }

    public void logout(String ticket)
    {
   //     loginTicketMapper.updateStatus(ticket,1);
        String redisKey= RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket=(LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket)
    {
     //   return loginTicketMapper.selectByTicket(ticket);
        String redisKey=RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId,String headerUrl)
    {
        clearCache(userId);
       // return userMapper.updateHeader(userId,headerUrl);
        int rows=userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;

    }

    public boolean alterPassword(String oldPassword,String newPassword,User user)
    {
        if(!CommunityUtil.md5(oldPassword+user.getSalt()).equals(user.getPassword()))
            return false;
        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword+user.getSalt()));
        return true;
    }

    // 1.查询时优先从缓存中取值
    private  User getCache(int userId)
    {
        String redisKey=RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
   // 2.取不到时从mysql获取并初始化缓存数据3.数据变更时清除缓存
    private User initCache(int userId)
    {
        User user=userMapper.selectById(userId);
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

   private void clearCache(int userId)
   {
       String redisKey=RedisKeyUtil.getUserKey(userId);
       redisTemplate.delete(redisKey);
   }
}
