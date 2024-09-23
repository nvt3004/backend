package com.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Coupon;
import com.errors.ApiResponse;
import com.errors.FieldErrorDTO;
import com.errors.InvalidException;
import com.models.CouponCreate;
import com.models.CouponDTO;
import com.services.CouponService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

	@Autowired
	private CouponService couponService;

	@PostMapping
	public ResponseEntity<ApiResponse<?>> createCoupon(@Valid @RequestBody CouponCreate couponCreate,
			BindingResult errors) {
		List<FieldErrorDTO> fieldErrors = couponService.validateCoupon(couponCreate, errors);

		if (!fieldErrors.isEmpty()) {
			ApiResponse<List<FieldErrorDTO>> errorResponse = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
					"Validation failed", fieldErrors);
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}

		try {
			Coupon savedCoupon = couponService.saveCoupon(couponCreate);
			ApiResponse<Coupon> response = new ApiResponse<>(HttpStatus.OK.value(), "Success", savedCoupon);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			ApiResponse<String> errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"An error occurred while creating the coupon", e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping
	public ResponseEntity<ApiResponse<?>> updateCoupon(@RequestParam("id") Integer id,
			@Valid @RequestBody CouponCreate couponCreate, BindingResult errors) {

		List<FieldErrorDTO> fieldErrors = couponService.validateCoupon(couponCreate, errors);

		if (!fieldErrors.isEmpty()) {
			ApiResponse<List<FieldErrorDTO>> errorResponse = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
					"Validation failed", fieldErrors);
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		}

		try {
			Coupon updatedCoupon = couponService.updateCoupon(id, couponCreate);
			ApiResponse<Coupon> response = new ApiResponse<>(HttpStatus.OK.value(), "Success", updatedCoupon);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (InvalidException e) {
			ApiResponse<String> errorResponse = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Coupon not found",
					e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			ApiResponse<String> errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"An error occurred while updating the coupon", e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<?>> deleteCoupon(@RequestParam("id") Integer id) {
		try {
			couponService.deleteCoupon(id);
			ApiResponse<String> response = new ApiResponse<>(HttpStatus.OK.value(), "Coupon deleted successfully",
					null);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (InvalidException e) {
			ApiResponse<String> errorResponse = new ApiResponse<>(HttpStatus.CONFLICT.value(),
					"Unable to delete coupon", e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
		} catch (Exception e) {
			ApiResponse<String> errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"An error occurred while deleting the coupon", e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getAllCoupons(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
			@RequestParam(value = "discountType", required = false) String discountType) {

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<CouponDTO> couponPage = couponService.getCoupons(startDate, endDate,discountType, pageable);

			ApiResponse<Page<CouponDTO>> response = new ApiResponse<>(HttpStatus.OK.value(),
					"Coupons retrieved successfully", couponPage);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			ApiResponse<String> errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"An error occurred while retrieving the coupons", e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

}
