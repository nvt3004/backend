package com;



import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class DatnStfBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatnStfBeApplication.class, args);
	}

}
