package com.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.responsedto.CartItemResponse;
import com.responsedto.VnpayDTO;

import jakarta.validation.constraints.NegativeOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartOrderModel {
	private String address;
	private String couponCode;
	private Integer paymentMethodId;
	private Boolean creatorIsAdmin;
	private Integer statusId;
	private BigDecimal fee;
	private List<CartOrderDetailModel> orderDetails;
	private Date leadTime;
	private VnpayDTO vnpay;
}
