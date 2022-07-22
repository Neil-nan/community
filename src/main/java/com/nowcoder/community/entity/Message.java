package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {

    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    //0未读，1已读，2删除
    private int status;
    private Date createTime;
}
