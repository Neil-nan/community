package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ServiceTests {

    @Autowired
    UserService userService;

    @Autowired
    DiscussPostService discussPostService;

    @Test
    public void testUserService(){
        User user = userService.findUserById(151);
        System.out.println(user);
    }

    @Test
    public void testDiscussService(){
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(149, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }

        int rows = discussPostService.findDiscussPostRows(149);
        System.out.println(rows);
    }
}
