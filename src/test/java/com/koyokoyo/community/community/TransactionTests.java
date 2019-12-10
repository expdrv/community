package com.koyokoyo.community.community;

import com.koyokoyo.community.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)
public class TransactionTests {
    @Autowired
    private AlphaService alphaService;

    @Test
    public void testTransaction()
    {
        Object obj=alphaService.save1();
        System.out.println(obj);
    }

    @Test
    public void testTransaction2()
    {
        Object obj=alphaService.save2();
        System.out.println(obj);
    }
}
