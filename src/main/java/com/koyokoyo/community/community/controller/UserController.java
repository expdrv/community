package com.koyokoyo.community.community.controller;

import com.koyokoyo.community.community.annotation.LoginRequired;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.service.FollowService;
import com.koyokoyo.community.community.service.LikeService;
import com.koyokoyo.community.community.service.UserService;
import com.koyokoyo.community.community.util.CommunityConstant;
import com.koyokoyo.community.community.util.CommunityUtil;
import com.koyokoyo.community.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(path="/setting",method = RequestMethod.GET)
    public String getSettingPage()
    {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path="/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImg, Model model)
    {
        if(headerImg==null)
        {
            model.addAttribute("error","还未上传图片");
            return "/site/setting";
        }

        String fileName=headerImg.getOriginalFilename();
        String surfix=fileName.substring(fileName.lastIndexOf(".")+1);
        if(StringUtils.isBlank(surfix))
        {
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }

        //生成随机文件名
        fileName=CommunityUtil.generateUUID()+surfix;
        //确认文件储存路径
        File destination =new File(uploadPath+"/"+fileName);
        try {
            headerImg.transferTo(destination);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw  new RuntimeException("上传文件失败，服务器发生异常");
          //  e.printStackTrace();
        }
        //更新当前用户头像的路径(web)
        //http:localhost:8080/community/user/header/xxx.png
        User user=hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";

    }

    @LoginRequired
    @RequestMapping(path="/alterpw",method=RequestMethod.POST)
    public String alterPassWord(String oldPassword,String newPassword,String confirmPassword,Model model)
    {

            if(StringUtils.isBlank(oldPassword))
            {
                model.addAttribute("oldPasswordError","原始密码不能为空");
                return "/site/setting";
            }
            if(StringUtils.isBlank(newPassword))
            {
                model.addAttribute("newPasswordError","新密码不能为空");
                return "/site/setting";
            }

            if(StringUtils.isBlank(confirmPassword)||!newPassword.equals(confirmPassword))
            {
                model.addAttribute("confirmPasswordError","两次输入密码不一致！");
                return "/site/setting";
            }
            if(newPassword.equals(oldPassword))
            {
                model.addAttribute("newPasswordError","新密码不能与原密码一致");
                return "/site/setting";

            }

            User user=hostHolder.getUser();
            boolean success=userService.alterPassword(oldPassword,newPassword,user);
            if(!success)
            {
                model.addAttribute("oldPasswordError","原密码错误");
                return "/site/setting";
            }

            return "redirect:/index";
    }
    @RequestMapping(path="/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse response)
    {
        //找到服务器存放的路径
        fileName=uploadPath+"/"+fileName;
        String surfix=fileName.substring(fileName.lastIndexOf(".")+1);
        //开始响应图片
        response.setContentType("image/"+surfix);
        try(FileInputStream fis=new FileInputStream(fileName);
            OutputStream os=response.getOutputStream())
        {

            byte[]buffer=new byte[1024];
            int b=0;
            while((b=fis.read(buffer))>=0)
            {
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取图像失败"+e.getMessage());
        }
    }

    //个人主页
    @RequestMapping(path="/profile/{userId}",method =RequestMethod.GET )
    public String getProfilePage(@PathVariable("userId") int userId,Model model)
    {
        User user=userService.finUserById(userId);
        if(user==null)
        {
            throw new RuntimeException("用户不存在");
        }

        model.addAttribute("user",user);

        int likeCount=likeService.findUserLikeCount(user.getId());
        model.addAttribute("userLikeCount",likeCount);

        //关注数量
        long followeeCount=followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount=followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed=false;
        if(hostHolder.getUser()!=null)
            hasFollowed=followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

}
