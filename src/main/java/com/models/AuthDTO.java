package com.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.entities.User;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthDTO {

    private int statusCode;
    private String error;
    private String message;
    private String token;
    private String refreshToken;
    private String expirationTime;
    private String fullName;
    private String username;
    private String phone;
    private String email;
    private String password;
    private String image;
    // private byte isActive = 1;
    private User listData;
    private List<User> userList;
    private List<String> roles;  // Thêm trường roles vào đây
    private String tokenType;
    private List<String> permissions;

    // Thêm setter cho roles
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
