package com.services;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.entities.AttributeOptionsVersion;
import com.entities.Coupon;
import com.entities.Feedback;
import com.entities.Order;
import com.entities.OrderDetail;
import com.entities.OrderStatus;
import com.entities.Product;
import com.entities.ProductVersion;
import com.entities.User;
import com.errors.ApiResponse;
import com.models.OrderByUserDTO;
import com.models.OrderDTO;
import com.models.OrderDetailDTO;
import com.repositories.OrderDetailJPA;
import com.repositories.OrderJPA;
import com.repositories.OrderStatusJPA;
import com.repositories.ProductVersionJPA;
import com.repositories.UserJPA;
import com.utils.UploadService;

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
	private UserJPA userJpa;

	@Autowired
	private OrderUtilsService orderUtilsService;

	@Autowired
	private OrderDetailService orderDetailService;

	@Autowired
	private OrderStatusService orderStatusService;

	@Autowired
	private UploadService uploadService;

	public ApiResponse<PageImpl<OrderDTO>> getAllOrders(String keyword, Integer statusId, Integer page, Integer size) {

		if (keyword == null) {
			keyword = "";
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<Order> ordersPage;

		if (statusId == null) {
			ordersPage = orderJpa.findOrdersByCriteria(keyword, null, pageable);
		} else {
			Optional<OrderStatus> optionalOrderStatus = orderStatusService.getOrderStatusById(statusId);
			if (optionalOrderStatus.isPresent()) {
				ordersPage = orderJpa.findOrdersByCriteria(keyword, statusId, pageable);
			} else {
				return new ApiResponse<>(404, "No order status found", null);
			}
		}

		if (ordersPage.isEmpty()) {
			return new ApiResponse<>(404, "No orders found", null);
		}

		List<OrderDTO> orderDtos = ordersPage.stream().map(this::createOrderDTO).collect(Collectors.toList());
		PageImpl<OrderDTO> resultPage = new PageImpl<>(orderDtos, pageable, ordersPage.getTotalElements());
		return new ApiResponse<>(200, "Orders fetched successfully", resultPage);
	}

	public ApiResponse<PageImpl<OrderByUserDTO>> getOrdersByUsername(String username, String keyword, Integer statusId,
			Integer page, Integer size) {

		if (keyword == null) {
			keyword = "";
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<Order> ordersPage;

		if (statusId == null) {
			ordersPage = orderJpa.findOrdersByUsername(username, keyword, null, pageable);
		} else {
			Optional<OrderStatus> optionalOrderStatus = orderStatusService.getOrderStatusById(statusId);
			if (optionalOrderStatus.isPresent()) {
				ordersPage = orderJpa.findOrdersByUsername(username, keyword, statusId, pageable);
			} else {
				return new ApiResponse<>(404, "No order status found", null);
			}
		}

		if (ordersPage.isEmpty()) {
			return new ApiResponse<>(404, "No orders found", null);
		}

		List<OrderByUserDTO> orderDtos = new ArrayList<>();
		for (Order order : ordersPage) {
			orderDtos.add(createOrderByUserDTO(order));
		}

		PageImpl<OrderByUserDTO> resultPage = new PageImpl<>(orderDtos, pageable, ordersPage.getTotalElements());
		return new ApiResponse<>(200, "Orders fetched successfully", resultPage);
	}

	private OrderByUserDTO createOrderByUserDTO(Order order) {

		BigDecimal totalPrice = orderUtilsService.calculateOrderTotal(order);
		BigDecimal discountedPrice = orderUtilsService.calculateDiscountedPrice(order);
		Integer feedBack = null;
		List<OrderByUserDTO.ProductDTO> products = new ArrayList<>();
		for (OrderDetail orderDetail : order.getOrderDetails()) {
			for (Feedback feedback : orderDetail.getFeedbacks()) {
				feedBack = feedback.getOrderDetail().getOrderDetailId();
			}
			products.add(mapToProductDTO(orderDetail, feedBack));

		}

		return new OrderByUserDTO(order.getOrderId(), order.getOrderDate(), order.getOrderStatus().getStatusName(),
				totalPrice, discountedPrice, products);
	}

	private OrderByUserDTO.ProductDTO mapToProductDTO(OrderDetail orderDetail, Integer feedBack) {

		String variant = getVariantFromOrderDetail(orderDetail);
		Product product = orderDetail.getProductVersionBean().getProduct();
		return new OrderByUserDTO.ProductDTO(product.getProductId(), product.getProductName(), feedBack,
				uploadService.getUrlImage(product.getProductImg()), variant, orderDetail.getQuantity(),
				orderDetail.getPrice());
	}

	private String getVariantFromOrderDetail(OrderDetail orderDetail) {
		String color = null;
		String size = null;

		for (AttributeOptionsVersion aov : orderDetail.getProductVersionBean().getAttributeOptionsVersions()) {
			String attributeName = aov.getAttributeOption().getAttribute().getAttributeName();
			String attributeValue = aov.getAttributeOption().getAttributeValue();
			if ("Color".equalsIgnoreCase(attributeName)) {
				color = attributeValue;
			} else if ("Size".equalsIgnoreCase(attributeName)) {
				size = attributeValue;
			}
		}

		if (color != null && size != null) {
			return color + ", " + size;
		} else if (color != null) {
			return color;
		} else if (size != null) {
			return size;
		}

		return "";
	}

	private OrderDTO createOrderDTO(Order order) {
		BigDecimal total = orderUtilsService.calculateOrderTotal(order);

		String statusName = order.getOrderStatus().getStatusName();
		Integer couponId = Optional.ofNullable(order.getCoupon()).map(Coupon::getCouponId).orElse(null);

		String paymentMethodName = Optional.ofNullable(order.getPayments())
				.map(payment -> payment.getPaymentMethod().getMethodName()).orElse(null);

		return new OrderDTO(order.getOrderId(), order.getAddress(), couponId,
				orderUtilsService.calculateDiscountedPrice(order), order.getShippingFee(), order.getDeliveryDate(),
				order.getFullname(), order.getOrderDate(), order.getPhone(), statusName, total, paymentMethodName);
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
			return new ApiResponse<>(400, "Status is required.", null);
		}

		Optional<OrderStatus> newOrderStatus = orderStatusJpa.findById(statusId);
		if (!newOrderStatus.isPresent()) {
			return new ApiResponse<>(400, "The provided status does not exist.", null);
		}

		Optional<Order> updatedOrder = orderJpa.findById(orderId);
		if (!updatedOrder.isPresent()) {
			return new ApiResponse<>(404, "The order with the provided ID does not exist.", null);
		}

		Order order = updatedOrder.get();
		String currentStatus = order.getOrderStatus().getStatusName();
		String newStatus = newOrderStatus.get().getStatusName();

		if (!isValidStatusTransition(currentStatus, newStatus)) {
			return new ApiResponse<>(400, "Invalid status transition from " + currentStatus + " to " + newStatus, null);
		}

		if (isOrderStatusChanged(order, newStatus)) {
			if ("Processed".equalsIgnoreCase(newStatus)) {
				List<String> insufficientStockMessages = checkProductVersionsStock(order.getOrderDetails());
				if (!insufficientStockMessages.isEmpty()) {
					return new ApiResponse<>(400, String.join(", ", insufficientStockMessages), null);
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

	private boolean isValidStatusTransition(String currentStatus, String newStatus) {
		switch (currentStatus.toLowerCase()) {
		case "pending":
			return "processed".equalsIgnoreCase(newStatus) || "cancelled".equalsIgnoreCase(newStatus);
		case "processed":
			return "shipped".equalsIgnoreCase(newStatus);
		case "shipped":
			return "delivered".equalsIgnoreCase(newStatus);
		case "delivered":
			return false;
		case "cancelled":
			return false;
		case "temp":
			return "cancelled".equalsIgnoreCase(newStatus);
		default:
			return false;
		}
	}

	private List<String> checkProductVersionsStock(List<OrderDetail> orderDetailList) {
		List<String> insufficientStockMessages = new ArrayList<>();
		for (OrderDetail orderDetail : orderDetailList) {
			if (!orderDetail.getOrder().getOrderStatus().getStatusName().equalsIgnoreCase("Processed")) {
				Integer orderDetailRequestedQuantity = orderDetail.getQuantity();
				ProductVersion productVersion = productVersionJpa.findById(orderDetail.getProductVersionBean().getId())
						.orElse(null);

				if (productVersion != null) {
					Integer productVersionStock = productVersion.getQuantity();
					Integer processedOrderQuantity = productVersionJpa
							.getTotalQuantityByProductVersionInProcessedOrders(productVersion.getId());
					Integer cancelledOrderQuantity = productVersionJpa
							.getTotalQuantityByProductVersionInCancelledOrders(productVersion.getId());
					Integer shippedOrderQuantity = productVersionJpa
							.getTotalQuantityByProductVersionInShippedOrders(productVersion.getId());
					Integer deliveredOrderQuantity = productVersionJpa
							.getTotalQuantityByProductVersionInDeliveredOrders(productVersion.getId());

					processedOrderQuantity = (processedOrderQuantity != null) ? processedOrderQuantity : 0;
					cancelledOrderQuantity = (cancelledOrderQuantity != null) ? cancelledOrderQuantity : 0;
					shippedOrderQuantity = (shippedOrderQuantity != null) ? shippedOrderQuantity : 0;
					deliveredOrderQuantity = (deliveredOrderQuantity != null) ? deliveredOrderQuantity : 0;

					Integer totalQuantitySold = processedOrderQuantity + shippedOrderQuantity + deliveredOrderQuantity;
					Integer totalQuantityReturnedToStock = cancelledOrderQuantity;

					Integer availableProductVersionStock = productVersionStock + totalQuantityReturnedToStock
							- totalQuantitySold;

					if (availableProductVersionStock < orderDetailRequestedQuantity) {
						insufficientStockMessages.add("Product version ID " + productVersion.getId()
								+ ": Available stock " + availableProductVersionStock + ", Requested quantity: "
								+ orderDetailRequestedQuantity);
					}
				} else {
					insufficientStockMessages
							.add("Product version ID " + orderDetail.getProductVersionBean().getId() + " not found.");
				}
			}
		}
		return insufficientStockMessages;
	}

	public ApiResponse<?> deleteOrderDetail(Integer orderId, Integer orderDetailId) {
		try {
			Optional<Order> optionalOrder = orderJpa.findById(orderId);
			if (optionalOrder.isEmpty()) {
				return new ApiResponse<>(404, "Order not found", null);
			}

			Order order = optionalOrder.get();
			String status = order.getOrderStatus().getStatusName();

			List<String> restrictedStatuses = Arrays.asList("Processed", "Shipped", "Delivered", "Cancelled");

			if (restrictedStatuses.contains(status)) {
				return new ApiResponse<>(400, "Cannot delete order details for an order with status " + status, null);
			}

			int rowsAffected = orderDetailJpa.deleteOrderDetailsByOrderDetailId(orderDetailId);

			if (rowsAffected != 0) {
				if (orderJpa.existsByOrderDetail(orderId)) {
					try {
						Optional<OrderStatus> cancelledStatusOpt = Optional
								.ofNullable(orderStatusService.findByName("Cancelled"));

						if (cancelledStatusOpt.isPresent()) {
							OrderStatus cancelledStatus = cancelledStatusOpt.get();
							order.setOrderStatus(cancelledStatus);
							orderJpa.save(order);
						} else {
							return new ApiResponse<>(404, "Cancelled status not found.", null);
						}
					} catch (Exception e) {
						return new ApiResponse<>(500, "Failed to find 'Cancelled' status: " + e.getMessage(), null);
					}
				}

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

	public ApiResponse<?> cancelOrder(Integer orderId, User currentUser) {
		Optional<Order> updatedOrder = orderJpa.findById(orderId);
		if (updatedOrder.isEmpty()) {
			return new ApiResponse<>(404, "The order with the provided ID does not exist.", null);
		}

		Order order = updatedOrder.get();

		if (!isUserAuthorized(order, currentUser)) {
			return new ApiResponse<>(403, "You do not have permission to cancel this order.", null);
		}

		String currentStatus = order.getOrderStatus().getStatusName();

		if (!isCancellable(currentStatus)) {
			return new ApiResponse<>(400, "The order cannot be cancelled from the current status: " + currentStatus,
					null);
		}

		Optional<OrderStatus> cancelledStatus = orderStatusJpa.findByStatusNameIgnoreCase("Cancelled");
		if (cancelledStatus.isEmpty()) {
			return new ApiResponse<>(500, "Cancellation status is not configured in the system.", null);
		}

		order.setOrderStatus(cancelledStatus.get());
		orderJpa.save(order);

		BigDecimal totalPriceOrder = orderUtilsService.calculateOrderTotal(order);

		BigDecimal discount = BigDecimal.ZERO;

		if (order.getDisPrice() != null && order.getDisPrice().compareTo(BigDecimal.ZERO) > 0) {
			discount = order.getDisPrice();
		} else if (order.getDisPercent() != null && order.getDisPercent().compareTo(BigDecimal.ZERO) > 0) {
			discount = totalPriceOrder.multiply(order.getDisPercent().divide(BigDecimal.valueOf(100)));
		}

		currentUser.setBalance(currentUser.getBalance().add(totalPriceOrder.subtract(discount)));
		userJpa.save(currentUser);

		return new ApiResponse<>(200, "Order cancelled successfully", null);
	}

	private boolean isUserAuthorized(Order order, User currentUser) {
		return order.getUser().getUserId() == currentUser.getUserId();
	}

	private boolean isCancellable(String currentStatus) {
		return "pending".equalsIgnoreCase(currentStatus);
	}

	public ByteArrayResource exportOrdersToExcel(Boolean isAdminOrder, String keyword, Integer statusId, int page,
			int size) {
		try {
			ApiResponse<PageImpl<OrderDTO>> ordersResponse = this.getAllOrders(keyword, statusId, page, size);

			if (ordersResponse.getErrorCode() != 200) {
				throw new RuntimeException(ordersResponse.getMessage());
			}

			PageImpl<OrderDTO> orders = ordersResponse.getData();

			// Tạo file Excel
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Orders");

			// Tạo các style cho cột header
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.WHITE.getIndex());
			headerStyle.setFont(headerFont);
			headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			// Tạo các style cho dữ liệu
			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.CENTER); // Căn giữa theo chiều ngang
			dataStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa theo chiều dọc

			// Tạo header row
			Row headerRow = sheet.createRow(0);
			headerRow.createCell(0).setCellValue("Order ID");
			headerRow.createCell(1).setCellValue("Order Date");
			headerRow.createCell(2).setCellValue("Customer");
			headerRow.createCell(3).setCellValue("Status");
			headerRow.createCell(4).setCellValue("Amount");

			for (int i = 0; i < 5; i++) {
				headerRow.getCell(i).setCellStyle(headerStyle);
			}

			// Đặt độ rộng cho các cột
			sheet.setColumnWidth(0, 6000);
			sheet.setColumnWidth(1, 8000);
			sheet.setColumnWidth(2, 12000);
			sheet.setColumnWidth(3, 6000);
			sheet.setColumnWidth(4, 6000);

			int rowNum = 1;
			for (OrderDTO order : orders.getContent()) {
				Optional<Order> orderEntityOpt = orderJpa.findById(order.getOrderId());
				if (orderEntityOpt.isPresent()) {
					Order orderEntity = orderEntityOpt.get();
					Row row = sheet.createRow(rowNum++);

					// Thêm dữ liệu vào các cột và áp dụng style cho dữ liệu
					Cell cell0 = row.createCell(0);
					cell0.setCellValue(order.getOrderId());
					cell0.setCellStyle(dataStyle);

					Cell cell1 = row.createCell(1);
					cell1.setCellValue(order.getOrderDate().toString());
					cell1.setCellStyle(dataStyle);

					Cell cell2 = row.createCell(2);
					cell2.setCellValue(order.getFullname());
					cell2.setCellStyle(dataStyle);

					Cell cell3 = row.createCell(3);
					cell3.setCellValue(order.getStatusName());
					cell3.setCellStyle(dataStyle);

					Cell cell4 = row.createCell(4);
					cell4.setCellValue(orderUtilsService.calculateOrderTotal(orderEntity).doubleValue());
					cell4.setCellStyle(dataStyle);
				}
			}

			// Tạo file Excel và trả về
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			workbook.close();

			// Chuyển đổi output stream thành resource để trả về
			return new ByteArrayResource(outputStream.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("An error occurred while exporting orders to Excel: " + e.getMessage(), e);
		}
	}

}
