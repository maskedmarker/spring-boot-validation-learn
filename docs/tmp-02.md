# spring-boot与validation


spring-framework对于validation的抽象是org.springframework.validation.Validator
```text
org.springframework.validation.Validator

A validator for application-specific objects.
This interface is totally divorced from any infrastructure or context; 
that is to say it is not coupled to validating only objects in the web tier, the data-access tier, or the whatever-tier. 
As such it is amenable to being used in any layer of an application, and supports the encapsulation of validation logic as a first-class citizen in its own right.
```

```text
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(javax.validation.executable.ExecutableValidator.class)
@ConditionalOnResource(resources = "classpath:META-INF/services/javax.validation.spi.ValidationProvider")
@Import(PrimaryDefaultValidatorPostProcessor.class)
public class ValidationAutoConfiguration {

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	@ConditionalOnMissingBean(javax.validation.Validator.class)
	public static LocalValidatorFactoryBean defaultValidator() {
		LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
		MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory();
		factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
		return factoryBean;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public static MethodValidationPostProcessor methodValidationPostProcessor(Environment environment, @Lazy Validator validator) {
		MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
		boolean proxyTargetClass = environment.getProperty("spring.aop.proxy-target-class", Boolean.class, true);
		processor.setProxyTargetClass(proxyTargetClass);
		processor.setValidator(validator);
		return processor;
	}
}
```

在mvc中注册的Validator


```text
public class WebMvcAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
	public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration implements ResourceLoaderAware {
	    @Bean
		@Override
		public Validator mvcValidator() {
			if (!ClassUtils.isPresent("javax.validation.Validator", getClass().getClassLoader())) {
				return super.mvcValidator();
			}
			return ValidatorAdapter.get(getApplicationContext(), getValidator());
		}
	}
	
	@Override
	@Nullable
	protected Validator getValidator() {
		return this.configurers.getValidator();
	}
}
```


```text
public class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {

	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter(
			@Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager,
			@Qualifier("mvcConversionService") FormattingConversionService conversionService,
			@Qualifier("mvcValidator") Validator validator) {

		RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
		adapter.setContentNegotiationManager(contentNegotiationManager);
		adapter.setMessageConverters(getMessageConverters());
		// 
		adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer(conversionService, validator));
		adapter.setCustomArgumentResolvers(getArgumentResolvers());
		adapter.setCustomReturnValueHandlers(getReturnValueHandlers());

		if (jackson2Present) {
			adapter.setRequestBodyAdvice(Collections.singletonList(new JsonViewRequestBodyAdvice()));
			adapter.setResponseBodyAdvice(Collections.singletonList(new JsonViewResponseBodyAdvice()));
		}

		AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();
		configureAsyncSupport(configurer);
		if (configurer.getTaskExecutor() != null) {
			adapter.setTaskExecutor(configurer.getTaskExecutor());
		}
		if (configurer.getTimeout() != null) {
			adapter.setAsyncRequestTimeout(configurer.getTimeout());
		}
		adapter.setCallableInterceptors(configurer.getCallableInterceptors());
		adapter.setDeferredResultInterceptors(configurer.getDeferredResultInterceptors());

		return adapter;
	}
	
	@Bean
	public Validator mvcValidator() {
		Validator validator = getValidator();
		if (validator == null) {
			if (ClassUtils.isPresent("javax.validation.Validator", getClass().getClassLoader())) {
				Class<?> clazz;
				try {
					String className = "org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean";
					clazz = ClassUtils.forName(className, WebMvcConfigurationSupport.class.getClassLoader());
				}
				catch (ClassNotFoundException | LinkageError ex) {
					throw new BeanInitializationException("Failed to resolve default validator class", ex);
				}
				validator = (Validator) BeanUtils.instantiateClass(clazz);
			}
			else {
				validator = new NoOpValidator();
			}
		}
		return validator;
	}
	
	// DelegatingWebMvcConfiguration会override该方法
	protected Validator getValidator() {
		return null;
	}
}
```

## 
```text
org.springframework.boot.autoconfigure.BackgroundPreinitializer.performPreinitialization

private void performPreinitialization() {
    try {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                runSafely(new ConversionServiceInitializer());
                runSafely(new ValidationInitializer());
                runSafely(new MessageConverterInitializer());
                runSafely(new JacksonInitializer());
                runSafely(new CharsetInitializer());
                preinitializationComplete.countDown();
            }

            public void runSafely(Runnable runnable) {
                try {
                    runnable.run();
                }
                catch (Throwable ex) {
                    // Ignore
                }
            }

        }, "background-preinit");
        thread.start();
    }
    catch (Exception ex) {
        // This will fail on GAE where creating threads is prohibited. We can safely
        // continue but startup will be slightly slower as the initialization will now
        // happen on the main thread.
        preinitializationComplete.countDown();
    }
}


private static class ValidationInitializer implements Runnable {

    @Override
    public void run() {
        Configuration<?> configuration = Validation.byDefaultProvider().configure();
        configuration.buildValidatorFactory().getValidator();
    }

}
```