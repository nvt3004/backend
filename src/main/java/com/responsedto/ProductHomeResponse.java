package com.responsedto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHomeResponse {
	private long id;
	private String productName;
	private String image;
	private double discount;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
}
