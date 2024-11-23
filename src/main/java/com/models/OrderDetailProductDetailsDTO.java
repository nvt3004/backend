package com.models;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;


@Data
public class OrderDetailProductDetailsDTO {
	private Integer productId;
	private String productName;
	private Integer productVersionId;
	private String productVersionName;
	private BigDecimal price;
	private Integer quantity;
	private String imageUrl;
	private String description;
	private BigDecimal total;
	private Integer orderDetailId;
	private AttributeProductVersionDTO attributeProductVersion;
	private List<AttributeDTO> attributeProducts;

	public OrderDetailProductDetailsDTO(Integer productId, Integer productVersionId,String productVersionName, BigDecimal price, Integer quantity,
			String imageUrl, String description, BigDecimal total, Integer orderDetailId,AttributeProductVersionDTO attributeProductVersion,
			List<AttributeDTO> attributeProducts, String productName) {
		this.productId = productId;
		this.productVersionId = productVersionId;
		this.productVersionName = productVersionName;
		this.price = price;
		this.quantity = quantity;
		this.imageUrl = imageUrl;
		this.description = description;
		this.total = total;
		this.orderDetailId = orderDetailId;
		this.attributeProductVersion = attributeProductVersion;
		this.attributeProducts = attributeProducts;
		this.productName = productName;
	}
}
