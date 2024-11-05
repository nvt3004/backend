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
	private BigDecimal wholesalePrice;
	private boolean active;
	private int quantity;
	private List<ImageResponse> images;
	private List<Attribute> attributes;
}
