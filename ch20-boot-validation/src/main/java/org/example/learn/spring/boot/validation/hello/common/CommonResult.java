package org.example.learn.spring.boot.validation.hello.common;

import lombok.Data;

@Data
public class CommonResult<T> {

    private Integer code;
    private String message;
    private T data;

    private CommonResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(200, "success", data);
    }

    public static <T> CommonResult<T> fail(Integer code, String message) {
        return new CommonResult<>(code, message, null);
    }
}
