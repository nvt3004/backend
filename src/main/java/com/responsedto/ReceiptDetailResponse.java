package com.responsedto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDetailResponse {

	private String image;
	private String name;
	private BigDecimal importPrice;
	private int quantity;
	private BigDecimal total;
}
