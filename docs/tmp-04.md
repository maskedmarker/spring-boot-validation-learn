

## @AssertTrue 标注的方法必须满足 JavaBean 规范


```java
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String gender;
    private String email;
    private String mobile;

    @AssertTrue(message = "email 和 mobile 不能同时为空")
    public boolean isEmailOrMobilePresent() {
        return isNotBlank(email) || isNotBlank(mobile);
    }

    private static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
```

```text
@AssertTrue 标注的方法必须满足 JavaBean 规范:
以 is 开头返回 boolean,或以 get 开头返回 Boolean/boolean
方法不能有参数
方法名会被识别为一个名为 emailOrMobilePresent 的布尔属性

上面用 isEmailOrMobilePresent() 是正确的。如果写成 checkEmailOrMobile(),虽然某些版本仍会执行校验,但不推荐。
```