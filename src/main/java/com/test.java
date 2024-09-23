package com;

import com.services.JWTService;
import com.utils.UploadService;

public class test {
	public static void main(String[] args) {
		UploadService up = new UploadService();
		JWTService jwt = new JWTService();
		System.out.println(jwt.generateToken("tylmpc06209"));
		System.out.println(40.0/100);
	}
}
