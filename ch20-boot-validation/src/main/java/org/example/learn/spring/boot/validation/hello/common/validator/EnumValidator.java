package org.example.learn.spring.boot.validation.hello.common.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SimpleEnumValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValidator {

    String message() default "enum value is invalid";

    String[] value() default {};

    // 分组校验（固定写法,暂时不用）
    Class<?>[] groups() default {};
    // 负载（固定写法,暂时不用）
    Class<? extends Payload>[] payload() default {};

}
