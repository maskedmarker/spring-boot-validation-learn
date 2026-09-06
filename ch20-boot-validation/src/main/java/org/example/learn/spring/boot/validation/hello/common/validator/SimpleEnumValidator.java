package org.example.learn.spring.boot.validation.hello.common.validator;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
public class SimpleEnumValidator implements ConstraintValidator<EnumValidator, String> {

    private EnumValidator constraintAnnotation;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String[] enumValues = constraintAnnotation.value();
        for (String enumValue : enumValues) {
            if (enumValue.equals(value)) {
                return true;
            }
        }

        return false;
    }
}
