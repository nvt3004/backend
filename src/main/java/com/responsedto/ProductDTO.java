package com.responsedto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
	private String objectID;
	private String id;
	private String name;
	private String description;
	private double rating;
	private String imgName;
	private List<String> categories;
	private List<String> versionName;
	private List<String> attributeName;
	private List<String> images;
	private Float discount;
	private Boolean like;
	private List<Integer> categoryID;
	private List<Integer> attributeId;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private BigDecimal minPriceSale;
	private BigDecimal maxPriceSale;
	private BigDecimal discountPercent;
	private int quantity;
}