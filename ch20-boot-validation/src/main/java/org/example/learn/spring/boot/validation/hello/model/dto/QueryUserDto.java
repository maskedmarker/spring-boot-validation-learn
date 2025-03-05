package org.example.learn.spring.boot.validation.hello.model.dto;

import javax.validation.constraints.NotNull;

public class QueryUserDto {

    @NotNull
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
