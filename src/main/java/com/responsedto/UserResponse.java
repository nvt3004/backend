package com.responsedto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
		private int userId;
		private String fullname;
		private String username;
		private int gender;
		private String email;
		private String phone;
		private String image;
		private String birthday;
		private byte status;
}
