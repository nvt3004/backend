package com.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.OrderDetail;
import com.entities.OrderStatus;
import com.entities.User;
import com.errors.ApiResponse;
import com.models.OrderDTO;
import com.services.AuthService;
import com.services.JWTService;
import com.services.OrderDetailService;
import com.services.OrderService;
import com.services.OrderStatusService;
import com.services.UserService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDetailService orderDetailService;

	@Autowired
	private OrderStatusService orderStatusService;

	@Autowired
	private AuthService authService;

	@Autowired
	private JWTService jwtService;

	@Autowired
	private UserService userService;

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getAllOrders(
			@RequestParam(value = "isAdminOrder", required = false) Boolean isAdminOrder,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "page", defaultValue = "0") int page,
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

		if (page < 0) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid page number. It must be greater than or equal to 0.", null));
		}

		if (size < 1) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid size. It must be greater than or equal to 1.", null));
		}
		try {
			ApiResponse<PageImpl<OrderDTO>> successResponse = orderService.getAllOrders(isAdminOrder, keyword, status,
					page, size);
			return ResponseEntity.ok(successResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An error occurred while retrieving orders");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/statuses")
	public ResponseEntity<ApiResponse<?>> getOrderStatus(@RequestHeader("Authorization") Optional<String> authHeader) {

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
			errorResponse.setMessage("Account locked");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
		}

		List<OrderStatus> orderStatuses = orderStatusService.getAllOrderStatuses();
		ApiResponse<List<OrderStatus>> response = new ApiResponse<>(200, "Order statuses fetched successfully",
				orderStatuses);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/update-order-detail")
	public ResponseEntity<ApiResponse<?>> updateOrderDetail(@RequestParam("orderDetailId") Integer orderDetailId,
			@RequestParam("productId") Integer productId, @RequestParam("colorId") Integer colorId,
			@RequestParam("sizeId") Integer sizeId, @RequestHeader("Authorization") Optional<String> authHeader) {

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
			errorResponse.setMessage("Account locked");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
		}
		if (orderDetailId == null || productId == null || colorId == null || sizeId == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid parameters", "Some required parameters are missing."));
		}

		ApiResponse<OrderDetail> response = orderDetailService.updateOrderDetail(orderDetailId, productId, colorId,
				sizeId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@PutMapping("/update-order-detail-quantity")
	public ResponseEntity<ApiResponse<?>> updateOrderDetailQuantity(
			@RequestParam("orderDetailId") Integer orderDetailId, @RequestParam("quantity") Integer quantity,
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
			errorResponse.setMessage("Account locked");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
		}

		if (orderDetailId == null || quantity == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid parameters", "Some required parameters are missing."));
		}

		ApiResponse<OrderDetail> validationResponse = orderDetailService.validateAndPrepareUpdate(orderDetailId,
				quantity);

		if (validationResponse.getErrorCode() != 200) {
			return ResponseEntity.status(HttpStatus.valueOf(validationResponse.getErrorCode()))
					.body(validationResponse);
		}

		OrderDetail orderDetail = validationResponse.getData();
		OrderDetail updatedOrderDetail = orderDetailService.updateOrderDetailQuantity(orderDetail, quantity);

		return ResponseEntity
				.ok(new ApiResponse<>(200, "Order detail quantity updated successfully", updatedOrderDetail));
	}

	@PutMapping("/update-status")
	public ResponseEntity<ApiResponse<?>> updateOrderStatus(@RequestParam("orderId") int orderId,
			@RequestParam("statusId") Integer statusId,
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
			errorResponse.setMessage("Account locked");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
		}

		ApiResponse<?> response = orderService.updateOrderStatus(orderId, statusId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@GetMapping("/order-details")
	public ResponseEntity<ApiResponse<?>> getOrderDetail(@RequestParam Integer orderId,
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
			errorResponse.setMessage("Account locked");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
		}
		if (orderId == null) {
			ApiResponse<String> response = new ApiResponse<>(400, "Order ID is required", null);
			return ResponseEntity.badRequest().body(response);
		}

		ApiResponse<Map<String, Object>> response = orderService.getOrderDetails(orderId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@DeleteMapping("/remove-orderdetail")
	public ResponseEntity<ApiResponse<?>> deleteOrderDetailsByOrderDetailId(@RequestParam Integer orderId,
			@RequestParam Integer orderDetailId, @RequestHeader("Authorization") Optional<String> authHeader) {

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
			errorResponse.setMessage("Account locked");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
		}

		if (orderId == null || orderDetailId == null) {
			ApiResponse<String> response = new ApiResponse<>(400, "Order ID and Order Detail ID are required", null);
			return ResponseEntity.badRequest().body(response);
		}

		ApiResponse<?> response = orderService.deleteOrderDetail(orderId, orderDetailId);

		return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
	}

}