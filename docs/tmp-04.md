

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


@AssertTrue原本是直接用于boolean类型属性上的.
当@AssertTrue作用于getter方法时,getter方法名需要符合JavaBean规范中getter名的规定.
getter名以 is/get 开头,getter方法返回类型为Boolean/boolean,getter方法不能有入参.


方法名会被识别为一个名为 emailOrMobilePresent 的布尔属性
上面用 isEmailOrMobilePresent() 是正确的。如果写成 checkEmailOrMobile(),虽然某些版本仍会执行校验,但不推荐。
```