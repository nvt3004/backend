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

	public ApiResponse<PageImpl<OrderDTO>> getAllOrders(Boolean isAdminOrder, String keyword, String status, int page,
			int size) {

		if (keyword == null) {
			keyword = "";
		}

		if (status == null) {
			status = "";
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
			return new ApiResponse<>(400, "Status is required.",null);
		}

		Optional<OrderStatus> newOrderStatus = orderStatusJpa.findById(statusId);
		if (newOrderStatus.isEmpty()) {
			return new ApiResponse<>(400, "The provided status does not exist.",null);
		}

		Optional<Order> updatedOrder = orderJpa.findById(orderId);
		if (updatedOrder.isEmpty()) {
			return new ApiResponse<>(404, "The order with the provided ID does not exist.", null);
		}

		Order order = updatedOrder.get();
		if (isOrderStatusChanged(order, newOrderStatus.get().getStatusName())) {
			if ("Processed".equalsIgnoreCase(newOrderStatus.get().getStatusName())) {
				Boolean isStockSufficient = updateProductVersionsForOrder(order.getOrderDetails());
				if (!isStockSufficient) {
					return new ApiResponse<>(400, "Not enough stock available for one or more products.",
							null);
				}
			} 
			order.setOrderStatus(newOrderStatus.get());
			orderJpa.save(order);
		}

		return new ApiResponse<>(200, "Order status updated successfully", null);
	}

	private boolean isOrderStatusChanged(Order order, String statusName) {
		return !statusName.equalsIgnoreCase(order.getOrderStatus().getStatusName());
	}

	private Boolean updateProductVersionsForOrder(List<OrderDetail> orderDetailList) {
	    for (OrderDetail orderDetail : orderDetailList) {
	        if (!orderDetail.getOrder().getOrderStatus().getStatusName().equalsIgnoreCase("Processed")) {
	            Integer orderDetailProductQuantity = orderDetail.getQuantity();
	            ProductVersion productVersion = productVersionJpa
	                    .findById(orderDetail.getProductVersionBean().getId())
	                    .orElse(null);

	            if (productVersion != null) {
	                Integer productVersionQuantity = productVersion.getQuantity();
	                Integer totalQuantityProcessedOrders = productVersionJpa
	                        .getTotalQuantityByProductVersionInProcessedOrders(productVersion.getId());
	                Integer totalQuantityCancelledOrders = productVersionJpa
	                        .getTotalQuantityByProductVersionInCancelledOrders(productVersion.getId());

	                totalQuantityProcessedOrders = (totalQuantityProcessedOrders != null) ? totalQuantityProcessedOrders : 0;
	                totalQuantityCancelledOrders = (totalQuantityCancelledOrders != null) ? totalQuantityCancelledOrders : 0;

	                Integer totalQuantityProductVersionInOrder = totalQuantityProcessedOrders + totalQuantityCancelledOrders;
	                Integer inventoryProductVersion = productVersionQuantity - totalQuantityProductVersionInOrder;
	                System.out.println(inventoryProductVersion + " inventoryProductVersion");
	                if (inventoryProductVersion < orderDetailProductQuantity) {
	                    return false;  
	                }
	            } else {
	                return false;  
	            }
	        }
	    }
	    return true; 
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
