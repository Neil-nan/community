package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class LoginTicket {

    private int id;
    private int userId;
    private String ticket;
    //0有效 1无效
    private int status;
    private Date expired;
}
