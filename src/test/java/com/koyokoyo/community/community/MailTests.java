package com.koyokoyo.community.community;

import com.koyokoyo.community.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail()
    {
        mailClient.sendMail("expdrv@qq.com","communiy hello","I will bring you to the land I swore");
    }

    @Test
    public void testHtmlMail()
    {
        Context context=new Context();
        context.setVariable("username","workharder");

        String content=templateEngine.process("/mail/demo",context);
        System.out.println(content);

        mailClient.sendMail("expdrv@qq.com","htmlmail",content);
    }
}
