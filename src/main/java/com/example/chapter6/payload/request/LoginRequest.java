package com.example.chapter6.payload.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class LoginRequest {

    @NotNull
    private String userId;
    @NotNull
    private String password;

    public LoginRequest(){

    }

    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}
