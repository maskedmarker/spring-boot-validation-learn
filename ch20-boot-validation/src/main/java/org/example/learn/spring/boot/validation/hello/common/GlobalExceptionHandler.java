package org.example.learn.spring.boot.validation.hello.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * ExceptionHandlerExceptionResolver收集所有携带@ControllerAdvice注解的bean,然后携带@ControllerAdvice注解的bean被定义为ExceptionHandlerMethodResolver
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 默认情况下:DefaultHandlerExceptionResolver会将validation的错误信息抓换为400-Bad Request
     * 如果自定义ExceptionHandler,那么就需要定义统一的返回报文结构(比如这里的CommonResult)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK) // 通过CommonResult.code来区分,而非http_status_code
    @ResponseBody
    public CommonResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        // 多个错误用逗号拼接
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return CommonResult.fail(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 另一种写法
     */
/*    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResult<Void>> handleMethodArgumentNotValid2(MethodArgumentNotValidException ex) {
        // 多个错误用逗号拼接
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        // 当返回ResponseEntity时,可以省略掉@ResponseStatus和@ResponseBody
        return ResponseEntity.ok(CommonResult.fail(HttpStatus.BAD_REQUEST.value(), message));
    }*/
}
