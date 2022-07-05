package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@Slf4j
@RequestMapping("/user")
public class UserController {

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

}
