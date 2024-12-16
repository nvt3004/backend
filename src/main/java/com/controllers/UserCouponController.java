package com.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.repositories.CouponJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Coupon;
import com.entities.User;
import com.entities.UserCoupon;
import com.errors.ResponseAPI;
import com.models.CouponDTO;
import com.models.UserCouponModel;
import com.models.UserModel;
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

	@Autowired
	CouponService couponService;
    @Autowired
    private CouponJPA couponJPA;

	@GetMapping("/get-all-coupon-home")
	public ResponseEntity<ResponseAPI<List<CouponDTO>>> getAllCouponHomeByUser(
			@RequestHeader("Authorization") Optional<String> authHeader) {
		ResponseAPI<List<CouponDTO>> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(403);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(403).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(999);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(403).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(403).body(response);
		}

		List<CouponDTO> list = couponService.getCouponsHome(user.getUserId());

		response.setCode(200);
		response.setMessage("Success");
		response.setData(list);

		return ResponseEntity.ok(response);
	}
	
	
	@PostMapping("/get-coupon/add")
	public ResponseEntity<ResponseAPI<Boolean>> addUserCoupon(
			@RequestHeader("Authorization") Optional<String> authHeader,@RequestBody UserCouponModel userCouponModel) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(403);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(403).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(999);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(403).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(403).body(response);
		}

		if(userCouponModel.getCode() == null || userCouponModel.getCode().isBlank() || userCouponModel.getCode().isEmpty()) {
			response.setCode(999);
			response.setMessage("Mã giảm giá không hợp lệ");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		Coupon coupon = couponService.getCouponByCode(userCouponModel.getCode());

		if (coupon == null) {
			response.setCode(999);
			response.setMessage("Mã giảm giá không tồn tại!");

			return ResponseEntity.status(999).body(response);
		}

		if (coupon.getQuantity() <= 0) {
			response.setCode(999);
			response.setMessage("Mã giảm giá đã đã hết do số lượng có hạn!");

			return ResponseEntity.status(999).body(response);
		}


		UserCoupon userCouponEntity = new UserCoupon();
		userCouponEntity.setCoupon(coupon);
		userCouponEntity.setUser(user);
		userCouponEntity.setRetrievalDate(LocalDateTime.now());
		userCouponEntity.setStatus(true);

		coupon.setQuantity(coupon.getQuantity()-1);
		couponJPA.save(coupon);
		userCouponService.createUserCoupon(userCouponEntity);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(true);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/get-all-coupon")
	public ResponseEntity<ResponseAPI<List<UserCouponResponse>>> getAllCouponByUser(
			@RequestHeader("Authorization") Optional<String> authHeader) {
		ResponseAPI<List<UserCouponResponse>> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(403);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(403).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(999);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(403).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(403).body(response);
		}

		List<UserCouponResponse> list = userCouponService.getAllCouponByUser(user.getUserId());

		response.setCode(200);
		response.setMessage("Success");
		response.setData(list);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/coupon")
	public ResponseEntity<ResponseAPI<UserCouponResponse>> getCoupon(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam(value = "code", defaultValue = "") String code) {
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

		if (code.isEmpty() || code.isBlank()) {
			response.setCode(400);
			response.setMessage("Mã giảm giá không hợp lệ");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		Coupon coupon = couponJPA.getCouponByCode(code);

		if (coupon == null) {
			response.setCode(404);
			response.setMessage("Mã giảm gia không tồn tại!");

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
