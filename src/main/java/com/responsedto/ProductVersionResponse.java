package com.responsedto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVersionResponse {
	private int id;
	private int idProduct;
	private String versionName;
	private BigDecimal retailPrice;
	private BigDecimal importPrice;
	private boolean active;
	private Integer idSale;
	private BigDecimal sale;
	private String saleName;
	private int quantity;
	private ImageResponse image;
	private List<Attribute> attributes;
}
