package com.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Order;
import com.entities.OrderDetail;
import com.repositories.OrderJPA;

@Service
public class ReportService {
	@Autowired
	OrderJPA orderJpa;

	// Thống kê doanh thu theo ngày
	public BigDecimal getReportRevenue(LocalDateTime startDate, LocalDateTime endDate) {
		List<Order> orders = orderJpa.getAllOrderByDate(startDate, endDate);
		BigDecimal total = BigDecimal.ZERO;

		for (Order od : orders) {
			BigDecimal totalDetail = BigDecimal.ZERO;
			BigDecimal disCount = BigDecimal.ZERO;

			for (OrderDetail dt : od.getOrderDetails()) {
				totalDetail = totalDetail.add(dt.getPrice().multiply(BigDecimal.valueOf(dt.getQuantity())));
			}

			if (od.getCoupon() != null) {
				if (od.getCoupon().getDisPercent() != null) {
					disCount = totalDetail.multiply(
							BigDecimal.ONE.subtract(od.getCoupon().getDisPercent().divide(new BigDecimal("100"))));
					totalDetail = disCount;
				} else {
					disCount = totalDetail.subtract(od.getCoupon().getDisPrice());
					totalDetail = disCount;
				}
			}

			total = total.add(totalDetail);
		}

		return total;
	}

	// Thống kê các sản phẩm đã bán theo ngày
	public Integer getTotalProductBuy(LocalDateTime startDate, LocalDateTime endDate) {
		List<Order> orders = orderJpa.getAllOrderByDate(startDate, endDate);
		Integer total = 0;

		for (Order od : orders) {
			for (OrderDetail dt : od.getOrderDetails()) {
				total += dt.getQuantity();
			}
		}

		return total;
	}
	
	
	// Thống kê lợi nhuận theo ngày
	public BigDecimal getTotalProfit(LocalDateTime startDate, LocalDateTime endDate) {
		List<Order> orders = orderJpa.getAllOrderByDate(startDate, endDate);
		BigDecimal total = BigDecimal.ZERO;

		for (Order od : orders) {
			BigDecimal disCount = BigDecimal.ZERO;
			
			for (OrderDetail dt : od.getOrderDetails()) {
				BigDecimal buyPrice = dt.getPrice().multiply(BigDecimal.valueOf(dt.getQuantity()));
				BigDecimal importPrice = dt.getProductVersionBean().getImportPrice().multiply(BigDecimal.valueOf(dt.getQuantity()));
				BigDecimal profit =  buyPrice.subtract(importPrice);
				
				total = total.add(profit);
			}
			
			if (od.getCoupon() != null) {
				if (od.getCoupon().getDisPercent() != null) {
					disCount = total.multiply(od.getCoupon().getDisPercent().divide(new BigDecimal("100")));
				} else {
					disCount = od.getCoupon().getDisPrice();
				}
			}
			
			total = total.subtract(disCount);
		}

		return total;
	}
	
	
}
