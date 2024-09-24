package com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.models.ReceiptCreateDTO;
import com.models.ReceiptDTO;
import com.models.ReceiptInfoDTO;
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

	@Autowired
	private UserService userService;

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getAllWarehouses(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<String> errorResponse = new ApiResponse<>();

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

		if (jwtService.isTokenExpired(token)) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage("Token expired");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			errorResponse.setErrorCode(404);
			errorResponse.setMessage("Account not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}

		if (user.getStatus() == 0) {
			errorResponse.setErrorCode(403);
			errorResponse.setMessage("Account locked - Username: " + username);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
		}

		Page<ReceiptInfoDTO> receiptDTOPage = warehouseService.getAllWarehouses(page, size);

		if (receiptDTOPage.isEmpty()) {
			ApiResponse<List<ReceiptDTO>> response = new ApiResponse<>(404, "No receipts found", null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		PageImpl<ReceiptInfoDTO> receiptDTOList = new PageImpl<>(receiptDTOPage.getContent(), receiptDTOPage.getPageable(),
				receiptDTOPage.getTotalElements());

		ApiResponse<?> response = new ApiResponse<>(200, "Success", receiptDTOList);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/receipt-detail")
	public ResponseEntity<ApiResponse<?>> getWarehouseById(@RequestParam Integer id,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<String> errorResponse = new ApiResponse<>();

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

		if (jwtService.isTokenExpired(token)) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage("Token expired");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			errorResponse.setErrorCode(404);
			errorResponse.setMessage("Account not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}

		if (user.getStatus() == 0) {
			errorResponse.setErrorCode(403);
			errorResponse.setMessage("Account locked - Username: " + username);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
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

		if (jwtService.isTokenExpired(token)) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage("Token expired");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			errorResponse.setErrorCode(404);
			errorResponse.setMessage("Account not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}

		if (user.getStatus() == 0) {
			errorResponse.setErrorCode(403);
			errorResponse.setMessage("Account locked - Username: " + username);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
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
