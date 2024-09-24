package com.responsedto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponResponse {
	private int id;
	private String couponCode;
	private BigDecimal percent;
	private BigDecimal price;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private int quantity;
}
