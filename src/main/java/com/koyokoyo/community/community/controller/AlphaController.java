package com.koyokoyo.community.community.controller;

import com.koyokoyo.community.community.entity.DiscussPost;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.service.AlphaService;
import com.koyokoyo.community.community.service.DiscussPostService;
import com.koyokoyo.community.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;


    @RequestMapping("/hello")
    @ResponseBody
    public String init()
    {
        return "Hello, Koyokoyo";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData()
    {
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration =request.getHeaderNames();
       // response.getHeaderNames();
        while(enumeration.hasMoreElements())
        {
            String name=enumeration.nextElement();
            String value=request.getHeader(name);
            System.out.println(name+": "+value);
        }
        System.out.println(request.getParameter("code"));

        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try (PrintWriter writer=response.getWriter();){

            writer.write("<h1>?Koyo?<h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Get
    //
    @RequestMapping(path="/student", method=RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name="current",required = false,defaultValue = "1") int current,
            @RequestParam(name="limit",required =false,defaultValue = "10") int limit)
    {
        return "some students,current: "+current+" limit: "+limit;
    }
    @RequestMapping(path="/min",method = RequestMethod.GET)
    @ResponseBody
    public String getMinimum(
            @RequestParam(name="var1",required = false,defaultValue = "0") int var1,
            @RequestParam(name="var2",required = false,defaultValue = "0") int var2)
    {
        int minimum=Math.min(var1,var2);
        return "The minimum of these two ints is: "+minimum;
    }
    @RequestMapping(path="/students/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id)
    {
        return "a student with id"+id;
    }

    //Post request
    @RequestMapping(path="/poststu",method=RequestMethod.POST,name="Header")
    @ResponseBody
    public String saveStudent(String name,int age)
    {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    //响应html
    //对应于在templates的view.html根据模板生成的网页
    @RequestMapping(path="teachers",method=RequestMethod.GET)
    public ModelAndView getTeacher()
    {
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.addObject("name","肖欣荣");
        modelAndView.addObject("age","36");
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    @RequestMapping(path="/school",method = RequestMethod.GET)
    public String getSchool(Model model)
    {
        model.addAttribute("name","RUC");
        model.addAttribute("age","70?");
        return "/demo/view";
    }
    @RequestMapping(path="/math",method = RequestMethod.GET)
    public String getAnswer(Model model)
    {
        model.addAttribute("name","Bible");
        model.addAttribute("answer","Genuine Holy");
        return "/demo/MVCdemo";
    }
    //响应JSON，异步请求 例子，注册网站时用户名重复的判断
    //Java Object->JSON String->JS Object
    @RequestMapping(path="emp",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getEmp()
    {
        Map<String,Object> map=new HashMap<>();
        map.put("name","肖欣荣");
        map.put("age",36);
        map.put("salary","no access");
        return map;
    }
    @RequestMapping(path="/discussresults",method = RequestMethod.POST)
    @ResponseBody
    public String getDiscussions(int userId)
    {
        StringBuilder sb=new StringBuilder();
        List<DiscussPost> list=discussPostService.findDiscussPosts(userId,0,10);
        for(DiscussPost discussPost:list)
        {
            sb.append(discussPost.toString());
            sb.append("\n");
        }
        return sb.toString();

    }
    @RequestMapping(path="/header",method=RequestMethod.POST)
    public ModelAndView showHeaders(@RequestParam(name="userId",required = false,defaultValue = "151")int userId)
    {
        ModelAndView modelAndView=new ModelAndView();

        User user= userService.finUserById(userId);
        modelAndView.addObject ("headerUrl",user.getHeaderUrl());
        modelAndView.setViewName("/demo/headerview");
        System.out.println(user.getHeaderUrl());
        return modelAndView;
    }
}
