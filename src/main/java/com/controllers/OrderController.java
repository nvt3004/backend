package com.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.OrderDetail;
import com.entities.OrderStatus;
import com.errors.ApiResponse;
import com.models.OrderCreateDTO;
import com.models.OrderDTO;
import com.services.OrderDetailService;
import com.services.OrderService;
import com.services.OrderStatusService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDetailService orderDetailService;

	@Autowired
	private OrderStatusService orderStatusService;

	// Trạng thái thanh toán online mặc định là Confirmed

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getAllOrders(
			@RequestParam(value = "isAdminOrder", required = false) Boolean isAdminOrder,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {

		ApiResponse<PageImpl<OrderDTO>> response = orderService.getAllOrders(isAdminOrder, keyword, status, page, size);

		return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
	}

	@GetMapping("/statuses")
	public ResponseEntity<ApiResponse<List<OrderStatus>>> getOrderStatus() {
		List<OrderStatus> orderStatuses = orderStatusService.getAllOrderStatuses();
		ApiResponse<List<OrderStatus>> response = new ApiResponse<>(200, "Order statuses fetched successfully",
				orderStatuses);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/update-order-detail")
	public ResponseEntity<ApiResponse<?>> updateOrderDetail(@RequestParam("orderDetailId") Integer orderDetailId,
			@RequestParam("productId") Integer productId, @RequestParam("colorId") Integer colorId,
			@RequestParam("sizeId") Integer sizeId) {

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
			@RequestParam("orderDetailId") Integer orderDetailId, @RequestParam("quantity") Integer quantity) {

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
			@RequestParam("statusName") String statusName) {

		ApiResponse<?> response = orderService.updateOrderStatus(orderId, statusName);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@GetMapping("/order-details")
	public ResponseEntity<ApiResponse<?>> getOrderDetail(@RequestParam Integer orderId) {
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
			@RequestParam Integer orderDetailId) {

		if (orderId == null || orderDetailId == null) {
			ApiResponse<String> response = new ApiResponse<>(400, "Order ID and Order Detail ID are required", null);
			return ResponseEntity.badRequest().body(response);
		}

		ApiResponse<?> response = orderService.deleteOrderDetail(orderId, orderDetailId);

		return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
	}

}