package com.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Coupon;
import com.entities.User;
import com.errors.ApiResponse;
import com.errors.FieldErrorDTO;
import com.errors.InvalidException;
import com.errors.UserServiceException;
import com.models.CouponCreateDTO;
import com.models.CouponDTO;
import com.responsedto.CouponUpdateDTO;
import com.services.AuthService;
import com.services.CouponService;
import com.services.JWTService;
import com.utils.ValidationUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/staff/coupons")
public class CouponController {

	@Autowired
	private CouponService couponService;

	@Autowired
	private AuthService authService;

	@Autowired
	private JWTService jwtService;

	@PostMapping
	@PreAuthorize("hasPermission(#userId, 'Add Coupon')")
	public ResponseEntity<ApiResponse<?>> createCoupon(@Valid @RequestBody CouponCreateDTO couponCreateDTO,
			BindingResult errors, @RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		List<FieldErrorDTO> validationErrors = couponService.valiCreateDTO(couponCreateDTO, errors);
		if (!validationErrors.isEmpty()) {
			System.out.println("Có chạy vào đây");
			errorResponse = new ApiResponse<>(400, "Validation failed.", validationErrors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		try {
			Coupon savedCoupon = couponService.saveCoupon(couponCreateDTO);
			ApiResponse<Coupon> response = new ApiResponse<>(HttpStatus.OK.value(), "Tạo mã giảm giá thành công.",
					savedCoupon);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"Đã xảy ra lỗi khi tạo mã giảm giá", e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping
	@PreAuthorize("hasPermission(#userId, 'Update Coupon')")
	public ResponseEntity<ApiResponse<?>> updateCoupon(@RequestParam("id") Integer id,
			@Valid @RequestBody CouponUpdateDTO couponUpdateDTO, BindingResult errors,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		List<FieldErrorDTO> fieldErrors = couponService.valiUpdateDTO(couponUpdateDTO, errors);

		if (!fieldErrors.isEmpty()) {
			errorResponse = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Validation failed", fieldErrors);
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}

		try {
			Coupon updatedCoupon = couponService.updateCoupon(id, couponUpdateDTO);
			ApiResponse<Coupon> response = new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật mã giảm giá thành công.",
					updatedCoupon);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (InvalidException e) {
			errorResponse = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
			return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"An error occurred while updating the coupon", e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping
	@PreAuthorize("hasPermission(#userId, 'Delete Coupon')")
	public ResponseEntity<ApiResponse<?>> deleteCoupon(@RequestParam("id") Integer id,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		try {
			couponService.deleteCoupon(id);
			ApiResponse<String> response = new ApiResponse<>(HttpStatus.OK.value(),
					"Mã phiếu giảm giá đã xóa thành công.", null);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (InvalidException e) {
			errorResponse = new ApiResponse<>(HttpStatus.CONFLICT.value(), e.getMessage(), null);
			return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
		} catch (Exception e) {
			errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"An error occurred while deleting the coupon " + e.getMessage(), null);
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping
	@PreAuthorize("hasPermission(#userId, 'View Coupon')")
	public ResponseEntity<ApiResponse<?>> getAllCoupons(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
			@RequestParam(value = "discountType", required = false) String discountType,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<CouponDTO> couponPage = couponService.getCoupons(startDate, endDate, discountType, pageable);

			ApiResponse<Page<CouponDTO>> response = new ApiResponse<>(HttpStatus.OK.value(),
					"Coupons retrieved successfully", couponPage);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"An error occurred while retrieving the coupons " + e.getMessage(), null);
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}