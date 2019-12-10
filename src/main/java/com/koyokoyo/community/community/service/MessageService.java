package com.koyokoyo.community.community.service;

import com.koyokoyo.community.community.dao.MessageMapper;
import com.koyokoyo.community.community.entity.Message;
import com.koyokoyo.community.community.util.CommunityUtil;
import com.koyokoyo.community.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired(required = false)
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId,int offset,int limit)
    {
        return messageMapper.selectConversations(userId,offset,limit);
    }

    public int findConversationCount(int userId)
    {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversaionId,int offset,int limit)
    {
        return messageMapper.selectLetters(conversaionId,offset,limit);
    }

    public int findLetterCount(String conversationId)
    {
        return messageMapper.selectLettersCount(conversationId);
    }

    public int findLetterUnreadCount(int userId,String conversationId)
    {
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    public int addMessage(Message message)
    {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessages(List<Integer> ids,int status)
    {
        return messageMapper.updateStatus(ids,status);
    }
}
