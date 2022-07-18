package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {

    private int id;
    private int userId;
    //评论的目标类
    private int entityType;
    //目标的id
    private int entityId;
    //指向的人
    private int targetId;
    private String content;
    //状态（删除）
    private int status;
    private Date createTime;
}
