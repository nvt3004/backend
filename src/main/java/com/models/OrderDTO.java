package com.models;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class OrderDTO {
	private Integer orderId;
	private Integer lastUpdatedById;
	private String lastUpdatedByFullname;
	private Date lastUpdatedDate;
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
	private BigDecimal amount;

	public OrderDTO(Integer orderId, Integer lastUpdatedById, String lastUpdatedByFullname, Date lastUpdatedDate,
			Boolean isOpenOrderDetail, Integer gender, String address, Integer couponId, String disCount,
			BigDecimal discountValue, BigDecimal subTotal, BigDecimal shippingFee, BigDecimal finalTotal,
			String finalTotalInWords, Date deliveryDate, String fullname, Date orderDate, String phone,
			String statusName, String paymentMethod, BigDecimal amount) {
		this.orderId = orderId;
		this.lastUpdatedById = lastUpdatedById;
		this.lastUpdatedByFullname = lastUpdatedByFullname;
		this.lastUpdatedDate = lastUpdatedDate;
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
		this.amount = amount; 
	}
}

