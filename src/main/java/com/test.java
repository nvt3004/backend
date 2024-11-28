package com;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.services.JWTService;
import com.utils.UploadService;

public class test {
	public static void main(String[] args) {
		BigDecimal tine = new BigDecimal("400000");
		BigDecimal phanTram = new BigDecimal("5").divide(new BigDecimal("100"));
		
		BigDecimal result = tine.multiply(BigDecimal.ONE.subtract(phanTram));
		
		System.out.println(result);
	}
}
