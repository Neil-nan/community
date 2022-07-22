package com.nowcoder.community;

import com.nowcoder.community.dao.*;
import com.nowcoder.community.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.Date;
import java.util.List;

//@RunWith(SpringRunner.class)
@SpringBootTest
//@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("Neil");
        user.setPassword("liunan548662");
        user.setSalt("adc");
        user.setEmail("2378428641@qq.com");
        user.setHeaderUrl("http://images.nowcoder.com/head/896t.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int rows = userMapper.updateStatus(152, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(152, "http://images.nowcoder.com/head/23t.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(152, "LiuNam548626");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for(DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testInsertDiscussPost(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(11);
        discussPost.setTitle("老子是管理员");
        discussPost.setContent("都给老子安静！");
        discussPost.setCreateTime(new Date());

        discussPostMapper.insertDiscussPost(discussPost);
    }

    @Test
    public void testSelectDiscussPostById(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(283);

        System.out.println(post.getTitle());
        System.out.println(post.getContent());
    }

    @Test
    public void testSelectCommentsByEntity(){
        List<Comment> comments = commentMapper.selectCommentsByEntity(1, 228, 0, 15);
        for (Comment comment : comments) {
            System.out.println(comment);
        }

        int count = commentMapper.selectCountByEntity(1, 228);
        System.out.println(count);
    }

    @Test
    public void testInsertComment(){
        Comment comment = new Comment();
        comment.setUserId(158);
        comment.setEntityType(1);
        comment.setEntityId(228);
        comment.setTargetId(0);
        comment.setContent("你好");
        comment.setCreateTime(new Date());

        commentMapper.insertComment(comment);
    }

    @Test
    public void testSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);
    }
}
