package com.models;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class OrderDTO {
	private Integer orderId;
	private Boolean isOpenOrderDetail;
	private Integer gender;
	private String fullname;
	private String phone;
	private String address;
	private Integer couponId;
	private String disCount;
	private BigDecimal discountValue;
	private BigDecimal subTotal;
	private BigDecimal shippingFee;
	private BigDecimal finalTotal;
	private String finalTotalInWords;
	private Date deliveryDate;
	private Date orderDate;
	private String statusName;
	private String paymentMethod;

	public OrderDTO(Integer orderId, Boolean isOpenOrderDetail, Integer gender, String address, Integer couponId, String disCount,
			BigDecimal discountValue, BigDecimal subTotal, BigDecimal shippingFee, BigDecimal finalTotal,
			String finalTotalInWords, Date deliveryDate, String fullname, Date orderDate, String phone,
			String statusName, String paymentMethod) {
		this.orderId = orderId;
		this.isOpenOrderDetail = isOpenOrderDetail;
		this.gender = gender;
		this.address = address;
		this.couponId = couponId;
		this.disCount = disCount;
		this.discountValue = discountValue;
		this.subTotal = subTotal;
		this.shippingFee = shippingFee;
		this.finalTotal = finalTotal;
		this.finalTotalInWords = finalTotalInWords;
		this.deliveryDate = deliveryDate;
		this.fullname = fullname;
		this.orderDate = orderDate;
		this.phone = phone;
		this.statusName = statusName;
		this.paymentMethod = paymentMethod;
	}
}
