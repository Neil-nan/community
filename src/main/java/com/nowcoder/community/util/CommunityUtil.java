package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    //生成随记字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }

    //MD5加密
    //密码加随机字符串拼接
    //输入的是字符串，不是密码
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }

        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
