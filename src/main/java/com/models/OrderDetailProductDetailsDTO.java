package com.models;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class OrderDetailProductDetailsDTO {
	private Integer productId;
	private Integer productVersionId;
	private BigDecimal price;
	private Integer quantity;
	private String imageUrl;
	private String description;
	private BigDecimal total;
	private Integer orderDetailId;
	private AttributeDTO attributeProductVersion;
	private List<AttributeDTO> attributeProducts;

	public OrderDetailProductDetailsDTO(Integer productId, Integer productVersionId, BigDecimal price, Integer quantity,
			String imageUrl, String description, BigDecimal total, Integer orderDetailId,AttributeDTO attributeProductVersion,
			List<AttributeDTO> attributeProducts) {
		this.productId = productId;
		this.productVersionId = productVersionId;
		this.price = price;
		this.quantity = quantity;
		this.imageUrl = imageUrl;
		this.description = description;
		this.total = total;
		this.orderDetailId = orderDetailId;
		this.attributeProductVersion = attributeProductVersion;
		this.attributeProducts = attributeProducts;
	}
}
