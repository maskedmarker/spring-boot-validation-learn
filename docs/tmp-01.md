# spring-validation

```text
核心点:
DataBinder不仅支持绑定属性值,还支持对绑定后的属性值进行验证.🎯🎯🎯🎯🎯🎯
对于spring-mvc,WebDataBinder作为DataBinder的子类,在解析request请求中并绑定到目标handler方法参数时,会先校验,此时可能会触发校验失败异常.

一句话串起来
HTTP 请求 → DispatcherServlet → RequestMappingHandlerAdapter 
    → RequestResponseBodyMethodProcessor.resolveArgument（Jackson 反序列化）
    → validateIfApplicable（看到 @Valid）→ DataBinder.validate → SpringValidatorAdapter.validate（Spring 接口 → JSR 接口）
    → ValidatorImpl.validate → ConstraintTree → NotNullValidator.isValid → ConstraintViolation → 译回 FieldError 
    → 抛 MethodArgumentNotValidException → 400。

```




```text
@Validated是spring-framework的注解,
@Valid是JSR-303/380的注解.


Spring Boot 2.3.2 
    → spring-framework 5.2.8.RELEASE + hibernate-validator 6.1.5.Final + validation-api 2.0.1.Final。

直接给结论：
hibernate-validator 只是"校验引擎"，它自己是不知道 MVC 的。
真正把它接到 MVC 参数校验上的是 Spring 自己——Spring 用 SpringValidatorAdapter 把 JSR-303 的 javax.validation.Validator 包装成 Spring 的 Validator/SmartValidator，然后由 MVC 的参数解析器在反序列化完参数后调用它。 
整个过程分两段：启动时的"装配"，和请求时的"调用"。
```

## 关键类
```text
类	                                                 位置	                                        作用
RequestResponseBodyMethodProcessor	                 spring-webmvc	                            @RequestBody 解析 + 触发校验
AbstractMessageConverterMethodArgumentResolver	     spring-webmvc	                            validateIfApplicable（@Valid/@Validated 判断）
DataBinder	                                         spring-context	                            validate(Object...) 遍历校验器
SpringValidatorAdapter	                             spring-context                             桥：JSR↔Spring 双向翻译
LocalValidatorFactoryBean	                         spring-context 	                        引导 hibernate-validator，生成 ValidatorImpl
ValidationAutoConfiguration	                         spring-boot-autoconfigure	                自动装配上面的 Bean
HibernateValidator	                                 hibernate-validator	                    SPI 入口（META-INF/services/...ValidationProvider）
ValidatorImpl	                                     hibernate-validator	                    引擎主流程 validate → validateInContext
ConstraintTree	                                     hibernate-validator	                    单个约束的实例化 + isValid 执行
NotNullValidator	                                 hibernate-validator	                    @NotNull 的具体校验器
```

```text
三个角色分工


javax.validation:validation-api	JSR-303/380	只定义注解(@NotNull、@Valid …)和接口(Validator、ConstraintValidator)
hibernate-validator	实现	注解的校验引擎：解析约束、实例化校验器、执行 isValid、收集 ConstraintViolation
spring-framework/spring-boot 桥接 + 接线	把上述引擎适配成 Spring 的校验抽象，并埋到 MVC 参数解析流程里
```



## HandlerMethodArgumentResolver

RequestPartMethodArgumentResolver在resolveArgument时,进行了入参校验

```text
public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

    parameter = parameter.nestedIfOptional();
    Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
    String name = Conventions.getVariableNameForParameter(parameter);

    if (binderFactory != null) {
        // WebDataBinder的target是arg
        WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
        if (arg != null) {
            // 校验入参
            validateIfApplicable(binder, parameter);
            if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
            }
        }
        // ...
    }

    // 为了支持handlerMethod入参为Optional类型 
    return adaptArgumentIfNecessary(arg, parameter);
}

protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
    Annotation[] annotations = parameter.getParameterAnnotations();
    for (Annotation ann : annotations) {
        // 原本支持spring-framework的注解@Validated
        Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
        // 后来又支持其他框架的注解(比如 javax)
        if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
            Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
            Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[] {hints});
            // 校验的入口在 org.springframework.validation.DataBinder#validate
            binder.validate(validationHints);
            // 只支持其中一种
            break;
        }
    }
}
```

## DataBinder

