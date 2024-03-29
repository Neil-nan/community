package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;//所有的Controller都添加了拦截器，那就一定有该线程

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        //检查是否为空
        if(headerImage == null){
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //判断格式
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件的格式不正确！");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //储存文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        //更新当前用户的头像的路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png （后面有方法进行相关路径的访问）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        //改user头像的路径
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //访问头像的web路径(方法类比验证码的显示)
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //相应图片，将图片输出给浏览器
        response.setContentType("image/" + suffix);


        //没有按照文档写，手动关流
        FileInputStream fis = null;
        OutputStream os = null;
        try {
            //输入流
            fis = new FileInputStream(fileName);
            //输出流
            os = response.getOutputStream();
            //一边读，一边写
            byte[] bytes = new byte[1024];
            int readCount = 0;
            while ((readCount = fis.read(bytes)) != -1){
                os.write(bytes, 0, readCount);
            }
            //输出流刷新
            os.flush();
        } catch (IOException e) {
            log.error("读取头像失败: " + e.getMessage());
        } finally {
            //分开try，防止其中一个异常互相影响
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    log.error("输出流错误" + e.getMessage());
                }
            }

            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("文件输入流错误" + e.getMessage());
                }
            }
        }

    }

    @LoginRequired
    @PostMapping("/password")
    public String changePassword(String oldPassword, String newPassword, String rePassword, Model model, @CookieValue("ticket") String ticket){
        //先获取user
        User user = hostHolder.getUser();

        //先验证new和re
        if(StringUtils.isBlank(newPassword) || StringUtils.isBlank(rePassword) || !newPassword.equals(rePassword)){
            model.addAttribute("reMsg", "密码不一致！");
            return "/site/setting";
        }

        //验证新旧账号
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if(map.containsKey("success")){//成功,然后重新登录
            userService.logout(ticket);
            return "redirect:/login";
        }else {
            model.addAttribute("oldMsg", map.get("oldMsg"));
            model.addAttribute("newMsg", map.get("newMsg"));
            return "/site/setting";
        }
    }

    //个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }

        //用户
        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }

        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

}
