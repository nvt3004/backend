package com.services;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.springframework.stereotype.Service;

import com.entities.Order;
import com.entities.OrderDetail;

@Service
public class OrderUtilsService {

	 private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

	    public BigDecimal calculateOrderTotal(Order order) {
	        BigDecimal total = BigDecimal.ZERO;
	        for (OrderDetail orderDetail : order.getOrderDetails()) {
	            BigDecimal retailPrice = orderDetail.getPrice();
	            BigDecimal quantity = new BigDecimal(orderDetail.getQuantity());
	            total = total.add(retailPrice.multiply(quantity));
	        }
	        return total;
	    }

	    public BigDecimal calculateDiscountedPrice(Order order) {
	        // Tính tổng đơn hàng ban đầu (trước giảm giá)
	        BigDecimal total = calculateOrderTotal(order);
	        BigDecimal discountAmount = BigDecimal.ZERO;  // Số tiền giảm

	        // Kiểm tra và tính toán số tiền giảm theo giá trị cố định (disPrice)
	        if (order.getDisPrice() != null && order.getDisPrice().compareTo(BigDecimal.ZERO) > 0) {
	            discountAmount = order.getDisPrice();
	        } 
	        // Kiểm tra và tính toán số tiền giảm theo phần trăm (disPercent)
	        else if (order.getDisPercent() != null && order.getDisPercent().compareTo(BigDecimal.ZERO) > 0) {
	            discountAmount = total.multiply(order.getDisPercent().divide(BigDecimal.valueOf(100)));
	        }

	        // Trả về số tiền giảm, đảm bảo không có giá trị âm
	        return discountAmount.max(BigDecimal.ZERO);
	    }



}
