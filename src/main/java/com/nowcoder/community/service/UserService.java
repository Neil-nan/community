package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("http://localhost:8080")
    private String domain;

    @Value("/community")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;


    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    //注册方法
    public Map<String, Object> register(User user){

        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null){
            try {
                throw new IllegalAccessException("参数不能为空");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //账号验证
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "该账号已经存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg", "该邮箱已经被注册");
            return map;
        }

        //全部通过后，注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getSalt() + user.getPassword()));
        //创建用户，但没有被激活
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        //用拼接，不要写死
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);


        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);

        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        //验证状态
        if(user.getStatus() == 0){
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(user.getSalt() + password);
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    //修改头像
    public int updateHeader(int userId, String headerUrl){
        return userMapper.updateHeader(userId, headerUrl);
    }



}
