package com.koyokoyo.community.community.controller;

import com.koyokoyo.community.community.entity.Message;
import com.koyokoyo.community.community.entity.Page;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.service.MessageService;
import com.koyokoyo.community.community.service.UserService;
import com.koyokoyo.community.community.util.CommunityUtil;
import com.koyokoyo.community.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    @RequestMapping(path="/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page)
    {
        //Integer.valueOf("anc");
        User user=hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList=messageService.findConversations(
                user.getId(),page.getOffset(),page.getLimit());

        List<Map<String,Object>> conversations=new ArrayList<>();
        if(conversationList!=null)
        {
            for(Message message:conversationList)
            {
                Map<String,Object> map=new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.
                        findLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId=user.getId()==message.getFromId()?message.getToId():message.getFromId();
                map.put("target",userService.finUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        //查询总未读消息数量
        int letterUnreadCount=messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path="/letter/detail/{conversationId}",method=RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId")String conversationId,Page page,Model model)
    {
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> list=messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> letters=new ArrayList<>();
        if(list!=null)
        {
            for(Message message:list)
            {
                Map<String,Object> map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.finUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",getLetterTarget(conversationId));

        List<Integer> ids=getLetterIds(list);
        if(!ids.isEmpty())
        {
            messageService.readMessages(ids,1);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getLetterIds(List<Message> letterList)
    {
        List<Integer> ids=new ArrayList<>();
        if(letterList!=null)
        {
            for(Message message:letterList)
            {
                if(hostHolder.getUser().getId()==message.getToId()&&message.getStatus()==0)
                {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }
    private User getLetterTarget(String conversationId)
    {
        String[] ids=conversationId.split("_");
        int id0=Integer.parseInt(ids[0]);
        int id1=Integer.parseInt(ids[1]);
        User user=hostHolder.getUser();
        return (user.getId()==(id0)?userService.finUserById(id1):userService.finUserById(id1));
    }

    @RequestMapping(path="/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content)
    {
       // Integer.valueOf("abc");
        User target=userService.findUserByName(toName);
        if(target==null)
        {
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message=new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId())
        {
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }
        else
        {
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

}
