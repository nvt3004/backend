package com;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling 
public class DatnStfBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatnStfBeApplication.class, args);
	}

}
