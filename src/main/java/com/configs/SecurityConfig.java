package com.configs;

import com.errors.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.AuthDetailsService;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private AuthDetailsService ourUserDetailsService;

	@Autowired
	private JWTAuthFilter jwtAuthFilter;

	@Autowired
	@Lazy
	private CustomPermissionEvaluator customPermissionEvaluator;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
				.authorizeHttpRequests(request -> request
						.requestMatchers("/api/login", "/api/login-social", "/api/register", "/api/send",
								"/api/verify-otp", "/api/reset-password", "/api/auth/refresh", "/api/user/feedback/**",
								"api/product/**", "api/getImage/**", "/api/home/**", "/api/vnp/**", "/images/**",
								"/api/today/**,/api/orders/**")
						.permitAll().requestMatchers("/api/admin/**").hasAnyAuthority("Admin")
						.requestMatchers("/api/analytics/**").hasAnyAuthority("User", "Admin", "Staff")
						.requestMatchers("/api/staff/**", "/api/push/product").hasAnyAuthority("Staff", "Admin")
						.requestMatchers("/api/support/**").hasAnyAuthority("Support", "Admin")
						.requestMatchers("/api/user/**").hasAnyAuthority("User", "Admin", "Staff")
						.requestMatchers("/api/adminuser/**").hasAnyAuthority("Admin", "User", "Staff").anyRequest()
						.authenticated())
				.exceptionHandling(
						exception -> exception.accessDeniedHandler((request, response, accessDeniedException) -> {
							ApiResponse<String> res = new ApiResponse<>();
							res.setErrorCode(998);
							res.setMessage(accessDeniedException.getMessage());
							res.setData(null);

							response.setStatus(998); // Mã lỗi tùy chỉnh
							response.setContentType("application/json");
							response.setCharacterEncoding("UTF-8");

							ObjectMapper mapper = new ObjectMapper();
							String jsonResponse = mapper.writeValueAsString(res);
							response.getWriter().write(jsonResponse);
							response.getWriter().flush();
						}))
				.sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return httpSecurity.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(ourUserDetailsService);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
		return expressionHandler;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}