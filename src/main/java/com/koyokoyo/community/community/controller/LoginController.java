package com.koyokoyo.community.community.controller;

import com.google.code.kaptcha.Producer;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.service.UserService;
import com.koyokoyo.community.community.util.CommunityConstant;
import com.koyokoyo.community.community.util.CommunityUtil;
import com.koyokoyo.community.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path="/login",method=RequestMethod.GET)
    public String getLoginPage()
    {
        return "/site/login";
    }
    @RequestMapping(path="/register",method = RequestMethod.GET)
    public String getRegisterPage()
    {
        return "/site/register";
    }

    @RequestMapping(path="/register",method=RequestMethod.POST)
    public String register(Model model, User user)
    {
        Map<String,Object> map=userService.register(user);
        if(map==null||map.isEmpty())
        {
            model.addAttribute("msg","注册成功，已向您邮箱发送邮件，请激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        else
        {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }
    // http://localhost/community/activation/101/code
    @RequestMapping(path="/activation/{userId}/{code}",method=RequestMethod.GET)
    public String  activation(Model model,
                              @PathVariable("userId") int userId,
                              @PathVariable("code") String code)
    {
        int result=userService.activation(userId,code);
        if(result==userService.ACTIVATION_SUCCESS)
        {
            model.addAttribute("msg","激活成功");
            model.addAttribute("target","/index");//完成后改为login
        }
        else if(result==userService.ACTIVATION_REPEAT)
        {
            model.addAttribute("msg","无效操作，已经激活过");
            model.addAttribute("target","/index");
        }
        else //FAILURE
        {
            model.addAttribute("msg","激活失败");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path="/kaptcha",method = RequestMethod.GET)
    public void getkaptcha(HttpServletResponse response /*,HttpSession session*/)
    {
        //生成验证码
        String text=kaptchaProducer.createText();
        BufferedImage image=kaptchaProducer.createImage(text);

        //存入session
      //  session.setAttribute("kaptcha",text);
        //存入Redis,同时需要一个临时的String作为凭证
        String kaptchaOwner= CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        String redisKey= RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try
        {
            OutputStream os=response.getOutputStream();
            ImageIO.write(image,"png",os);
        }
        catch (IOException e)
        {
            logger.error("响应验证码失败"+e.getMessage());
        }
    }

    @RequestMapping(path="/login",method=RequestMethod.POST)
    public String Login(String username,String password,
                        String code,boolean rememberme,
                        Model model,/*HttpSession session,*/HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner)
    {
        //检查验证码
       // String kaptcha= (String)session.getAttribute("kaptcha");
        String kaptcha=null;
        if(StringUtils.isNotBlank(kaptchaOwner))
        {
            String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha=(String) redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code))
        {
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        //检查账号密码
        int expireSeconds=rememberme?DEFAULT_EXPIRED_SECONDS:REMEMBER_EXPIRED_SECONDS;
        Map<String,Object> map=userService.login(username,password,expireSeconds);
        if(map.containsKey("ticket"))
        {
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge((int)expireSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }
        else
        {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }

    }

    @RequestMapping(path="/logout",method=RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket)
    {
        userService.logout(ticket);
        return "redirect:/login";
    }

}
