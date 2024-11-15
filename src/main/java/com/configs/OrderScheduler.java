package com.configs;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.entities.Order;
import com.entities.OrderStatus;
import com.entities.User;
import com.errors.ApiResponse;
import com.repositories.OrderJPA;
import com.repositories.OrderStatusJPA;
import com.repositories.UserJPA;
import com.services.OrderUtilsService;

import jakarta.transaction.Transactional;

@Component
public class OrderScheduler {

    private final OrderJPA orderJpa;
    private final OrderStatusJPA orderStatusJpa;
    private final UserJPA userJpa;
    private final OrderUtilsService orderUtilsService;

    public OrderScheduler(OrderJPA orderJpa, OrderStatusJPA orderStatusJpa, 
                          UserJPA userJpa, OrderUtilsService orderUtilsService) {
        this.orderJpa = orderJpa;
        this.orderStatusJpa = orderStatusJpa;
        this.userJpa = userJpa;
        this.orderUtilsService = orderUtilsService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleOrderCancellation() {
        Date threeDaysAgo = new Date(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000));

        List<Order> orders = orderJpa.findAllByCreatedAtBeforeAndOrderStatusStatusName(threeDaysAgo, "Pending");

        for (Order order : orders) {
            cancelOrder(order.getOrderId(), order.getUser());
        }
    }

    @Transactional
    public void cancelOrder(Integer orderId, User currentUser) {
        Optional<Order> updatedOrder = orderJpa.findById(orderId);
        if (updatedOrder.isEmpty()) {
            System.err.println("Order with ID " + orderId + " does not exist.");
            return;
        }

        Order order = updatedOrder.get();

        Optional<OrderStatus> cancelledStatus = orderStatusJpa.findByStatusNameIgnoreCase("Cancelled");
        if (cancelledStatus.isEmpty()) {
            System.err.println("Cancelled status not found in system.");
            return;
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

    }


}
