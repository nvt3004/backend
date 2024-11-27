package com.services;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.entities.AttributeOptionsVersion;
import com.entities.Coupon;
import com.entities.Feedback;
import com.entities.Image;
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
import com.models.OrderQRCodeDTO;
import com.repositories.OrderDetailJPA;
import com.repositories.OrderJPA;
import com.repositories.OrderStatusJPA;
import com.repositories.ProductVersionJPA;
import com.repositories.ReceiptDetailJPA;
import com.repositories.UserJPA;
import com.utils.ExcelUtil;
import com.utils.NumberToWordsConverterUtil;
import com.utils.UploadService;

@Service
@EnableAsync
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
	private ReceiptDetailJPA receiptDetailJpa;

	@Autowired
	private OrderUtilsService orderUtilsService;

	@Autowired
	private OrderDetailService orderDetailService;

	@Autowired
	private OrderStatusService orderStatusService;

	@Autowired
	private UploadService uploadService;

	@Autowired
	private MailService mailService;

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
	    BigDecimal subTotal = orderUtilsService.calculateOrderTotal(order);
	    BigDecimal discountValue = orderUtilsService.calculateDiscountedPrice(order);
	    BigDecimal finalTotal = subTotal.add(order.getShippingFee()).subtract(discountValue);
	    finalTotal = finalTotal.max(BigDecimal.ZERO);
	    String finalTotalInWords = NumberToWordsConverterUtil.convert(finalTotal);


	    Integer couponId = Optional.ofNullable(order.getCoupon()).map(Coupon::getCouponId).orElse(null);
	    String disCount = orderUtilsService.getDiscountDescription(order);

	    boolean isDelivered = "Delivered".equalsIgnoreCase(order.getOrderStatus().getStatusName());
	    System.out.println(isDelivered + " isDelivered");
	    List<OrderByUserDTO.ProductDTO> products = new ArrayList<>();

	    for (OrderDetail orderDetail : order.getOrderDetails()) {
	        boolean hasFeedback = false;
	        for (Feedback feedback : orderDetail.getFeedbacks()) {
	            if (feedback.getOrderDetail().getOrderDetailId() != null) {
	                hasFeedback = true; 
	                break; 
	            }
	        }

	        boolean productIsDelivered = isDelivered && !hasFeedback;

	        products.add(mapToProductDTO(orderDetail, productIsDelivered));
	    }

	    return new OrderByUserDTO(
	        order.getOrderId(), 
	        order.getOrderDate(), 
	        order.getOrderStatus().getStatusName(),
	        couponId, 
	        disCount, 
	        discountValue, 
	        subTotal, 
	        order.getShippingFee(), 
	        finalTotal, 
	        finalTotalInWords,
	        products
	    );
	}

	private OrderByUserDTO.ProductDTO mapToProductDTO(OrderDetail orderDetail,
			Boolean isFeedback) {

		String variant = getVariantFromOrderDetail(orderDetail);
		Product product = orderDetail.getProductVersionBean().getProduct();
		return new OrderByUserDTO.ProductDTO(product.getProductId(), orderDetail.getOrderDetailId(), isFeedback,
				product.getProductName(), uploadService.getUrlImage(product.getProductImg()), variant,
				orderDetail.getQuantity(), orderDetail.getPrice());
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

		BigDecimal subTotal = orderUtilsService.calculateOrderTotal(order);

		BigDecimal discountValue = orderUtilsService.calculateDiscountedPrice(order);

		BigDecimal finalTotal = subTotal.add(order.getShippingFee()).subtract(discountValue);
		finalTotal = finalTotal.max(BigDecimal.ZERO);

		String finalTotalInWords = NumberToWordsConverterUtil.convert(finalTotal);

		Integer couponId = Optional.ofNullable(order.getCoupon()).map(Coupon::getCouponId).orElse(null);

		String disCount = orderUtilsService.getDiscountDescription(order);

		String statusName = order.getOrderStatus().getStatusName();
		String paymentMethodName = Optional.ofNullable(order.getPayments())
				.map(payment -> payment.getPaymentMethod().getMethodName()).orElse(null);
		Boolean isOpenOrderDetail = orderJpa.existsOrderDetailByOrderId(order.getOrderId());
		return new OrderDTO(order.getOrderId(), isOpenOrderDetail, order.getUser().getGender(), order.getAddress(),
				couponId, disCount, discountValue, subTotal, order.getShippingFee(), finalTotal, finalTotalInWords,
				order.getDeliveryDate(), order.getFullname(), order.getOrderDate(), order.getPhone(), statusName,
				paymentMethodName);
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
			CompletableFuture.runAsync(() -> sendOrderStatusUpdateEmail(order, newStatus));
		}

		return new ApiResponse<>(200, "Order status updated successfully", null);
	}

	@Async
	private void sendOrderStatusUpdateEmail(Order order, String newStatus) {
		String customerEmail = order.getUser().getEmail();
		String subject = "Your order #" + order.getOrderId() + " status has been updated";
		String htmlContent = generateOrderStatusEmailContent(order, newStatus);

		mailService.sendHtmlEmail(customerEmail, subject, htmlContent);
	}

	private String generateOrderStatusEmailContent(Order order, String newStatus) {
		BigDecimal subTotal = orderUtilsService.calculateOrderTotal(order);
		BigDecimal discountValue = orderUtilsService.calculateDiscountedPrice(order);
		BigDecimal finalTotal = subTotal.add(order.getShippingFee()).subtract(discountValue);
		finalTotal = finalTotal.max(BigDecimal.ZERO);

		return """
				<html>
				<body style='font-family: Arial, sans-serif;'>
				    <div style='max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 5px; padding: 20px;'>
				        <h2 style='color: #333;'>Dear %s,</h2>
				        <p>Your order <strong># %d</strong> status has been updated to: <strong style='color: #28a745;'>%s</strong>.</p>
				        <p>Order details:</p>
				        <table style='width: 100%%; border-collapse: collapse; margin-bottom: 20px;'>
				            <thead>
				                <tr style='background-color: #f8f9fa; text-align: left;'>
				                    <th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Product</th>
				                    <th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Quantity</th>
				                    <th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Price</th>
				                </tr>
				            </thead>
				            <tbody>
				                %s
				                <tr>
				                    <td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'><strong>Subtotal:</strong></td>
				                    <td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>%s</td>
				                </tr>
				                <tr>
				                    <td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'><strong>Shipping Fee:</strong></td>
				                    <td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>%s</td>
				                </tr>
				                <tr>
				                    <td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'><strong>Discount:</strong></td>
				                    <td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>%s</td>
				                </tr>
				                <tr style='background-color: #f8f9fa;'>
				                    <td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'><strong>Total:</strong></td>
				                    <td style='padding: 10px; border: 1px solid #ddd; color: #28a745; text-align: right;'><strong>%s</strong></td>
				                </tr>
				            </tbody>
				        </table>
				        <p style='margin-top: 20px;'>If you have any questions, please contact us at <a href='mailto:ngothai3004@gmail.com' style='color: #007bff;'>ngothai3004@gmail.com</a>.</p>
				        <p>Thank you for shopping with us!</p>
				    </div>
				</body>
				</html>
				"""
				.formatted(order.getFullname(), order.getOrderId(), newStatus, generateOrderItemsHtml(order),
						formatCurrency(subTotal), formatCurrency(order.getShippingFee()), formatCurrency(discountValue),
						formatCurrency(finalTotal));
	}

	String formatCurrency(BigDecimal amount) {
		Locale locale = new Locale("vi", "VN");
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
		String formattedAmount = currencyFormatter.format(amount);
		return formattedAmount.replace("₫", "VND");
	}

	private String generateOrderItemsHtml(Order order) {
		StringBuilder html = new StringBuilder();
		for (OrderDetail detail : order.getOrderDetails()) {
			String productName = detail.getProductVersionBean().getVersionName();
			int quantity = detail.getQuantity();
			String price = formatCurrency(detail.getPrice());
			Image image = detail.getProductVersionBean().getImage();
			String imageUrl = null;
			if (image != null) {
				imageUrl = detail.getProductVersionBean().getImage().getImageUrl();
			} else {
				imageUrl = "https://domain_thuc_te_huhu.com/default-image.jpg";
			}

			System.out.println(imageUrl + " imageUrl");

			html.append(
					"""
							<tr>
							    <td style='padding: 10px; border: 1px solid #ddd; text-align: center;'>
							        <img src='%s' alt='%s' style='max-width: 100px; max-height: 100px; border-radius: 5px; width: 'auto'; height: 'auto'; objectFit: 'contain''><br>
							        %s
							    </td>
							    <td style='padding: 10px; border: 1px solid #ddd; text-align: center;'>%d</td>
							    <td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>%s</td>
							</tr>
							"""
							.formatted(imageUrl, productName, productName, quantity, price));
		}
		return html.toString();
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

					Integer productVersionStock = receiptDetailJpa
							.getTotalQuantityForProductVersion(productVersion.getId());
					productVersionStock = (productVersionStock != null) ? productVersionStock : 0;

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

					Integer availableProductVersionStock = productVersionStock + totalQuantitySold;

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

			NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

			String[] headers = { "Order ID", "Order Date", "Customer", "Status", "Amount" };
			Object[][] data = orders.getContent().stream().map(order -> {
				String formattedOrderDate = order.getOrderDate().toInstant().atZone(ZoneId.of("UTC"))
						.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

				String formattedAmount = currencyFormatter.format(order.getFinalTotal().doubleValue());
				formattedAmount = formattedAmount.replace("₫", "VND");
				return new Object[] { order.getOrderId(), formattedOrderDate, order.getFullname(),
						order.getStatusName(), formattedAmount };
			}).toArray(Object[][]::new);

			// Tạo file Excel
			ByteArrayOutputStream outputStream = ExcelUtil.createExcelFile("Orders", headers, data);

			// Trả về resource
			return new ByteArrayResource(outputStream.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while exporting orders to Excel: " + e.getMessage(), e);
		}
	}

	public ApiResponse<Map<String, Object>> getOrder(Integer orderId) {
		List<OrderDetail> orderDetailList = orderDetailJpa.findByOrderDetailByOrderId(orderId);

		if (orderDetailList == null || orderDetailList.isEmpty()) {
			return new ApiResponse<>(404, "Order details not found", null);
		}

		OrderQRCodeDTO orderDetailDTO = orderDetailService.convertToOrderQRCode(orderDetailList);

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("orderDetail", Collections.singletonList(orderDetailDTO));

		return new ApiResponse<>(200, "Order details fetched successfully", responseMap);
	}

}
