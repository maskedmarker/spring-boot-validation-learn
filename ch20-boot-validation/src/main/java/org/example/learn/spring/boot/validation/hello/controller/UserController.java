package org.example.learn.spring.boot.validation.hello.controller;

import org.example.learn.spring.boot.validation.hello.model.User;
import org.example.learn.spring.boot.validation.hello.model.dto.QueryUserDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {


    @PostMapping("/getById")
    public User getById(@Valid @RequestBody QueryUserDto queryUserDto) {
        return User.builder().id(1L).name("zhangSan").build();
    }
}
