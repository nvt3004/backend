package com.models;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {
	    private String fullName;
	    private String username;
	    private String password;
	    private String image;
	    private Date birthday;
	    private int gender;
}
