package com.koyokoyo.community.community.controller.intercepter;

import com.koyokoyo.community.community.entity.LoginTicket;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.service.UserService;
import com.koyokoyo.community.community.util.CookieUtil;
import com.koyokoyo.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketIntercepter implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket= CookieUtil.getValue(request,"ticket");

        if(ticket!=null)
        {
            LoginTicket loginTicket=userService.findLoginTicket(ticket);
            //检查凭证有效性
            if(loginTicket!=null&&loginTicket.getStatus()!=1&&loginTicket.getExpired().after(new Date()))
            {
                User user=userService.finUserById(loginTicket.getUserId());
                //在本次请求中持有用户
                //在多线程情况下保持隔离
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    //在模板之前调用，可以将用户信息渲染到模板中
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user=hostHolder.getUser();
        if(user!=null&&modelAndView!=null)
        {
            modelAndView.addObject("loginUser",user);
            System.out.println(user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