DataBinder不仅支持绑定属性值,还支持对绑定后的属性值进行验证.🎯🎯🎯🎯🎯🎯

```text
public void validate(Object... validationHints) {
    Object target = getTarget();
    Assert.state(target != null, "No target to validate");
    BindingResult bindingResult = getBindingResult();
    // Call each validator with the same binding result
    for (Validator validator : getValidators()) {
        // validationHints是SmartValidator需要的入参,其他类型的不需要
        if (!ObjectUtils.isEmpty(validationHints) && validator instanceof SmartValidator) {
            ((SmartValidator) validator).validate(target, bindingResult, validationHints);
        }
        else if (validator != null) {
            validator.validate(target, bindingResult);
        }
    }
}
```

### WebDataBinder

WebDataBinder is Special DataBinder for data binding from web request parameters to JavaBean objects
WebDataBinder的validator来自于ConfigurableWebBindingInitializer.
WebDataBinder的validator具体是ValidatorAdapter,ValidatorAdapter.target是LocalValidatorFactoryBean


```text
// org.springframework.web.bind.support.ConfigurableWebBindingInitializer.initBinder

public void initBinder(WebDataBinder binder) {
    // ...
    // 每次生成WebDataBinder时, 初始化时validator如果supports时,才会添加validator
    if (this.validator != null && binder.getTarget() != null && this.validator.supports(binder.getTarget().getClass())) {
        binder.setValidator(this.validator);
    }
    if (this.conversionService != null) {
        binder.setConversionService(this.conversionService);
    }
    // ...
}
```


## DefaultDataBinderFactory

```text

```





```text
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod

protected ModelAndView invokeHandlerMethod(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

    ServletWebRequest webRequest = new ServletWebRequest(request, response);
    try {
        // 获取WebDataBinderFactory
        WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
        ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

        ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
        if (this.argumentResolvers != null) {
            invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        }
        if (this.returnValueHandlers != null) {
            invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
        }
        // InvocableHandlerMethod初始化阶段,为其设置WebDataBinderFactory
        invocableMethod.setDataBinderFactory(binderFactory);
        invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
        modelFactory.initModel(webRequest, mavContainer, invocableMethod);
        mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

        AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
        asyncWebRequest.setTimeout(this.asyncRequestTimeout);

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        asyncManager.setTaskExecutor(this.taskExecutor);
        asyncManager.setAsyncWebRequest(asyncWebRequest);
        asyncManager.registerCallableInterceptors(this.callableInterceptors);
        asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

        if (asyncManager.hasConcurrentResult()) {
            Object result = asyncManager.getConcurrentResult();
            mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
            asyncManager.clearConcurrentResult();
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(result, !traceOn);
                return "Resume with async result [" + formatted + "]";
            });
            invocableMethod = invocableMethod.wrapConcurrentResult(result);
        }

        invocableMethod.invokeAndHandle(webRequest, mavContainer);
        if (asyncManager.isConcurrentHandlingStarted()) {
            return null;
        }

        return getModelAndView(mavContainer, modelFactory, webRequest);
    }
    finally {
        webRequest.requestCompleted();
    }
}
```


```text
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.getDataBinderFactory

private WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) throws Exception {
    Class<?> handlerType = handlerMethod.getBeanType();
    Set<Method> methods = this.initBinderCache.get(handlerType);
    if (methods == null) {
        methods = MethodIntrospector.selectMethods(handlerType, INIT_BINDER_METHODS);
        this.initBinderCache.put(handlerType, methods);
    }
    List<InvocableHandlerMethod> initBinderMethods = new ArrayList<>();
    // Global methods first
    this.initBinderAdviceCache.forEach((controllerAdviceBean, methodSet) -> {
        if (controllerAdviceBean.isApplicableToBeanType(handlerType)) {
            Object bean = controllerAdviceBean.resolveBean();
            for (Method method : methodSet) {
                initBinderMethods.add(createInitBinderMethod(bean, method));
            }
        }
    });
    for (Method method : methods) {
        Object bean = handlerMethod.getBean();
        initBinderMethods.add(createInitBinderMethod(bean, method));
    }
    return createDataBinderFactory(initBinderMethods);
}

protected InitBinderDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> binderMethods) throws Exception {
    return new ServletRequestDataBinderFactory(binderMethods, getWebBindingInitializer());
}
```

