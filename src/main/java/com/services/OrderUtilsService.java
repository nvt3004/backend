package com.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.entities.Order;
import com.entities.OrderDetail;

@Service
public class OrderUtilsService {

	public BigDecimal calculateOrderTotal(Order order) {
		BigDecimal total = BigDecimal.ZERO;
		for (OrderDetail orderDetail : order.getOrderDetails()) {
			BigDecimal retailPrice = orderDetail.getPrice();
			BigDecimal quantity = new BigDecimal(orderDetail.getQuantity());
			total = total.add(retailPrice.multiply(quantity));
		}
		return total.setScale(0, RoundingMode.DOWN);
	}

	public BigDecimal calculateDiscountedPrice(Order order) {
		BigDecimal total = calculateOrderTotal(order);
		BigDecimal discountAmount = BigDecimal.ZERO;

		if (order.getDisPrice() != null && order.getDisPrice().compareTo(BigDecimal.ZERO) > 0) {
			discountAmount = order.getDisPrice();
		} else if (order.getDisPercent() != null && order.getDisPercent().compareTo(BigDecimal.ZERO) > 0) {
			discountAmount = total.multiply(order.getDisPercent().divide(BigDecimal.valueOf(100)));
		}

		// Làm tròn về số nguyên, bỏ phần thập phân
		return discountAmount.setScale(0, RoundingMode.DOWN).max(BigDecimal.ZERO);
	}

	public String getDiscountDescription(Order order) {
		if (order.getDisPrice() != null && order.getDisPrice().compareTo(BigDecimal.ZERO) > 0) {
			return String.format("%s VND", order.getDisPrice().toPlainString());
		} else if (order.getDisPercent() != null && order.getDisPercent().compareTo(BigDecimal.ZERO) > 0) {
			return String.format("%s%%", order.getDisPercent().toPlainString());
		}
		return null;
	}

	private static final Map<String, String> STATUS_MAPPING = Map.of("Pending", "Chờ xử lý", "Processed", "Đã xử lý",
			"Shipped", "Đã giao", "Delivered", "Đã nhận", "Cancelled", "Đã hủy","Waitingforconfirmation","Chờ xác nhận");

	public static String translateStatus(String status) {
		return STATUS_MAPPING.getOrDefault(status, status);
	}
}
