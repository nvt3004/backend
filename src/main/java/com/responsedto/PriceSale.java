package com.responsedto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceSale {
	private BigDecimal minPriceSale;
	private BigDecimal maxPriceSale;
	private BigDecimal discountPercent;
	private int quantity;
}
