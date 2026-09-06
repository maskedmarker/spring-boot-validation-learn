package org.example.learn.spring.boot.validation.hello.model.request;

import lombok.Data;
import org.example.learn.spring.boot.validation.hello.common.validator.EnumValidator;

import javax.validation.constraints.NotNull;

@Data
public class CreateUserRequest {

    private String name;

    @EnumValidator(value = {"0", "1"}, message = "性别是非法枚举值")
    private String gender;

    private String email;
}
