package com.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.entities.Address;
import com.entities.Coupon;
import com.entities.Order;
import com.entities.OrderDetail;
import com.entities.OrderStatus;
import com.entities.Payment;
import com.entities.PaymentMethod;
import com.entities.ProductVersion;
import com.entities.User;
import com.errors.ApiResponse;
import com.models.OrderCreateDTO;
import com.models.OrderDTO;
import com.models.OrderDetailCreateDTO;
import com.models.OrderDetailDTO;
import com.repositories.AddressJPA;
import com.repositories.CouponJPA;
import com.repositories.OrderDetailJPA;
import com.repositories.OrderJPA;
import com.repositories.OrderStatusJPA;
import com.repositories.PaymentJPA;
import com.repositories.PaymentMethodJPA;
import com.repositories.ProductVersionJPA;
import com.repositories.UserJPA;

@Service
public class OrderService {
	@Autowired
	private OrderJPA orderJpa;

	@Autowired
	private OrderStatusJPA orderStatusJpa;

	@Autowired
	private ProductVersionJPA productVersionJpa;

	@Autowired
	private OrderDetailJPA orderDetailJpa;

	@Autowired
	private OrderUtilsService orderUtilsService;

	@Autowired
	private OrderDetailService orderDetailService;

	public ApiResponse<PageImpl<OrderDTO>> getAllOrders(Boolean isAdminOrder, String keyword,
				String status, int page, int size) {
			
			if (keyword == null) {
				keyword = "";
			}

			if (status == null) {
				status = "";
			}

			if (page < 0) {
				return new ApiResponse<>(400, "Invalid page number. It must be greater than or equal to 0.", null);
			}

			if (size < 1) {
				return new ApiResponse<>(400, "Invalid size. It must be greater than or equal to 1.", null);
			}

			Pageable pageable = PageRequest.of(page, size);
			Page<Order> ordersPage = orderJpa.findOrdersByCriteria(isAdminOrder, keyword, status, pageable);

			if (ordersPage.isEmpty()) {
				return new ApiResponse<>(404, "No orders found", null);
			}

			List<OrderDTO> orderDtos = ordersPage.stream().map(this::createOrderDTO).collect(Collectors.toList());
			PageImpl<OrderDTO> resultPage = new PageImpl<>(orderDtos, pageable, ordersPage.getTotalElements());
			return new ApiResponse<>(200, "Orders fetched successfully", resultPage);
		}


	private OrderDTO createOrderDTO(Order order) {
		BigDecimal total = orderUtilsService.calculateOrderTotal(order);
		String paymentMethod = orderUtilsService.getPaymentMethod(order);

		String statusName = order.getOrderStatus().getStatusName();
		Integer couponId = (order.getCoupon() != null) ? order.getCoupon().getCouponId() : null;

		return new OrderDTO(order.getOrderId(), order.getAddress(), couponId, order.getDeliveryDate(),
				order.getFullname(), order.getOrderDate(), order.getPhone(), statusName, total, paymentMethod);
	}

	public ApiResponse<Map<String, Object>> getOrderDetails(Integer orderId) {
		List<OrderDetail> orderDetailList = orderDetailJpa.findByOrderDetailByOrderId(orderId);

		if (orderDetailList == null || orderDetailList.isEmpty()) {
			return new ApiResponse<>(404, "Order details not found", null);
		}

		OrderDetailDTO orderDetailDTO = orderDetailService.convertToOrderDetailDTO(orderDetailList);

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("orderDetail", Collections.singletonList(orderDetailDTO));

		return new ApiResponse<>(200, "Order details fetched successfully", responseMap);
	}

