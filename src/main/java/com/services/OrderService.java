package com.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.entities.AttributeOptionsVersion;
import com.entities.Coupon;
import com.entities.Feedback;
import com.entities.Image;
import com.entities.Order;
import com.entities.OrderDetail;
import com.entities.OrderStatus;
import com.entities.Payment;
import com.entities.Product;
import com.entities.ProductVersion;
import com.entities.User;
import com.errors.ApiResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import com.itextpdf.text.pdf.qrcode.WriterException;
import com.models.OrderByUserDTO;
import com.models.OrderDTO;
import com.models.OrderDetailDTO;
import com.models.OrderDetailProductDetailsDTO;
import com.models.OrderQRCodeDTO;
import com.repositories.OrderDetailJPA;
import com.repositories.OrderJPA;
import com.repositories.OrderStatusJPA;
import com.repositories.PaymentJPA;
import com.repositories.ProductVersionJPA;
import com.repositories.UserJPA;
import com.utils.DateUtils;
import com.utils.ExcelUtil;
import com.utils.FormarCurrencyUtil;
import com.utils.NumberToWordsConverterUtil;
import com.utils.UploadService;

import jakarta.mail.MessagingException;

@Service
public class OrderService {
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

	@Autowired
	private OrderJPA orderJpa;

	@Autowired
	private OrderDetailJPA orderDetailJpa;

	@Autowired
	private OrderStatusJPA orderStatusJpa;

	@Autowired
	private ProductVersionJPA productVersionJpa;

	@Autowired
	private UserJPA userJpa;

