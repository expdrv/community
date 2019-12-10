package com.koyokoyo.community.community.controller;

import com.koyokoyo.community.community.annotation.LoginRequired;
import com.koyokoyo.community.community.entity.Page;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.service.FollowService;
import com.koyokoyo.community.community.service.UserService;
import com.koyokoyo.community.community.util.CommunityConstant;
import com.koyokoyo.community.community.util.CommunityUtil;
import com.koyokoyo.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path="/follow",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType,int entityId)
    {
        User user=hostHolder.getUser();

        followService.follow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"关注成功！");
    }

    @RequestMapping(path="/unfollow",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String unfollow(int entityType,int entityId)
    {
        User user=hostHolder.getUser();

        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"取关成功！");
    }

    @RequestMapping(path="/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model)
    {
        User user=userService.finUserById(userId);
        if(user==null)
            throw new RuntimeException("用户不存在");
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String,Object>> userList=followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if(userList!=null)
        {
            for(Map<String,Object> map:userList)
            {
                User followee=(User) map.get("user");
           //     Date followTime=(Date) map.get("followTime");
                map.put("hasFollowed",hasFollowed(followee.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }
    @RequestMapping(path="/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model)
    {
        User user=userService.finUserById(userId);
        if(user==null)
            throw new RuntimeException("用户不存在");
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("followers/"+userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String,Object>> userList=followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if(userList!=null)
        {
            for(Map<String,Object> map:userList)
            {
                User follower=(User) map.get("user");
                //     Date followTime=(Date) map.get("followTime");
                map.put("hasFollowed",hasFollowed(follower.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }
    private boolean hasFollowed(int userId)
    {
        if(hostHolder.getUser()==null)
            return false;
        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }

}
