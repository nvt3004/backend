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
	        return new BigDecimal(decimalFormat.format(total));
	    }

	    public BigDecimal calculateDiscountedPrice(Order order) {
	        BigDecimal total = calculateOrderTotal(order);
	        BigDecimal discountedPrice = total;

	        if (order.getDisPrice() != null && order.getDisPrice().compareTo(BigDecimal.ZERO) > 0) {
	            discountedPrice = total.subtract(order.getDisPrice());
	        } 
	        else if (order.getDisPercent() != null && order.getDisPercent().compareTo(BigDecimal.ZERO) > 0) {
	            BigDecimal discount = total.multiply(order.getDisPercent().divide(BigDecimal.valueOf(100)));
	            discountedPrice = total.subtract(discount);
	        }

	        return discountedPrice.max(BigDecimal.ZERO);
	    }


}
