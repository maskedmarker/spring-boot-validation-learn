package org.example.learn.spring.boot.validation.hello.service;


import org.example.learn.spring.boot.validation.hello.dao.mapper.UserMapper;
import org.example.learn.spring.boot.validation.hello.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> findAllUsers() {
        return userMapper.findAll();
    }

    public User saveUser(User user) {
        if (user.getCreateTime() == null) {
            user.setCreateTime(new Date());
        }
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(new Date());
        }

        userMapper.save(user);

        return user;
    }

    public User getById(Long id) {
        return userMapper.getById(id);
    }
}