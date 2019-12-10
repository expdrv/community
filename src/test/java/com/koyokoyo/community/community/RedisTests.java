package com.koyokoyo.community.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes=CommunityApplication.class)
public class RedisTests {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings()
    {
        String redisKey="test:count";

        redisTemplate.opsForValue().set(redisKey,1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
    }

    @Test
    public void testHashes()
    {
        String redisKey="test:user";
        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","zz");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testList()
    {
        String redisKey="test:ids";

        redisTemplate.opsForList().leftPush(redisKey,100);
        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,2));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
    }

    @Test
    public void testSet()
    {
        String redisKey="test:teachers";

        redisTemplate.opsForSet().add(redisKey,"hape","zz","nmsl","sxc");

        System.out.println(redisTemplate.opsForSet().members(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));

    }

    @Test
    public void testSortedSets()
    {
        String redisKey="test:student";

        redisTemplate.opsForZSet().add(redisKey,"yourfather",100);
        redisTemplate.opsForZSet().add(redisKey,"hmp",59);
        redisTemplate.opsForZSet().add(redisKey,"genius",0);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"hmp"));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"genius"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"genius"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,2));
    }

    @Test
    public void testKeys() throws InterruptedException {
        String redisKey="test:user";

        System.out.println(redisTemplate.hasKey(redisKey));

        redisTemplate.expire(redisKey,1, TimeUnit.SECONDS);
        Thread.currentThread().sleep(2000);

        System.out.println(redisTemplate.hasKey(redisKey));
    }

    @Test
    public void testBoundOperations()
    {
        String redisKey="test:count";
        BoundValueOperations operations=redisTemplate.boundValueOps(redisKey);
        operations.increment();
        System.out.println(operations.get());
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    @Test
    public void testTransactionl()
    {
       Object obj= redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey="test:tx";

                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey,"hapezg");
                redisOperations.opsForSet().add(redisKey,"tsq");
                redisOperations.opsForSet().add(redisKey,"xhrnmsl");
                redisOperations.opsForSet().add(redisKey,"huashengbiss");

                System.out.println(redisOperations.opsForSet().members(redisKey));

                return redisOperations.exec();
            }
        });
       System.out.println(obj.getClass());
        System.out.println(obj);
    }
}
