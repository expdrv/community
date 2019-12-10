package com.koyokoyo.community.community.controller;

import com.koyokoyo.community.community.annotation.LoginRequired;
import com.koyokoyo.community.community.entity.Comment;
import com.koyokoyo.community.community.entity.DiscussPost;
import com.koyokoyo.community.community.entity.Page;
import com.koyokoyo.community.community.entity.User;
import com.koyokoyo.community.community.service.CommentService;
import com.koyokoyo.community.community.service.DiscussPostService;
import com.koyokoyo.community.community.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "尚未登录");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0, "发送成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        User user = userService.finUserById(post.getUserId());
        model.addAttribute("user", user);

        long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
        model.addAttribute("likeCount",likeCount);


        int likeStaus=hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(
                hostHolder.getUser().getId(),ENTITY_TYPE_POST,post.getId());

        model.addAttribute("likeStatus",likeStaus);
        //评论分页信息
        //评论：给帖子的评论
        //回复：给评论的评论
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        //此处分页

        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        /**
         * Vo mean view object 显示的对象
         */
        //评论Vo列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //其中一个评论的Vo和其作者
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                commentVo.put("user", userService.finUserById(comment.getUserId()));

                likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);

                likeStaus=hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(
                        hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());

                commentVo.put("likeStatus",likeStaus);

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                //回复Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复以及作者
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.finUserById(reply.getUserId()));
                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.finUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        //点赞
                        likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);

                        likeStaus=hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(
                                hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());

                        replyVo.put("likeStatus",likeStaus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                int replyCount=commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }


        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }
}