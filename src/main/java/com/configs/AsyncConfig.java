package com.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

//	@Bean(name = "taskExecutor")
//	public ThreadPoolTaskExecutor taskExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(10); // Số thread cơ bản
//		executor.setMaxPoolSize(20); // Số thread tối đa
//		executor.setQueueCapacity(500); // Kích thước hàng đợi
//		executor.setThreadNamePrefix("Async-"); // Tiền tố tên thread
//		executor.initialize();
//		return executor;
//	}
}
