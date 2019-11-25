package com.koyokoyo.community.community.service;

import com.koyokoyo.community.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype")  单例与多例
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;
    public AlphaService()
    {
        System.out.println("Constructing");
    }

    @PostConstruct
    public void init()
    {
        System.out.println("Initialization Done");
    }

    @PreDestroy
    public void destroy()
    {
        System.out.println("To be finalized");
    }

    public String find()
    {
        return alphaDao.select();
    }
}
