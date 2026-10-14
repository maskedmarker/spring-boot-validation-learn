package org.example.learn.spring.boot.validation.hello.model.request;

import lombok.Data;
import org.example.learn.spring.boot.validation.hello.common.validator.EnumValidator;
import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertTrue;

@Data
public class CreateUserRequest {

    private String name;

    @EnumValidator(value = {"0", "1"}, message = "性别是非法枚举值")
    private String gender;

    private String email;

    private String mobile;

    @AssertTrue(message = "手机号和邮箱不能同时为空")
    public boolean isContractInfoPresent() {
        return StringUtils.hasText(email) || StringUtils.hasText(mobile);
    }
}
