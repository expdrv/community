package com.koyokoyo.community.community.service;

import com.koyokoyo.community.community.dao.AlphaDao;
import com.koyokoyo.community.community.dao.DiscussPostMapper;
import com.koyokoyo.community.community.dao.UserMapper;
import com.koyokoyo.community.community.entity.DiscussPost;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")  单例与多例
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;
    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired(required = false)
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public String find()
    {
        return alphaDao.select();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save1()
    {

    User user=new User();
    user.setUsername("hapezz");
    user.setSalt(CommunityUtil.generateUUID().substring(0,5));
    user.setPassword(CommunityUtil.md5("12345678"+user.getSalt()));
    user.setEmail("hapezz@nmsl.com");
    user.setHeaderUrl("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1268548801,3001813041&fm=26&gp=0.jpg");
    user.setCreateTime(new Date());

    userMapper.insertUser(user);

    DiscussPost post=new DiscussPost();
    post.setUserId(user.getId());
    post.setTitle("Hello，战场原是我的");
    post.setContent("不服就干");
    post.setCreateTime(new Date());

    discussPostMapper.insertDiscussPost(post);

    Integer.valueOf("fafDd");

    return "ok";
    }

    public Object save2()
    {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {

            @Override
            public Object doInTransaction(TransactionStatus status) {
                User user=new User();
                user.setUsername("hapezz");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("12345678"+user.getSalt()));
                user.setEmail("hapezz@nmsl.com");
                user.setHeaderUrl("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1268548801,3001813041&fm=26&gp=0.jpg");
                user.setCreateTime(new Date());

                userMapper.insertUser(user);

                DiscussPost post=new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("Hello，战场原是我的");
                post.setContent("不服就干");
                post.setCreateTime(new Date());

                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("fafDd");
                return "ok";
            }
        });
    }

}
