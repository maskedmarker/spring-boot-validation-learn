package org.example.learn.spring.boot.validation.hello.controller;

import org.example.learn.spring.boot.validation.hello.common.CommonResult;
import org.example.learn.spring.boot.validation.hello.model.User;
import org.example.learn.spring.boot.validation.hello.model.request.CreateUserRequest;
import org.example.learn.spring.boot.validation.hello.model.request.QueryUserRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final AtomicLong userIdGenerator = new AtomicLong(1);

    private Map<Long, User> users = new HashMap<>();

    @PostMapping("/create")
    public CommonResult<User> create(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .id(userIdGenerator.getAndIncrement())
                .name(request.getName())
                .gender(request.getGender())
                .email(request.getEmail())
                .build();

        users.put(user.getId(), user);

        return CommonResult.success(user);
    }

    @PostMapping("/getById")
    public CommonResult<User> getById(@Valid @RequestBody QueryUserRequest request) {
        return CommonResult.success(users.get(request.getId()));
    }
}
