package com.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.entities.User;
import lombok.Data;

import java.util.Date;
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
    private String fullName;
    private String username;
    private String phone;
    private String email;
    private String password;
    private String image;
    private String provider;
    private Date creatDate;
    private Date birthDate;
    private Integer gender;
    private User listData;
    private List<User> userList;
    private List<String> roles;
    private String tokenType;
    private List<String> permissions;
}
