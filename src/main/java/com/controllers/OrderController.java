package com.controllers;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.OrderDetail;
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
import com.utils.UploadService;

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

	@Autowired
	private UploadService uploadService;

	@GetMapping("/staff/orders")
	@PreAuthorize("hasPermission(#userId, 'View Order')")
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
			ApiResponse<PageImpl<OrderDTO>> successResponse = orderService.getAllOrders(keyword, statusId, page, size);
			return ResponseEntity.ok(successResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An error occurred while retrieving orders " + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/user/orders/username")
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
	@PreAuthorize("hasPermission(#userId, 'View Order')")
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
	
	@GetMapping("/user/orders/statuses")
	
	public ResponseEntity<ApiResponse<?>> getAllOrderStatus2(
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
	@PreAuthorize("hasPermission(#userid, 'Update Order')")
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

		ApiResponse<OrderDetail> response = orderDetailService.updateOrderDetail(orderDetailId, user, productId,
				colorId, sizeId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@PutMapping("/staff/orders/update-order-detail-quantity")
	@PreAuthorize("hasPermission(#userid, 'Update Order')")
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
				.validateAndUpdateOrderDetailQuantity(orderDetailId, user, quantity);

		if (validationResponse.getErrorCode() != 200) {
			return ResponseEntity.status(HttpStatus.valueOf(validationResponse.getErrorCode()))
					.body(validationResponse);
		}

		return ResponseEntity.ok(new ApiResponse<>(200, "Order detail quantity updated successfully", null));
	}

	@PutMapping("/staff/orders/update-status")
	@PreAuthorize("hasPermission(#userid, 'Update Order')")
	public ResponseEntity<ApiResponse<?>> updateOrderStatus(@RequestParam("orderId") Integer orderId,
			@RequestBody(required = false) Map<String, String> payload, @RequestParam("statusId") Integer statusId,
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

		if (orderId == null || statusId == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}
		String reason = payload != null ? payload.get("reason") : null;
		errorResponse = orderService.updateOrderStatus(orderId, statusId, user, reason);

		if (errorResponse.getErrorCode() == 200) {
			return ResponseEntity.ok(errorResponse);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(errorResponse.getErrorCode())).body(errorResponse);
		}
	}

	@GetMapping("/staff/orders/{orderId}")
	@PreAuthorize("hasPermission(#userid, 'View Order')")
	public ResponseEntity<ApiResponse<?>> getOrderDetail(@PathVariable Integer orderId,
			@RequestHeader("Authorization") Optional<String> authHeader) {
		System.out.println("Chạy Vô OrderDetails");
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
	@PreAuthorize("hasPermission(#userid, 'Delete Order')")
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
	//@PreAuthorize("hasPermission(#userid, 'Delete Order')")
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

	@PutMapping("/user/orders/confirm-received")
	public ResponseEntity<ApiResponse<?>> confirmOrderReceived(@RequestParam("orderId") Integer orderId,
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

		ApiResponse<?> response = orderService.confirmOrderReceived(orderId, user);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@GetMapping("/staff/orders/export")
	public ResponseEntity<?> exportOrdersToExcel(
			@RequestParam(value = "isAdminOrder", required = false) Boolean isAdminOrder,
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
		ApiResponse<ByteArrayResource> apiResponse = new ApiResponse<>();
		try {

			ByteArrayResource file = orderService.exportOrdersToExcel(isAdminOrder, keyword, statusId, page, size);

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx")
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
		} catch (Exception e) {
			apiResponse.setErrorCode(500);
			apiResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
		}
	}

	@GetMapping("/orders/{orderId}")
	public ResponseEntity<ApiResponse<?>> getOrderDetailByOrderId(@PathVariable Integer orderId) {
		if (orderId == null) {
			ApiResponse<String> response = new ApiResponse<>(400, "Order ID is required", null);
			return ResponseEntity.badRequest().body(response);
		}

		ApiResponse<Map<String, Object>> response = orderService.getOrder(orderId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

//	@PostMapping("/staff/orders/export")
//	public void exportInvoiceAsPdf(@RequestParam("orderId") Integer orderId, HttpServletResponse response) {
//		try {
//			ApiResponse<ByteArrayOutputStream> pdfStream = orderService.generateInvoicePdf(orderId);
//
//			ApiResponse<BufferedImage> image = orderService.convertPdfToImage(pdfStream.getData());
//
//			response.setContentType("image/png");
//			ImageIO.write(image.getData(), "png", response.getOutputStream());
//		} catch (IllegalArgumentException e) {
//			response.setStatus(400);
//			try {
//				response.getWriter().write(e.getMessage());
//			} catch (IOException ioException) {
//				ioException.printStackTrace();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setStatus(500);
//			try {
//				response.getWriter().write("Error generating PDF: " + e.getMessage());
//			} catch (IOException ioException) {
//				ioException.printStackTrace();
//			}
//		}
//	}

	@PostMapping("/staff/orders/export")
	public ResponseEntity<?> exportInvoiceAsPdf(@RequestParam("orderId") Integer orderId,
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
			// 1. Tạo PDF từ Order ID
			ApiResponse<ByteArrayOutputStream> pdfStreamResponse = orderService.generateInvoicePdf(orderId);
			ByteArrayOutputStream pdfStream = pdfStreamResponse.getData();
			byte[] pdfBytes = pdfStream.toByteArray();

			// 2. Trả về PDF dưới dạng file
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_PDF);
			headers.setContentDispositionFormData("attachment", "invoice.pdf");
			headers.setContentLength(pdfBytes.length);

			return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			ApiResponse<String> response = new ApiResponse<>(400, e.getMessage(), null);
			return ResponseEntity.badRequest().body(response);
		} catch (Exception e) {
			e.printStackTrace();
			ApiResponse<String> response = new ApiResponse<>(500, "Error generating PDF: " + e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PutMapping("/staff/orders/refund/{orderId}")
	public ResponseEntity<ApiResponse<?>> refundOrder(@PathVariable Integer orderId,
			@RequestHeader("Authorization") Optional<String> authHeader) throws Exception {
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
		return orderService.refundOrder(orderId, user);

	}

}