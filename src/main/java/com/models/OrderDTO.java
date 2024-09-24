package com.models;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class OrderDTO {
	private int orderId;
	private String fullname;
	private String phone;
	private String address;
	private Integer couponId;
	private Date deliveryDate;
	private Date orderDate;
	private String statusName;
	private BigDecimal total;
	private String paymentMethod;

	public OrderDTO(int orderId, String address, Integer couponId, Date deliveryDate, String fullname, Date orderDate,
			String phone, String statusName, BigDecimal total, String paymentMethod) {
		this.orderId = orderId;
		this.address = address;
		this.couponId = couponId;
		this.deliveryDate = deliveryDate;
		this.fullname = fullname;
		this.orderDate = orderDate;
		this.phone = phone;
		this.statusName = statusName;
		this.total = total;
		this.paymentMethod = paymentMethod;
	}

}