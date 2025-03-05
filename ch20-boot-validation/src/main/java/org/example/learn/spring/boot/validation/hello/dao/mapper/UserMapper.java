package org.example.learn.spring.boot.validation.hello.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.learn.spring.boot.validation.hello.model.User;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> findAll();

    int save(User user);

    User getById(Long id);
}
