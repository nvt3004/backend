package com.configs;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.entities.Image;
import com.entities.Order;
import com.entities.OrderDetail;
import com.entities.OrderStatus;
import com.entities.User;
import com.repositories.OrderJPA;
import com.repositories.OrderStatusJPA;
import com.repositories.UserJPA;
import com.services.MailService;
import com.services.OrderUtilsService;
import com.utils.DateUtils;
import com.utils.FormarCurrencyUtil;

import jakarta.transaction.Transactional;

@Component
public class OrderScheduler {

    private final OrderJPA orderJpa;
    private final OrderStatusJPA orderStatusJpa;
    private final MailService mailService;
    private final OrderUtilsService orderUtilsService;

    public OrderScheduler(OrderJPA orderJpa, OrderStatusJPA orderStatusJpa, 
                          UserJPA userJpa, MailService mailService, OrderUtilsService orderUtilsService) {
        this.orderJpa = orderJpa;
        this.orderStatusJpa = orderStatusJpa;
        this.mailService = mailService;
        this.orderUtilsService = orderUtilsService;
    }

    @Scheduled(cron = "0 0 7 * * ?") 
    public void scheduleOrderCancellation() {
        Date threeDaysAgo = new Date(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000));

        List<Order> orders = orderJpa.findAllByCreatedAtBeforeAndOrderStatusStatusName(threeDaysAgo, "Pending");

        for (Order order : orders) {
            String cancellationReason = "Đơn hàng quá lâu không được xác nhận.";
            cancelOrder(order.getOrderId(), order.getUser(), cancellationReason);
        }
    }

    @Transactional
    public void cancelOrder(Integer orderId, User currentUser, String reason) {
    	try {
            Optional<Order> updatedOrder = orderJpa.findById(orderId);
            if (updatedOrder.isEmpty()) {
                System.err.println("Order with ID " + orderId + " does not exist.");
                return;
            }

            Order order = updatedOrder.get();

            Optional<OrderStatus> cancelledStatus = orderStatusJpa.findByStatusNameIgnoreCase("Cancelled");
            if (cancelledStatus.isEmpty()) {
                return;
            }

            order.setOrderStatus(cancelledStatus.get());
            if (order.getPayments().getPaymentMethod().getMethodName().equalsIgnoreCase("Chuyển khoản")) {
                order.getPayments().setAmount(BigDecimal.valueOf(-1));
            }
            orderJpa.save(order);

            sendOrderStatusUpdateEmail(order, "Cancelled", reason);
		} catch (Exception e) {
			System.out.println(e + " Lỗi");
		}
    
    }
    private void sendOrderStatusUpdateEmail(Order order, String newStatus, String reason) {
		String customerEmail = order.getUser().getEmail();
		String subject = "Đơn hàng #" + order.getOrderId() + " " + getStatusMessage(newStatus);
		String htmlContent = generateOrderStatusEmailContent(order, newStatus, reason);

		mailService.sendHtmlEmail(customerEmail, subject, htmlContent);
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

//	@Scheduled(cron = "0 */1 * * * ?")
//	@Scheduled(cron = "0 0 7 * * ?") 
//	public void scheduleOrderToWaitingForConfirmation() {
//	    List<Order> orders = orderJpa.findAllByExpectedDeliveryDateAndOrderStatusStatusName(new Date(), "Shipped");
//	    System.out.println(orders.size());
//	    for (Order order : orders) {
//	        if (order.getOrderStatus().getStatusName().equalsIgnoreCase("Shipped")) {
//	            Optional<OrderStatus> waitingForConfirmationStatus = orderStatusJpa.findByStatusNameIgnoreCase("Waitingforconfirmation");
//	            if (waitingForConfirmationStatus.isPresent()) {
//	                order.setOrderStatus(waitingForConfirmationStatus.get());
//	                orderJpa.save(order);
//	                sendConfirmationEmail(order);
//	            }
//	        }
//	    }
//	}
	
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
	    mailService.sendHtmlEmail(to,subject,body);
	}
	
//	@Scheduled(cron = "0 */1 * * * ?")
	@Scheduled(cron = "0 0 0 * * ?") 
	public void scheduleOrderToDelivered() {
		Date today = new Date();
        System.out.println("Today: " + today);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -7); 
        Date sevenDaysAgo = calendar.getTime();
	    List<Order> orders = orderJpa.findAllByExpectedDeliveryDateAndOrderStatusStatusName(sevenDaysAgo, "Waitingforconfirmation");
	    System.out.println("Found " + orders.size() + " orders to update to 'Delivered'.");
	    
	    for (Order order : orders) {
	        if (order.getOrderStatus().getStatusName().equalsIgnoreCase("Waitingforconfirmation")) {
	            Optional<OrderStatus> deliveredStatus = orderStatusJpa.findByStatusNameIgnoreCase("Delivered");
	            if (deliveredStatus.isPresent()) {
	                order.setOrderStatus(deliveredStatus.get());
	                orderJpa.save(order);
	                System.out.println("Updated order ID " + order.getOrderId() + " to 'Delivered'.");
	            } else {
	                System.out.println("Error: Status 'Delivered' not found in database.");
	            }
	        }
	    }
	}


}
