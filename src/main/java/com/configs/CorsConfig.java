// package com.configs;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
// public class CorsConfig {

//     @Bean
//     public WebMvcConfigurer webMvcConfigurer() {
//         return new WebMvcConfigurer() {
//             @Override
//             public void addCorsMappings(CorsRegistry registry) {
//                 registry.addMapping("/**")
//                         .allowedOriginPatterns("http://localhost:3000",
//                                 "https://stepstothefuture.store",
//                                 "http://103.72.97.191:3000",
//                                 "http://103.72.97.191:5000",
//                                 "http://103.72.97.191:8080",
//                                 "https://api.stepstothefuture.store",
//                                 "https://py.stepstothefuture.store")
//                         .allowedMethods("GET", "POST", "PUT", "DELETE")
//                         .allowedHeaders("*")
//                         .allowCredentials(true);
//             }
//         };
//     }
// }
