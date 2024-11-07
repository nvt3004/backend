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
	private Integer reviewCount;
	private List<String> categories;
	private String imgName;
	private List<String> versionName;
	private List<String> colors;
	private List<String> sizes;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private String image;
	private Float discount;
	private Boolean like;
}