package com.services;

import java.math.BigDecimal;

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
        return total;
    }

    public String getPaymentMethod(Order order) {
        return order.getPayments().stream()
                .map(payment -> payment.getPaymentMethod().getMethodName())
                .findFirst()
                .orElse("N/A");
    }

    public String getPhoneNumber(Order order) {
        return order.getPhone();
    }
}
