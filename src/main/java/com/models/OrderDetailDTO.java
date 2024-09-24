package com.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class OrderDetailDTO {
	private int orderId;
	private String address;
	private Integer couponId;
	private Date deliveryDate;
	private String disPercent;
	private String disPrice;
	private String fullname;
	private Date orderDate;
	private boolean paymentStatus;
	private String phone;
	private String statusName;
	private BigDecimal total;
	private String paymentMethod;
	private String phoneNumber;
	private String email;
	private List<OrderDetailProductDetailsDTO> productDetails;

	public OrderDetailDTO(int orderId, String address, Integer couponId, Date deliveryDate, String disPercent,
			String disPrice, String fullname, Date orderDate, boolean paymentStatus, String phone,
			String statusName, BigDecimal total, String paymentMethod, String phoneNumber,
			String email, List<OrderDetailProductDetailsDTO> productDetails) {
		this.orderId = orderId;
		this.address = address;
		this.couponId = couponId;
		this.deliveryDate = deliveryDate;
		this.disPercent = disPercent;
		this.disPrice = disPrice;
		this.fullname = fullname;
		this.orderDate = orderDate;
		this.paymentStatus = paymentStatus;
		this.phone = phone;
		this.statusName = statusName;
		this.total = total;
		this.paymentMethod = paymentMethod;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.productDetails = productDetails;
	}

}