	public ApiResponse<?> updateOrderStatus(Integer orderId, Integer statusId) {
		if (statusId == null) {
			return new ApiResponse<>(400, "Invalid status", "Status is required.");
		}

		Optional<OrderStatus> newOrderStatus = orderStatusJpa.findById(statusId);
		System.out.println(newOrderStatus.get().getStatusName() + " StatusName");
		if (newOrderStatus.isEmpty()) {
			return new ApiResponse<>(400, "Invalid status", "The provided status name does not exist.");
		}

		Optional<Order> updatedOrder = orderJpa.findById(orderId);
		if (updatedOrder.isEmpty()) {
			return new ApiResponse<>(404, "Order not found", "The order with the provided ID does not exist.");
		}

		Order order = updatedOrder.get();
		if (isOrderStatusChanged(order, newOrderStatus.get().getStatusName())) {
			order.setOrderStatus(newOrderStatus.get());
			orderJpa.save(order);
			if ("Processing".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
				updateProductVersionsForOrder(order.getOrderDetails());
			} else if ("Cancelled".equalsIgnoreCase(order.getOrderStatus().getStatusName())) {
				revertProductVersionsForCancelledOrder(order.getOrderDetails());
			}
		}

		return new ApiResponse<>(200, "Order status updated successfully", null);
	}

	private boolean isOrderStatusChanged(Order order, String statusName) {
		return !statusName.equalsIgnoreCase(order.getOrderStatus().getStatusName());
	}

	private void updateProductVersionsForOrder(List<OrderDetail> orderDetailList) {
		for (OrderDetail orderDetail : orderDetailList) {
			if (!orderDetail.getOrder().getOrderStatus().getStatusName().equalsIgnoreCase("Processing")) {
				Integer orderDetailProductQuantity = orderDetail.getQuantity();
				List<ProductVersion> productVersionList = productVersionJpa
						.findProductVersionById(orderDetail.getProductVersionBean().getId());
				for (ProductVersion productVersion : productVersionList) {
					Integer productVersionQuantity = productVersion.getQuantity();
					Integer newProductVersionQuantity = productVersionQuantity - orderDetailProductQuantity;
					productVersion.setQuantity(newProductVersionQuantity);
					productVersionJpa.save(productVersion);
				}
			}
		}
	}

	private void revertProductVersionsForCancelledOrder(List<OrderDetail> orderDetailList) {
		for (OrderDetail orderDetail : orderDetailList) {
			Integer orderDetailProductQuantity = orderDetail.getQuantity();
			List<ProductVersion> productVersionList = productVersionJpa
					.findProductVersionById(orderDetail.getProductVersionBean().getId());
			for (ProductVersion productVersion : productVersionList) {
				Integer productVersionQuantity = productVersion.getQuantity();
				Integer newProductVersionQuantity = productVersionQuantity + orderDetailProductQuantity;
				productVersion.setQuantity(newProductVersionQuantity);
				productVersionJpa.save(productVersion);
			}
		}
	}

	public ApiResponse<?> deleteOrderDetail(Integer orderId, Integer orderDetailId) {
		try {
			Optional<Order> optionalOrder = orderJpa.findById(orderId);
			if (optionalOrder.isEmpty()) {
				return new ApiResponse<>(404, "Order not found", null);
			}

			Order order = optionalOrder.get();
			String status = order.getOrderStatus().getStatusName();

			List<String> restrictedStatuses = Arrays.asList("Processing", "Shipped", "Delivered", "Cancelled");

			if (restrictedStatuses.contains(status)) {
				return new ApiResponse<>(400, "Cannot delete order details for an order with status " + status, null);
			}

			int rowsAffected = orderDetailJpa.deleteOrderDetailsByOrderDetailId(orderDetailId);

			if (rowsAffected != 0) {
				return new ApiResponse<>(200, "Product deleted successfully.", null);
			} else {
				return new ApiResponse<>(404, "OrderDetail with ID " + orderDetailId + " not found.", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<>(500, "An error occurred while deleting the OrderDetail. Please try again.", null);
		}
	}
	
	public Order createOrderCart(Order order) {
		return orderJpa.save(order);
	}
	
	public boolean deleteOrderById(int id) {
		try {
			orderJpa.deleteById(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Order getOrderById(int id) {
		return orderJpa.findById(id).orElse(null);
	}

	public BigDecimal getAmountByOrderId(int id) {
		Order order = orderJpa.findById(id).get();

		BigDecimal total = BigDecimal.ZERO;

		for (OrderDetail detail : order.getOrderDetails()) {
			total = total.add(detail.getPrice());
		}

		return total;
	}
}
