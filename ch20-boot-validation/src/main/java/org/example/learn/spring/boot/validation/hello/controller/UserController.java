package org.example.learn.spring.boot.validation.hello.controller;

import org.example.learn.spring.boot.validation.hello.model.User;
import org.example.learn.spring.boot.validation.hello.model.dto.QueryUserDto;
import org.example.learn.spring.boot.validation.hello.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping("/createUser")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @GetMapping("/getById")
    public User getById(@Valid @RequestBody QueryUserDto queryUserDto) {
        return userService.getById(queryUserDto.getId());
    }
}