	@Autowired
	private PaymentJPA paymentJpa;

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
				return new ApiResponse<>(404, "Không tìm thấy trạng thái đơn hàng", null);
			}
		}

		if (ordersPage.isEmpty()) {
			return new ApiResponse<>(404, "Không tìm thấy đơn hàng nào", null);
		}

		List<OrderDTO> orderDtos = ordersPage.stream().map(this::createOrderDTO).collect(Collectors.toList());
		PageImpl<OrderDTO> resultPage = new PageImpl<>(orderDtos, pageable, ordersPage.getTotalElements());
		return new ApiResponse<>(200, "Lấy danh sách đơn hàng thành công", resultPage);
	}

	public ApiResponse<PageImpl<OrderByUserDTO>> getOrdersByUsername(String username, String keyword, Integer statusId,
			Integer page, Integer size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Order> ordersPage;
		if (statusId == null) {
			ordersPage = orderJpa.findOrdersByUsername(username, keyword, null, pageable);
		} else {
			Optional<OrderStatus> optionalOrderStatus = orderStatusService.getOrderStatusById(statusId);
			if (optionalOrderStatus.isPresent()) {
				ordersPage = orderJpa.findOrdersByUsername(username, keyword, statusId, pageable);
			} else {
				return new ApiResponse<>(404, "Không tìm thấy trạng thái đơn hàng", null);
			}
		}

		if (ordersPage.isEmpty()) {
			return new ApiResponse<>(404, "Không tìm thấy đơn hàng nào", null);
		}

		List<OrderByUserDTO> orderDtos = new ArrayList<>();
		for (Order order : ordersPage) {
			orderDtos.add(createOrderByUserDTO(order));
		}

		PageImpl<OrderByUserDTO> resultPage = new PageImpl<>(orderDtos, pageable, ordersPage.getTotalElements());
		return new ApiResponse<>(200, "Lấy danh sách đơn hàng thành công", resultPage);
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

		return new OrderByUserDTO(order.getOrderId(), order.getOrderDate(),order.getDeliveryDate(), order.getOrderStatus().getStatusName(),
				couponId, disCount, discountValue, subTotal, order.getShippingFee(), finalTotal, finalTotalInWords,
				products);
	}
	
	private OrderByUserDTO.ProductDTO mapToProductDTO(OrderDetail orderDetail, Boolean isFeedback) {

		String variant = getVariantFromOrderDetail(orderDetail);
		Product product = orderDetail.getProductVersionBean().getProduct();
		return new OrderByUserDTO.ProductDTO(product.getProductId(), orderDetail.getOrderDetailId(), isFeedback,
				product.getProductName(), uploadService.getUrlImage(product.getProductImg()), variant,
				orderDetail.getQuantity(), orderDetail.getPrice());
	}

	private String getVariantFromOrderDetail(OrderDetail orderDetail) {
	    List<String> attributes = new ArrayList<>();

	    for (AttributeOptionsVersion aov : orderDetail.getProductVersionBean().getAttributeOptionsVersions()) {
	        String attributeName = aov.getAttributeOption().getAttribute().getAttributeName();
	        String attributeValue = aov.getAttributeOption().getAttributeValue();            

	        attributes.add(attributeName + ": " + attributeValue);
	    }

	    return String.join(", ", attributes);
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
		BigDecimal amount = Optional.ofNullable(order.getPayments()).map(payment -> payment.getAmount())
				.orElse(BigDecimal.ZERO);
		Boolean isOpenOrderDetail = orderJpa.existsOrderDetailByOrderId(order.getOrderId());
		Integer lastUpdatedById = Optional.ofNullable(order.getLastUpdatedBy()).map(User::getUserId).orElse(null);
		String lastUpdatedByName = Optional.ofNullable(order.getLastUpdatedBy()).map(User::getFullName).orElse(null);
		Date lastUpdatedDate = Optional.ofNullable(order.getLastUpdatedDate()).orElse(null);

		return new OrderDTO(order.getOrderId(), lastUpdatedById, lastUpdatedByName, lastUpdatedDate, isOpenOrderDetail,
				order.getUser().getGender(), order.getAddress(), couponId, disCount, discountValue, subTotal,
				order.getShippingFee(), finalTotal, finalTotalInWords, order.getDeliveryDate(), order.getFullname(),
				order.getOrderDate(), order.getPhone(), statusName, paymentMethodName, amount);
	}

	public ApiResponse<Map<String, Object>> getOrderDetails(Integer orderId) {
		List<OrderDetail> orderDetailList = orderDetailJpa.findByOrderDetailByOrderId(orderId);

		if (orderDetailList == null || orderDetailList.isEmpty()) {
			return new ApiResponse<>(404, "Không tìm thấy chi tiết đơn hàng", null);
		}

		OrderDetailDTO orderDetailDTO = orderDetailService.convertToOrderDetailDTO(orderDetailList);

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("orderDetail", Collections.singletonList(orderDetailDTO));

		return new ApiResponse<>(200, "Lấy chi tiết đơn hàng thành công", responseMap);
	}
	
	public ApiResponse<?> updateOrderStatus(Integer orderId, Integer statusId, User currentUser, String reason) {
	    if (statusId == null) {
	        return new ApiResponse<>(400, "Trạng thái là bắt buộc.", null);
	    }

	    Optional<OrderStatus> newOrderStatus = orderStatusJpa.findById(statusId);
	    if (!newOrderStatus.isPresent()) {
	        return new ApiResponse<>(400, "Trạng thái được cung cấp không tồn tại.", null);
	    }

	    Optional<Order> updatedOrder = orderJpa.findById(orderId);
	    if (!updatedOrder.isPresent()) {
	        return new ApiResponse<>(404, "Không tìm thấy đơn hàng với ID được cung cấp.", null);
	    }

	    Order order = updatedOrder.get();
	    String currentStatus = order.getOrderStatus().getStatusName();
	    String newStatus = newOrderStatus.get().getStatusName();

	    if ("Delivered".equalsIgnoreCase(currentStatus)) {
	        String currentStatusVietnamese = OrderUtilsService.translateStatus(currentStatus);
	        String newStatusVietnamese = OrderUtilsService.translateStatus(newStatus);

	        return new ApiResponse<>(400,
	            "Không thể chuyển đổi trạng thái từ " + currentStatusVietnamese + " sang " + newStatusVietnamese, null);
	    }

	    if (!isValidStatusTransition(currentStatus, newStatus)) {
	        String currentStatusVietnamese = OrderUtilsService.translateStatus(currentStatus);
	        String newStatusVietnamese = OrderUtilsService.translateStatus(newStatus);

	        return new ApiResponse<>(400,
	            "Chuyển đổi trạng thái không hợp lệ từ " + currentStatusVietnamese + " sang " + newStatusVietnamese, null);
	    }

	    if (isOrderStatusChanged(order, newStatus)) {
	        if ("Processed".equalsIgnoreCase(newStatus)) {
	            List<String> insufficientStockMessages = checkProductVersionsStock(order.getOrderDetails());
	            if (!insufficientStockMessages.isEmpty()) {
	                return new ApiResponse<>(400, String.join(", ", insufficientStockMessages), null);
	            }
	        }
	        if ("Cancelled".equalsIgnoreCase(newStatus)) {
	            if (order.getPayments().getPaymentMethod().getMethodName().equalsIgnoreCase("Chuyển khoản")) {
	                order.getPayments().setAmount(BigDecimal.valueOf(-1));
	            }
	        }
	        order.setOrderStatus(newOrderStatus.get());
	        order.setLastUpdatedBy(currentUser);
	        order.setLastUpdatedDate(new Date());
	        orderJpa.save(order);
	        if (!newStatus.equalsIgnoreCase("Shipped") && !newStatus.equalsIgnoreCase("Waitingforconfirmation")) {
	            sendOrderStatusUpdateEmail(order, newStatus, reason);
	        }
	        if("Waitingforconfirmation".equalsIgnoreCase(newStatus)) {
	        	sendConfirmationEmail(order);
	        }

	    }

	    return new ApiResponse<>(200, "Cập nhật trạng thái đơn hàng thành công", null);
	}
	
	private void sendConfirmationEmail(Order order) {
	    String to = order.getUser().getEmail(); 
	    String subject = "Yêu cầu xác nhận đơn hàng";
	    
	    String body = String.format(
	        "<html>" +
	            "<head>" +
	                "<style>" +
	                    "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
	                    ".highlight { color: #007bff; font-weight: bold; }" +
	                    ".footer { margin-top: 20px; font-size: 0.9em; color: #555; }" +
	                    ".content { margin: 10px 0; }" +
	                    ".container { width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }" +
	                    ".header { background-color: #007bff; color: white; text-align: center; padding: 15px; border-radius: 8px 8px 0 0; }" +
	                    ".button { display: inline-block; background-color: #28a745; color: white; padding: 10px 20px; border-radius: 5px; text-decoration: none; font-weight: bold; margin-top: 20px; }" +
	                    ".button:hover { background-color: #218838; }" +
	                "</style>" +
	            "</head>" +
	            "<body>" +
	                "<div class='container'>" +
	                    "<div class='header'>" +
	                        "<h2>Yêu cầu xác nhận đơn hàng</h2>" +
	                    "</div>" +
	                    "<div class='content'>" +
	                        "<p>Chào <span class='highlight'>%s</span>,</p>" +
	                        "<p>Đơn hàng của bạn đã sẵn sàng để xác nhận. Vui lòng kiểm tra và xác nhận đơn hàng của bạn.</p>" +
	                        "<p><strong>Thông tin đơn hàng:</strong></p>" +
	                        "<p><strong>Mã đơn hàng:</strong> %s</p>" +
	                        "<p><strong>Ngày giao hàng dự kiến:</strong> %s</p>" +
//	                        "<p>Vui lòng truy cập vào trang quản lý đơn hàng để xác nhận.</p>" +
//	                        "<a href='http://localhost:3000/account' class='button'>Xác nhận đơn hàng</a>" +
	                    "</div>" +
	                    "<div class='footer'>" +
	                        "<p>Cảm ơn bạn đã mua hàng tại chúng tôi!</p>" +
	                        "<p><small>Đây là email tự động, vui lòng không trả lời email này.</small></p>" +
	                    "</div>" +
	                "</div>" +
	            "</body>" +
	        "</html>", 
	        order.getFullname(), 
	        order.getOrderId(), 
	        DateUtils.formatDate( order.getDeliveryDate())
	    );
	    CompletableFuture.runAsync(() ->  mailService.sendHtmlEmail(to,subject,body));
	}

	private void sendOrderStatusUpdateEmail(Order order, String newStatus, String reason) {
		String customerEmail = order.getUser().getEmail();
		String subject = "Đơn hàng #" + order.getOrderId() + " " + getStatusMessage(newStatus);
		String htmlContent = generateOrderStatusEmailContent(order, newStatus, reason);

		CompletableFuture.runAsync(() -> mailService.sendHtmlEmail(customerEmail, subject, htmlContent));
	}

	private String getStatusMessage(String status) {
		switch (status.toLowerCase()) {
		case "pending":
			return "đang chờ xác nhận.";
		case "processed":
			return "đã xác nhận.";
		case "shipped":
			return "đã vận chuyển.";
		case "delivered":
			return "đã giao thành công.";
		case "cancelled":
			return "đã bị hủy.";
		default:
			return "đã cập nhật.";
		}
	}

	private String generateOrderStatusEmailContent(Order order, String newStatus, String reason) {
		BigDecimal subTotal = orderUtilsService.calculateOrderTotal(order);
		BigDecimal discountValue = orderUtilsService.calculateDiscountedPrice(order);
		BigDecimal finalTotal = subTotal.add(order.getShippingFee()).subtract(discountValue);
		finalTotal = finalTotal.max(BigDecimal.ZERO);

		String statusMessage = getStatusMessage(newStatus);
		String cancelReasonMessage = "";

		if ("Cancelled".equalsIgnoreCase(newStatus) && reason != null) {
			cancelReasonMessage = "<p><strong>Lý do hủy:</strong> " + reason + "</p>";
		}

		return """
				 <html>
				 <body style='font-family: Arial, sans-serif;'>
				 <style>
				    body { font-family: Arial, sans-serif; line-height: 1.6; }
				    .highlight { color: #007bff; font-weight: bold;}
				    .footer { margin-top: 20px; font-size: 0.9em; color: #555; }
				    .content { margin: 10px 0; }
				</style>
				     <div style='max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 5px; padding: 20px;'>
				         <h2 style='color: #333;'>Kính chào %s,</h2>
				      <p>Đơn hàng <strong># %d</strong> của bạn %s</p>
				         %s
				         <p>Thông tin chi tiết đơn hàng:</p>
				         <table style='width: 100%%; border-collapse: collapse; margin-bottom: 20px;'>
				             <thead>
				                 <tr style='background-color: #f8f9fa; text-align: left;'>
				                     <th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Sản phẩm</th>
				                     <th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Số lượng</th>
				                     <th style='padding: 10px; border: 1px solid #ddd; text-align: center;'>Giá</th>
				                 </tr>
				             </thead>
				             <tbody>
				                 %s
				                 <tr>
				                     <td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'><strong>Tổng phụ:</strong></td>
				                     <td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>%s</td>
				                 </tr>
				                 <tr>
				                     <td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'><strong>Phí vận chuyển:</strong></td>
				                     <td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>%s</td>
				                 </tr>
				                 <tr>
				                     <td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'><strong>Giảm giá:</strong></td>
				                     <td style='padding: 10px; border: 1px solid #ddd; text-align: right;'>%s</td>
				                 </tr>
				                 <tr style='background-color: #f8f9fa;'>
				                     <td colspan='2' style='padding: 10px; border: 1px solid #ddd; text-align: right;'><strong>Tổng cộng:</strong></td>
				                     <td style='padding: 10px; border: 1px solid #ddd; color: #28a745; text-align: right;'><strong>%s</strong></td>
				                 </tr>
				             </tbody>
				         </table>
				               <p>Vui lòng kiểm tra lại thông tin đơn hàng của bạn.</p>
				            <p>
				    Nếu bạn có bất kỳ câu hỏi nào hay cần sự hỗ trợ thêm, xin vui lòng liên hệ với chúng tôi qua thông tin dưới đây:
				</p>

				<p class="content">
				    - Email: <a href='mailto:ngothai3004@gmail.com' style='color: #007bff;'>ngothai3004@gmail.com</a><br>
				    - Điện thoại: <span class="highlight">(+84) 939 658 044</span>
				</p>
				<p>Xin cảm ơn bạn đã mua sắm cùng chúng tôi!</p>
				<p>Trân trọng,<br>Công ty TNHH Step To The Future</p>
				<p class="footer">
				    <small>Đây là email tự động, vui lòng không trả lời email này.</small>
				</p>

				     </div>
				 </body>
				 </html>
				 """
				.formatted(order.getFullname(), order.getOrderId(), statusMessage, cancelReasonMessage,
						generateOrderItemsHtml(order), FormarCurrencyUtil.formatCurrency(subTotal),
						FormarCurrencyUtil.formatCurrency(order.getShippingFee()),
						FormarCurrencyUtil.formatCurrency(discountValue),
						FormarCurrencyUtil.formatCurrency(finalTotal));
	}

	private String generateOrderItemsHtml(Order order) {
		StringBuilder html = new StringBuilder();
		for (OrderDetail detail : order.getOrderDetails()) {
			String productName = detail.getProductVersionBean().getVersionName();
			int quantity = detail.getQuantity();
			String price = FormarCurrencyUtil.formatCurrency(detail.getPrice());
			Image image = detail.getProductVersionBean().getImage();
			String imageUrl = null;
			if (image != null) {
				imageUrl = detail.getProductVersionBean().getImage().getImageUrl();
			} else {
				imageUrl = "https://domain_thuc_te_huhu.com/default-image.jpg";
			}

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
			return "waitingforconfirmation".equalsIgnoreCase(newStatus);
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
	                productVersionStock = (productVersionStock != null) ? productVersionStock : 0;

	                Integer processedOrderQuantity = productVersionJpa
	                        .getTotalQuantityByProductVersionInProcessedOrders(productVersion.getId());
	                Integer cancelledOrderQuantity = productVersionJpa
	                        .getTotalQuantityByProductVersionInCancelledOrders(productVersion.getId());
	                Integer shippedOrderQuantity = productVersionJpa
	                        .getTotalQuantityByProductVersionInShippedOrders(productVersion.getId());
	                Integer deliveredOrderQuantity = productVersionJpa
	                        .getTotalQuantityByProductVersionInDeliveredOrders(productVersion.getId());
	                Integer waitingForConfirmationQuantity = productVersionJpa
	                        .getTotalQuantityByProductVersionInWaitingForConfirmationOrders(productVersion.getId());

	                processedOrderQuantity = (processedOrderQuantity != null) ? processedOrderQuantity : 0;
	                cancelledOrderQuantity = (cancelledOrderQuantity != null) ? cancelledOrderQuantity : 0;
	                shippedOrderQuantity = (shippedOrderQuantity != null) ? shippedOrderQuantity : 0;
	                deliveredOrderQuantity = (deliveredOrderQuantity != null) ? deliveredOrderQuantity : 0;
	                waitingForConfirmationQuantity = (waitingForConfirmationQuantity != null) ? waitingForConfirmationQuantity : 0;

	                Integer totalQuantitySold = processedOrderQuantity + shippedOrderQuantity + deliveredOrderQuantity + waitingForConfirmationQuantity;

	                Integer availableProductVersionStock = productVersionStock + totalQuantitySold;

	                if (availableProductVersionStock < orderDetailRequestedQuantity) {
	                    insufficientStockMessages.add("Không đủ tồn kho cho phiên bản sản phẩm ID "
	                            + productVersion.getId() + ": Tồn kho hiện tại " + availableProductVersionStock
	                            + ", Số lượng yêu cầu: " + orderDetailRequestedQuantity);
	                }

	            } else {
	                insufficientStockMessages.add("Không tìm thấy phiên bản sản phẩm ID "
	                        + orderDetail.getProductVersionBean().getId() + ".");
	            }
	        }
	    }
	    return insufficientStockMessages;
	}


	public ApiResponse<?> deleteOrderDetail(Integer orderId, Integer orderDetailId) {
		try {
			Optional<Order> optionalOrder = orderJpa.findById(orderId);
			if (optionalOrder.isEmpty()) {
				return new ApiResponse<>(404, "Không tìm thấy đơn hàng", null);
			}

			Order order = optionalOrder.get();
			String status = order.getOrderStatus().getStatusName();

			List<String> restrictedStatuses = Arrays.asList("Processed", "Shipped", "Delivered", "Cancelled");

			if (restrictedStatuses.contains(status)) {
				return new ApiResponse<>(400, "Không thể xóa chi tiết đơn hàng với trạng thái " + status, null);
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
							return new ApiResponse<>(404, "Không tìm thấy trạng thái 'Đã hủy'.", null);
						}
					} catch (Exception e) {
						return new ApiResponse<>(500, "Không thể tìm thấy trạng thái 'Đã hủy': " + e.getMessage(),
								null);
					}
				}
				Optional<OrderDetail> orderDetail = orderDetailJpa.findById(orderDetailId);
				sendEmailNotification(order, orderDetail.get());
				return new ApiResponse<>(200, "Xóa sản phẩm thành công.", null);
			} else {
				return new ApiResponse<>(404, "Không tìm thấy chi tiết đơn hàng với ID " + orderDetailId, null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse<>(500, "Đã xảy ra lỗi khi xóa chi tiết đơn hàng. Vui lòng thử lại.", null);
		}
	}

	private void sendEmailNotification(Order order, OrderDetail orderDetail) throws Exception {
		String userEmail = order.getUser().getEmail();

		CompletableFuture.runAsync(() -> {
			String subject = "Thông báo xóa sản phẩm khỏi đơn hàng #" + order.getOrderId();

			String message = """
					 <!DOCTYPE html>
					 <html>
					     <head>
					         <style>
					    body { font-family: Arial, sans-serif; line-height: 1.6; }
					    .highlight { color: #007bff; font-weight: bold;}
					    .footer { margin-top: 20px; font-size: 0.9em; color: #555; }
					    .content { margin: 10px 0; }
					</style>
					     </head>
					     <body>
					         <p>Kính chào <strong>%s</strong>,</p>
					         <p>Sản phẩm <span class="highlight">"%s"</span> với số lượng <span class="highlight">%d</span> trong đơn hàng <span class="highlight">#%d</span> đã được xóa thành công.</p>
					            <p>Vui lòng kiểm tra lại thông tin đơn hàng của bạn.</p>
							<p>
							    Nếu bạn không yêu cầu thay đổi này hoặc có bất kỳ câu hỏi hay cần sự hỗ trợ thêm, xin vui lòng liên hệ với chúng tôi qua thông tin dưới đây:
							</p>

					<p class="content">
					    - Email: <a href='mailto:ngothai3004@gmail.com' style='color: #007bff;'>ngothai3004@gmail.com</a><br>
					    - Điện thoại: <span class="highlight">(+84) 939 658 044</span>
					</p>
					 <p>Xin cảm ơn bạn đã mua sắm cùng chúng tôi!</p>
					<p>Trân trọng,<br>Công ty TNHH Step To The Future</p>
					<p class="footer">
					    <small>Đây là email tự động, vui lòng không trả lời email này.</small>
					</p>
					     </body>
					 </html>
					 """
					.formatted(order.getFullname(), orderDetail.getProductVersionBean().getProduct().getProductName(),
							orderDetail.getQuantity(), order.getOrderId());

			mailService.sendHtmlEmail(userEmail, subject, message);
		});
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
			return new ApiResponse<>(404, "Không tìm thấy đơn hàng với ID đã cung cấp.", null);
		}

		Order order = updatedOrder.get();

		if (!isUserAuthorized(order, currentUser)) {
			return new ApiResponse<>(403, "Bạn không có quyền hủy đơn hàng này.", null);
		}

		String currentStatus = order.getOrderStatus().getStatusName();

		if (!isCancellable(currentStatus)) {
			return new ApiResponse<>(400, "Đơn hàng không thể hủy với trạng thái hiện tại: " + currentStatus, null);
		}

		Optional<OrderStatus> cancelledStatus = orderStatusJpa.findByStatusNameIgnoreCase("Cancelled");
		if (cancelledStatus.isEmpty()) {
			return new ApiResponse<>(500, "Trạng thái hủy đơn hàng chưa được cấu hình trong hệ thống.", null);
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

		return new ApiResponse<>(200, "Đơn hàng đã được hủy thành công", null);
	}

	private boolean isUserAuthorized(Order order, User currentUser) {
		return order.getUser().getUserId() == currentUser.getUserId();
	}

	private boolean isCancellable(String currentStatus) {
		return "pending".equalsIgnoreCase(currentStatus);
	}
	
	public ApiResponse<?> confirmOrderReceived(Integer orderId, User currentUser) {
	    Optional<Order> updatedOrder = orderJpa.findById(orderId);
	    if (updatedOrder.isEmpty()) {
	        return new ApiResponse<>(404, "Không tìm thấy đơn hàng với ID được cung cấp.", null);
	    }

	    Order order = updatedOrder.get();

	    if (!isUserAuthorized(order, currentUser)) {
	        return new ApiResponse<>(403, "Bạn không có quyền xác nhận đơn hàng này.", null);
	    }

	    String currentStatus = order.getOrderStatus().getStatusName();
	    if (!"Waitingforconfirmation".equalsIgnoreCase(currentStatus)) {
	        return new ApiResponse<>(400, "Chỉ có thể xác nhận đơn hàng ở trạng thái Chờ xác nhận.", null);
	    }

	    Optional<OrderStatus> deliveredStatus = orderStatusJpa.findByStatusNameIgnoreCase("Delivered");
	    if (deliveredStatus.isEmpty()) {
	        return new ApiResponse<>(500, "Trạng thái đã nhận hàng chưa được cấu hình trong hệ thống.", null);
	    }

	    order.setOrderStatus(deliveredStatus.get());
	    orderJpa.save(order);

	    return new ApiResponse<>(200, "Đơn hàng đã được xác nhận là đã nhận.", null);
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
			return new ApiResponse<>(404, "Không tìm thấy chi tiết đơn hàng", null);
		}

		OrderQRCodeDTO orderDetailDTO = orderDetailService.convertToOrderQRCode(orderDetailList);

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("orderDetail", Collections.singletonList(orderDetailDTO));

		return new ApiResponse<>(200, "Lấy chi tiết đơn hàng thành công", responseMap);
	}

	public ApiResponse<ByteArrayOutputStream> generateInvoicePdf(Integer orderId) throws Exception {

		ApiResponse<Map<String, Object>> apiResponse = getOrder(orderId);

		if (apiResponse.getErrorCode() != 200) {
			return new ApiResponse<>(apiResponse.getErrorCode(), apiResponse.getMessage(), null);
		}

		Map<String, Object> data = apiResponse.getData();
		OrderQRCodeDTO orderData = ((List<OrderQRCodeDTO>) data.get("orderDetail")).get(0);

		if (!"Processed".equalsIgnoreCase(orderData.getStatusName())) {
			return new ApiResponse<>(400, "Chỉ được phép tạo hóa đơn cho các đơn hàng 'Đã xử lý'.", null);
		}

		BaseFont bf = BaseFont.createFont("C:/Windows/Fonts/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		Font font = new Font(bf, 5, Font.BOLD);
		Font fontBold = new Font(bf, 6, Font.BOLD);
		Font infoFont = new Font(Font.FontFamily.HELVETICA, 4, Font.BOLD);

		Float height = calculateRequiredHeight(orderData, font);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document(createPageSize(height));
		document.setMargins(35, 35, -10, 0);

		PdfWriter writer = PdfWriter.getInstance(document, baos);
		document.open();

		String qrCodeData = generateQrCodeData(orderData);
		com.itextpdf.text.Image qrCodeImage = generateQrCodeImage(qrCodeData, 50);

		qrCodeImage.setAbsolutePosition(-5, document.getPageSize().getHeight() - 45);
		document.add(qrCodeImage);

		Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", font);

		title.setAlignment(Element.ALIGN_CENTER);

		title.setSpacingBefore(24);
		title.setSpacingAfter(10);
		LineSeparator line = new LineSeparator();
		line.setLineWidth(1f);
		line.setLineColor(BaseColor.BLACK);

		document.add(title);
		document.add(line);

		Paragraph orderInfoTitle = new Paragraph("Thông tin đơn hàng", fontBold);
		orderInfoTitle.setAlignment(Element.ALIGN_CENTER);
		document.add(orderInfoTitle);

		Paragraph orderIdParagraph = new Paragraph("Mã Hóa Đơn: " + orderData.getOrderId(), font);
		orderIdParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(orderIdParagraph);

		String formattedOrderDate = orderData.getOrderDate().toInstant().atZone(ZoneId.of("UTC"))
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

		Paragraph orderDateParagraph = new Paragraph("Ngày đặt hàng: " + formattedOrderDate, font);
		orderDateParagraph.setAlignment(Element.ALIGN_CENTER);
		document.add(orderDateParagraph);

		Paragraph finalTotal = new Paragraph(
				"Tổng tiền: " + FormarCurrencyUtil.formatCurrency(orderData.getFinalTotal()), font);
		finalTotal.setAlignment(Element.ALIGN_CENTER);
		document.add(finalTotal);

		PdfPTable billToTable = new PdfPTable(2);
		billToTable.setWidthPercentage(160);
		billToTable.setSpacingBefore(0);

		// Thông tin của công ty
		PdfPCell billFromCell = new PdfPCell();
		billFromCell.setBorder(Rectangle.NO_BORDER);

		Paragraph companyInfoTitle = new Paragraph("Đơn vị bán hàng:", fontBold);
		companyInfoTitle.setAlignment(Element.ALIGN_LEFT);
		billFromCell.addElement(companyInfoTitle);

		Paragraph companyName = new Paragraph("Cửa hàng thời trang Step To The Future", font);
		companyName.setAlignment(Element.ALIGN_LEFT);
		billFromCell.addElement(companyName);

		Paragraph companyAddress = new Paragraph("Địa chỉ: Đ. Số 22, Thường Thạnh, Cái Răng, Cần Thơ, Việt Nam", font);
		companyAddress.setAlignment(Element.ALIGN_LEFT);
		billFromCell.addElement(companyAddress);

		Paragraph companyPhone = new Paragraph("Điện thoại: 098 388 11 00", font);
		companyPhone.setAlignment(Element.ALIGN_LEFT);
		billFromCell.addElement(companyPhone);

		Paragraph companyEmail = new Paragraph("Email: ngothai3004@gmail.com", font);
		companyEmail.setAlignment(Element.ALIGN_LEFT);
		billFromCell.addElement(companyEmail);

		// Thông tin của khách hàng
		PdfPCell billToCell = new PdfPCell();
		billToCell.setBorder(Rectangle.NO_BORDER);

		Paragraph customerInfoTitle = new Paragraph("Khách hàng:", fontBold);
		customerInfoTitle.setAlignment(Element.ALIGN_LEFT);
		billToCell.addElement(customerInfoTitle);

		Paragraph customerName = new Paragraph("Tên khách hàng: " + orderData.getFullname(), font);
		customerName.setAlignment(Element.ALIGN_LEFT);
		billToCell.addElement(customerName);

		Paragraph customerAddress = new Paragraph("Địa chỉ: " + orderData.getAddress(), font);
		customerAddress.setAlignment(Element.ALIGN_LEFT);
		billToCell.addElement(customerAddress);

		Paragraph customerPhone = new Paragraph("Điện thoại: " + orderData.getPhone(), font);
		customerPhone.setAlignment(Element.ALIGN_LEFT);
		billToCell.addElement(customerPhone);

		billToTable.setSpacingAfter(10);

		billToTable.addCell(billFromCell);
		billToTable.addCell(billToCell);

		document.add(billToTable);
		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(160);
		table.setWidths(new float[] { 3f, 3f, 3f });

		PdfPCell headerCell;
		headerCell = new PdfPCell(new Phrase("Tên SP & SL", font));
		headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		headerCell.setPadding(5);
		table.addCell(headerCell);

		headerCell = new PdfPCell(new Phrase("Đơn Giá", font));
		headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		headerCell.setPadding(5);
		table.addCell(headerCell);

		headerCell = new PdfPCell(new Phrase("Thành Tiền", font));
		headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		headerCell.setPadding(5);
		table.addCell(headerCell);

		for (OrderDetailProductDetailsDTO product : orderData.getProductDetails()) {
			PdfPCell cell = new PdfPCell();
//			String color = product.getAttributeProductVersion().getColor() != null
//				    ? product.getAttributeProductVersion().getColor().getColor()
//				    : "N/A";
//				String size = product.getAttributeProductVersion().getSize() != null
//				    ? product.getAttributeProductVersion().getSize().getSize()
//				    : "N/A";

			Paragraph productNameParagraph = new Paragraph(product.getProductName() + " ,SL: " + product.getQuantity(),
					font);

			productNameParagraph.setLeading(font.getSize() * 1.2f);
			productNameParagraph.setAlignment(Element.ALIGN_LEFT);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.addElement(productNameParagraph);
			cell.setPadding(5);
			table.addCell(cell);

			cell = new PdfPCell(new Phrase(FormarCurrencyUtil.formatCurrency(product.getPrice()), font));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPadding(5);
			table.addCell(cell);

			cell = new PdfPCell(new Phrase(FormarCurrencyUtil.formatCurrency(product.getTotal()), font));
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPadding(5);
			table.addCell(cell);
			System.out.println(orderData.getProductDetails().get(0).getProductName() + " Có null không");
			System.out.println("Vô tới đây luôn");
		}
		document.add(table);
		PdfPTable summaryTable = new PdfPTable(2);
		summaryTable.setWidthPercentage(160);
		summaryTable.setWidths(new float[] { 6, 3 });

		PdfPCell descriptionCell;
		PdfPCell valueCell;

		descriptionCell = new PdfPCell(new Phrase("Tổng đơn hàng:", font));
		descriptionCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		descriptionCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		valueCell = new PdfPCell(new Phrase(FormarCurrencyUtil.formatCurrency(orderData.getSubTotal()), font));
		valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		valueCell.setPadding(5);
		summaryTable.addCell(descriptionCell);
		summaryTable.addCell(valueCell);

		descriptionCell = new PdfPCell(new Phrase("Phí vận chuyển:", font));
		descriptionCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		descriptionCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		valueCell = new PdfPCell(new Phrase(FormarCurrencyUtil.formatCurrency(orderData.getShippingFee()), font));
		valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		valueCell.setPadding(5);
		summaryTable.addCell(descriptionCell);
		summaryTable.addCell(valueCell);

		descriptionCell = new PdfPCell(
				new Phrase("Giảm giá: (" + FormarCurrencyUtil.formatDiscount(orderData.getDisCount()) + ")", font));
		descriptionCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		descriptionCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		valueCell = new PdfPCell(new Phrase(FormarCurrencyUtil.formatCurrency(orderData.getDiscountValue()), font));
		valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		valueCell.setPadding(5);
		summaryTable.addCell(descriptionCell);
		summaryTable.addCell(valueCell);

		descriptionCell = new PdfPCell(new Phrase("Tổng cộng:", font));
		descriptionCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		descriptionCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		valueCell = new PdfPCell(new Phrase(FormarCurrencyUtil.formatCurrency(orderData.getFinalTotal()), font));
		valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		valueCell.setPadding(5);
		summaryTable.addCell(descriptionCell);
		summaryTable.addCell(valueCell);

		document.add(summaryTable);
		PdfPTable tableTotalInWord = new PdfPTable(1);
		tableTotalInWord.setWidthPercentage(160);

		PdfPCell cellTotalInWord = new PdfPCell(new Phrase(orderData.getFinalTotalInWords(), font));
		cellTotalInWord.setHorizontalAlignment(Element.ALIGN_CENTER);
		cellTotalInWord.setPadding(10);
		cellTotalInWord.setBorder(PdfPCell.NO_BORDER);
		tableTotalInWord.addCell(cellTotalInWord);

		document.add(tableTotalInWord);

		document.close();
		sentEmail(orderData, baos);
		return new ApiResponse<>(200, "Tạo hóa đơn thành công.", baos);
	}

	private ApiResponse<?> sentEmail(OrderQRCodeDTO orderData, ByteArrayOutputStream baos) {
		String toEmail = orderData.getEmail();
		String subject = "Hóa đơn mua hàng";
		String body = generateEmailContent(orderData);

		try {
			CompletableFuture.runAsync(() -> {
				try {
					mailService.sendInvoiceEmail(toEmail, subject, body, baos);
				} catch (MessagingException e) {
					System.err.println("Lỗi khi gửi email: " + e.getMessage());
				}
			});
		} catch (Exception e) {
			return new ApiResponse<>(500, "Lỗi khi gửi email: " + e.getMessage(), null);
		}

		return new ApiResponse<>(200, "Email đang được gửi.", null);
	}

	private String generateEmailContent(OrderQRCodeDTO orderData) {
		return """
				  <!DOCTYPE html>
				  <html>
				  <head>
				      <style>
				          body { font-family: Arial, sans-serif; line-height: 1.6; }
				          .highlight { color: #007bff; font-weight: bold; }
				          .content { margin: 10px 0; }
				          .footer { margin-top: 20px; font-size: 0.9em; color: #555; }
				      </style>
				  </head>
				  <body>
				      <h2>Cảm ơn bạn đã mua hàng</h2>
				      <p class="content">Vui lòng xem hóa đơn đính kèm.</p>
				          <p>Vui lòng kiểm tra lại thông tin đơn hàng của bạn.</p>
							<p>
				    Nếu bạn có bất kỳ câu hỏi nào hay cần sự hỗ trợ thêm, xin vui lòng liên hệ với chúng tôi qua thông tin dưới đây:
				</p>

				<p class="content">
				    - Email: <a href='mailto:ngothai3004@gmail.com' style='color: #007bff;'>ngothai3004@gmail.com</a><br>
				    - Điện thoại: <span class="highlight">(+84) 939 658 044</span>
				</p>
				 <p>Xin cảm ơn bạn đã mua sắm cùng chúng tôi!</p>
				<p>Trân trọng,<br>Công ty TNHH Step To The Future</p>
				<p class="footer">
				   <small>Đây là email tự động, vui lòng không trả lời email này.</small>
				</p>
				  </body>
				  </html>
				  """
				.formatted(orderData.getFullname());
	}

	private com.itextpdf.text.Image generateQrCodeImage(String qrCodeData, int size)
			throws WriterException, IOException, BadElementException, com.google.zxing.WriterException {
		Map<EncodeHintType, Object> hintMap = new HashMap<>();
		hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

		com.google.zxing.common.BitMatrix matrix = new MultiFormatWriter().encode(qrCodeData, BarcodeFormat.QR_CODE,
				size, size);
		Integer dpi = 200;
		BufferedImage image = matrixToBufferedImage(matrix, dpi);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		byte[] imageBytes = baos.toByteArray();

		com.itextpdf.text.Image iTextImage = com.itextpdf.text.Image.getInstance(imageBytes);
		iTextImage.scaleAbsolute(size, size);
		return iTextImage;
	}

	private BufferedImage matrixToBufferedImage(com.google.zxing.common.BitMatrix matrix, int dpi) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
			}
		}
		return image;
	}

	private String generateQrCodeData(OrderQRCodeDTO orderData) {
		return "https://stepstothefuture.store/orders/" + orderData.getOrderId();
	}

	public ApiResponse<BufferedImage> convertPdfToImage(ByteArrayOutputStream pdfStream) {
		try {
			byte[] pdfBytes = pdfStream.toByteArray();
			try (PDDocument pdfDocument = PDDocument.load(pdfBytes)) {
				PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
				BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
				return new ApiResponse<>(200, "Success", image);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return new ApiResponse<>(500, "Error converting PDF to image: " + e.getMessage(), null);
		}
	}

	private float calculateRequiredHeight(OrderQRCodeDTO orderData, Font font) {
		float height = 0;
		height += 35f * 2;

		int numRows = orderData.getProductDetails().size() + 1;
		height += 10f * numRows;

		for (OrderDetailProductDetailsDTO productDetail : orderData.getProductDetails()) {
			String productName = productDetail.getProductName();
			float productNameHeight = font.getSize() * (int) Math.ceil((float) productName.length() / 8);
			height += productNameHeight;
		}

		float customerInfoHeight = 0;

		String customerName = orderData.getFullname();
		if (customerName != null) {
			customerInfoHeight += font.getSize() * (int) Math.ceil((float) customerName.length() / 25);
		}

		String customerAddress = orderData.getAddress();
		if (customerAddress != null) {
			customerInfoHeight += font.getSize() * (int) Math.ceil((float) customerAddress.length() / 20);
		}

		String customerPhone = orderData.getPhone();
		if (customerPhone != null) {
			customerInfoHeight += font.getSize() * (int) Math.ceil((float) customerPhone.length() / 25);
		}

		String customerEmail = orderData.getEmail();
		if (customerEmail != null) {
			customerInfoHeight += font.getSize() * (int) Math.ceil((float) customerEmail.length() / 25);
		}

		height += customerInfoHeight;

		return height;
	}

	private Rectangle createPageSize(float height) {
		float widthInPoints = 58 * 2.83465f;
		float heightInPoints = height * 2.83465f;
		return new Rectangle(widthInPoints, heightInPoints);
	}

	public ResponseEntity<ApiResponse<?>> refundOrder(Integer orderId, User currentUser) { 
	    Optional<Order> orderOptional = orderJpa.findById(orderId);
	    if (!orderOptional.isPresent()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy đơn hàng", null));
	    }

	    Order order = orderOptional.get();

	    if (order.getPayments().getAmount().compareTo(BigDecimal.valueOf(-2)) == 0) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(new ApiResponse<>(400, "Đơn hàng đã được hoàn tiền trước đó", null));
	    }

	    try {
	        Payment payment = order.getPayments();
	        payment.setAmount(BigDecimal.valueOf(-2));
	        paymentJpa.save(payment);

	        order.setOrderStatus(orderStatusJpa.findByStatusName("Cancelled")
	                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái 'Đã hủy'")));
	        order.setLastUpdatedBy(currentUser);
	        order.setLastUpdatedDate(new Date());

	        orderJpa.save(order);
	        return ResponseEntity.ok(new ApiResponse<>(200, "Hoàn tiền thành công cho đơn hàng #" + order.getOrderId(), null));

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new ApiResponse<>(500, "Đã xảy ra lỗi trong quá trình xử lý hoàn tiền", null));
	    }
	}

}
