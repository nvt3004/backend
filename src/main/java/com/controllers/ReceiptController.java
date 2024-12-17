package com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.User;
import com.errors.ApiResponse;
import com.errors.FieldErrorDTO;
import com.errors.InvalidException;
import com.errors.UserServiceException;
import com.models.ReceiptCreateDTO;
import com.models.ReceiptDTO;
import com.models.ReceiptInfoDTO;
import com.responsedto.ReceiptResponse;
import com.services.AuthService;
import com.services.JWTService;
import com.services.ReceiptService;
import com.services.UserService;
import com.utils.ValidationUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/staff/receipt")
public class ReceiptController {

	@Autowired
	private ReceiptService warehouseService;

	@Autowired
	private AuthService authService;

	@Autowired
	private JWTService jwtService;

	@GetMapping
	@PreAuthorize("hasPermission(#userId, 'View Receipt')")
	public ResponseEntity<ApiResponse<?>> getAllWarehouses(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "keyword", required = false) String keyword,
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

		Page<ReceiptResponse> receiptDTOPage = warehouseService.getAllWarehousesStf(page, size, keyword);

		if (receiptDTOPage.isEmpty()) {
			ApiResponse<List<ReceiptDTO>> response = new ApiResponse<>(404, "No receipts found", null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		PageImpl<ReceiptResponse> receiptDTOList = new PageImpl<>(receiptDTOPage.getContent(),
				receiptDTOPage.getPageable(), receiptDTOPage.getTotalElements());

		ApiResponse<?> response = new ApiResponse<>(200, "Success", receiptDTOList);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/receipt-detail")
	@PreAuthorize("hasPermission(#userId, 'View Receipt')")
	public ResponseEntity<ApiResponse<?>> getWarehouseById(@RequestParam Integer id,
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

		ReceiptDTO receiptDTO = warehouseService.getWarehouseById(id);

		if (receiptDTO == null) {
			ApiResponse<ReceiptDTO> response = new ApiResponse<>(404, "Not found receipt", null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		ApiResponse<ReceiptDTO> response = new ApiResponse<>(200, "Success", receiptDTO);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	@PreAuthorize("hasPermission(#userId, 'Add Receipt')")
	public ResponseEntity<ApiResponse<?>> createReceipt(@Valid @RequestBody ReceiptCreateDTO receiptCreateDTO,
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

		List<FieldErrorDTO> validationErrors = ValidationUtil.validateErrors(errors);
		if (!validationErrors.isEmpty()) {
			errorResponse = new ApiResponse<>(400, "Validation failed.", validationErrors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		ApiResponse<?> response = warehouseService.createReceipt(receiptCreateDTO, user);
		return ResponseEntity.ok(response);
	}
}
