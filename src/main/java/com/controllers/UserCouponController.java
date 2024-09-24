package com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Coupon;
import com.entities.User;
import com.errors.ResponseAPI;
import com.repositories.UserCouponJPA;
import com.responsedto.ProfileResponse;
import com.responsedto.UserCouponResponse;
import com.services.AuthService;
import com.services.CouponService;
import com.services.JWTService;
import com.services.UserCouponService;
import com.services.UserService;

@RestController
@RequestMapping("api/user")
public class UserCouponController {
	
	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserCouponService userCouponService;
	
	@Autowired
	UserCouponJPA userCouponJPA;
	
	
	@GetMapping("/get-all-coupon")
	public ResponseEntity<ResponseAPI<List<UserCouponResponse>>> getAllCouponByUser(@RequestHeader("Authorization") Optional<String> authHeader)
	{	
		ResponseAPI<List<UserCouponResponse>> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
		
		
		List<UserCouponResponse> list = userCouponService.getAllCouponByUser(user.getUserId());
		
		response.setCode(200);
		response.setMessage("Success");
		response.setData(list);
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/coupon/{code}")
	public ResponseEntity<ResponseAPI<UserCouponResponse>> getCoupon(@RequestHeader("Authorization") Optional<String> authHeader,
			@PathVariable("code") String code)
	{	
		ResponseAPI<UserCouponResponse> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
		
		
		Coupon coupon = userCouponJPA.findCouponByCode(code);
		
		if(coupon == null) {
			response.setCode(404);
			response.setMessage("Coupon not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		UserCouponResponse couponResponse = new UserCouponResponse();
		couponResponse.setId(coupon.getCouponId());
		couponResponse.setCouponCode(coupon.getCouponCode());
		couponResponse.setPercent(coupon.getDisPercent());
		couponResponse.setPrice(coupon.getDisPrice());
		couponResponse.setStartDate(coupon.getStartDate());
		couponResponse.setEndDate(coupon.getEndDate());
		
		response.setCode(200);
		response.setMessage("Success");
		response.setData(couponResponse);
		
		return ResponseEntity.ok(response);
	}
}
