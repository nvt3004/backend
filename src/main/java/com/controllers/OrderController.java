package com.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.OrderDetail;
import com.entities.OrderStatus;
import com.entities.User;
import com.errors.ApiResponse;
import com.errors.InvalidException;
import com.errors.UserServiceException;
import com.models.OrderByUserDTO;
import com.models.OrderDTO;
import com.models.OrderStatusDTO;
import com.services.AuthService;
import com.services.JWTService;
import com.services.OrderDetailService;
import com.services.OrderService;
import com.services.OrderStatusService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.apache.poi.ss.usermodel.Cell;

@RestController
@RequestMapping("/api")
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

	@GetMapping("/staff/orders")
	@PreAuthorize("hasPermission(#userId, 'STAFF_ORDER_VIEW_ALL')")
	public ResponseEntity<ApiResponse<?>> getAllOrders(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
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

		if (page < 0 || size < 1) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(400,
					"Invalid page number or size. Page must be greater than or equal to 0 and size must be greater than or equal to 1.",
					null));
		}

		try {
			ApiResponse<PageImpl<OrderDTO>> successResponse = orderService.getAllOrders(keyword, statusId,
					page, size);
			return ResponseEntity.ok(successResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An error occurred while retrieving orders " + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/user/orders/username")
//	@PreAuthorize("hasPermission(#userId, 'USER_ORDER_VIEW_SELF')")
	public ResponseEntity<ApiResponse<?>> getOrdersByUsername(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {

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

		if (page < 0) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid page number. It must be greater than or equal to 0.", null));
		}

		if (size < 1) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid size. It must be greater than or equal to 1.", null));
		}

		try {
			ApiResponse<PageImpl<OrderByUserDTO>> successResponse = orderService.getOrdersByUsername(user.getUsername(),
					keyword, statusId, page, size);
			return ResponseEntity.ok(successResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An error occurred while retrieving orders " + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/staff/orders/statuses")
	@PreAuthorize("hasPermission(#userId, 'STAFF_ORDER_STATUS_VIEW_ALL')")
	public ResponseEntity<ApiResponse<?>> getAllOrderStatus(
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

		List<OrderStatusDTO> orderStatusDTOList = orderStatusService.getAllOrderStatusDTOs();
		ApiResponse<List<OrderStatusDTO>> response = new ApiResponse<>(200, "Order statuses fetched successfully",
				orderStatusDTOList);

		return ResponseEntity.ok(response);

	}

	@PutMapping("/staff/orders/update-order-detail")
	@PreAuthorize("hasPermission(#userid, 'STAFF_ORDER_DETAIL_UPDATE')")
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

		if (orderDetailId == null || productId == null || colorId == null || sizeId == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}

		ApiResponse<OrderDetail> response = orderDetailService.updateOrderDetail(orderDetailId, productId, colorId,
				sizeId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@PutMapping("/staff/orders/update-order-detail-quantity")
	@PreAuthorize("hasPermission(#userid, 'STAFF_ORDER_DETAIL_UPDATE')")
	public ResponseEntity<ApiResponse<?>> updateOrderDetailQuantity(
			@RequestParam("orderDetailId") Integer orderDetailId, @RequestParam("quantity") Integer quantity,
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

		if (orderDetailId == null || quantity == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}

		ApiResponse<OrderDetail> validationResponse = orderDetailService
				.validateAndUpdateOrderDetailQuantity(orderDetailId, quantity);

		if (validationResponse.getErrorCode() != 200) {
			return ResponseEntity.status(HttpStatus.valueOf(validationResponse.getErrorCode()))
					.body(validationResponse);
		}
		
		return ResponseEntity
				.ok(new ApiResponse<>(200, "Order detail quantity updated successfully", null));
	}

	@PutMapping("/staff/orders/update-status")
	@PreAuthorize("hasPermission(#userid, 'STAFF_ORDER_STATUS_UPDATE')")
	public ResponseEntity<ApiResponse<?>> updateOrderStatus(@RequestParam("orderId") Integer orderId,
			@RequestParam("statusId") Integer statusId, @RequestHeader("Authorization") Optional<String> authHeader) {

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

		if (orderId == null || statusId == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}

		errorResponse = orderService.updateOrderStatus(orderId, statusId);

		if (errorResponse.getErrorCode() == 200) {
			return ResponseEntity.ok(errorResponse);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(errorResponse.getErrorCode())).body(errorResponse);
		}
	}

	@GetMapping("/staff/orders/{orderId}")
	@PreAuthorize("hasPermission(#userid, 'STAFF_ORDER_DETAIL_VIEW')")
	public ResponseEntity<ApiResponse<?>> getOrderDetail(@PathVariable Integer orderId,
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

	@DeleteMapping("/staff/orders/remove-orderdetail")
//	@PreAuthorize("hasPermission(#userid, 'STAFF_ORDER_DETAIL_REMOVE')")
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

		if (orderId == null || orderDetailId == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}

		ApiResponse<?> response = orderService.deleteOrderDetail(orderId, orderDetailId);

		return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
	}

	@PutMapping("/user/orders/cancel-order")
//	@PreAuthorize("hasPermission(#userid, 'USER_ORDER_CANCEL')")
	public ResponseEntity<ApiResponse<?>> cancelOrder(@RequestParam("orderId") Integer orderId,
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

		if (orderId == null) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Order ID is required.", null));
		}

		errorResponse = orderService.cancelOrder(orderId, user);

		if (errorResponse.getErrorCode() == 200) {
			return ResponseEntity.ok(errorResponse);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(errorResponse.getErrorCode())).body(errorResponse);
		}
	}
	
	@GetMapping("/staff/orders/export")
//	@PreAuthorize("hasPermission(#userId, 'STAFF_ORDER_VIEW_ALL')")
	public ResponseEntity<?> exportOrdersToExcel(
	        @RequestParam(value = "isAdminOrder", required = false) Boolean isAdminOrder,
	        @RequestParam(value = "keyword", required = false) String keyword,
	        @RequestParam(value = "statusId", required = false) Integer statusId,
	        @RequestParam(value = "page", defaultValue = "0") int page,
	        @RequestParam(value = "size", defaultValue = "5") int size) {
		 ApiResponse<ByteArrayResource> apiResponse = new ApiResponse<>();
	    try {

	    	ByteArrayResource file = orderService.exportOrdersToExcel(isAdminOrder, keyword, statusId, page, size);

	    	return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx")
	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                .body(file);
	    } catch (Exception e) {
	    	 apiResponse.setErrorCode(500);
	         apiResponse.setMessage(e.getMessage());
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
	    }
	}	

}