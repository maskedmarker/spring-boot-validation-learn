package org.example.learn.spring.boot.validation.hello.model.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QueryUserRequest {

    @NotNull(message = "id不能为空")
    private Long id;
}
