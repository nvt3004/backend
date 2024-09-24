package com.responsedto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartOrderResponse {
	private int orderId;
	private Date orderDate;
	private Date deliveryDate;
	private String fullname;
	private String phone;
	private String address;
	private String paymentName;
	private String statusOrderName;
	private BigDecimal disPrice;
	private BigDecimal disPercent;
	private String couponCode;
	private int totalProduct;
}
