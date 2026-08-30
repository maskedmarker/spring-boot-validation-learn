



```text
一、三个角色分工


javax.validation:validation-api	JSR-303/380	只定义注解(@NotNull、@Valid …)和接口(Validator、ConstraintValidator)
hibernate-validator	实现	注解的校验引擎：解析约束、实例化校验器、执行 isValid、收集 ConstraintViolation
spring-framework/spring-boot 桥接 + 接线	把上述引擎适配成 Spring 的校验抽象，并埋到 MVC 参数解析流程里
```

```text
二、启动装配阶段：validator 从哪来，怎么进 MVC


1. 校验引擎 Bean 的诞生
```


```text
public class DefaultHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) {
        try {
                // ...
                else if (ex instanceof MethodArgumentNotValidException) {
                    return handleMethodArgumentNotValidException((MethodArgumentNotValidException) ex, request, response, handler);
                }
                // ...
        }
        catch (Exception handlerEx) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failure while trying to resolve exception [" + ex.getClass().getName() + "]", handlerEx);
            }
        }
        return null;
    }
}
```

```text
// org.springframework.web.servlet.DispatcherServlet.processHandlerException

protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) throws Exception {

    // Success and error responses may use different content types
    request.removeAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

    // Check registered HandlerExceptionResolvers...
    ModelAndView exMv = null;
    if (this.handlerExceptionResolvers != null) {
        for (HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
            exMv = resolver.resolveException(request, response, handler, ex);
            if (exMv != null) {
                break;
            }
        }
    }
    if (exMv != null) {
        if (exMv.isEmpty()) {
            // 
            request.setAttribute(EXCEPTION_ATTRIBUTE, ex);
            return null;
        }
        // We might still need view name translation for a plain error model...
        if (!exMv.hasView()) {
            String defaultViewName = getDefaultViewName(request);
            if (defaultViewName != null) {
                exMv.setViewName(defaultViewName);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Using resolved error view: " + exMv, ex);
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("Using resolved error view: " + exMv);
        }
        WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
        return exMv;
    }

    throw ex;
}
```


```text
//org.springframework.web.method.annotation.ExceptionHandlerMethodResolver.getMappedMethod

private Method getMappedMethod(Class<? extends Throwable> exceptionType) {
    List<Class<? extends Throwable>> matches = new ArrayList<>();
    for (Class<? extends Throwable> mappedException : this.mappedMethods.keySet()) {
        if (mappedException.isAssignableFrom(exceptionType)) {
            matches.add(mappedException);
        }
    }
    if (!matches.isEmpty()) {
        matches.sort(new ExceptionDepthComparator(exceptionType));  // 最具体的异常类被优先处理
        return this.mappedMethods.get(matches.get(0));
    }
    else {
        return null;
    }
}
```


```text
hibernate-validator是怎么作用域spring mvc的入参校验?

Hibernate Validator 在 Spring MVC 中起作用，核心是通过两个不同的“抓手”：
一个是RequestResponseBodyMethodProcessor，它负责处理 @RequestBody 这样的请求体校验；
另一个是 MethodValidationPostProcessor，它通过 AOP 拦截来处理 @Validated 注解的类和方法参数校验
```