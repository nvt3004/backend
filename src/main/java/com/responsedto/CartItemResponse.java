package com.responsedto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
	private int catrItemId;
	private int versionId;
	private boolean statusVersion;
	private int inventory;
	private String productName;
	private int stockQuantity;
	private String image;
	private BigDecimal price;
	private int quantity;
	private List<Attribute> attributes;
	private ProductDetailResponse productDetail;
	private BigDecimal sale;
	private BigDecimal salePrice;
	
	public CartItemResponse(int catrItemId, int versionId, boolean statusVersion, int inventory, String versionName,
			BigDecimal price, int quantity, int stock, boolean active) {
		this.catrItemId = catrItemId;
		this.versionId = versionId;
		this.statusVersion = statusVersion;
		this.inventory = inventory;
		this.productName = versionName;
		this.price = price;
		this.quantity = quantity;
		this.stockQuantity = stock;
		this.statusVersion = active;
	}
	
}
