package org.example.learn.spring.boot.validation.hello.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class User {

    private Long id;

    private String name;

    private String email;

    private Date createTime;

    private Date updateTime;
}
