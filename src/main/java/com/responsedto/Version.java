package com.responsedto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Version {
	private int id;
	private int versionSaleId;
	private String versionName;
	private BigDecimal price;
	private BigDecimal sale;
	private BigDecimal salePrice;
	private boolean active;
	private int quantity;
	private boolean inStock;
	private String image;
	private List<Attribute> attributes;
}
