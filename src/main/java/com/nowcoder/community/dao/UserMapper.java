package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
//@Component
public interface UserMapper {

    //查找（通过id 姓名 邮箱）
    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    //增加用户（返回行数）
    int insertUser(User user);

    //修改用户
    //修改状态
    int updateStatus(int id, int status);

    //修改头像路径
    int updateHeader(int id, String headUrl);

    //修改密码
    int updatePassword(int id, String password);
}
