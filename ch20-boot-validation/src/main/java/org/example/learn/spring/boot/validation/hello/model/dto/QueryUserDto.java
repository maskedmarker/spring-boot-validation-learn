package org.example.learn.spring.boot.validation.hello.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QueryUserDto {

    @NotNull
    private Long id;
}
