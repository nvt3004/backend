package com.responsedto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
	private ProductHomeResponse product;
	private List<Version> versions;
	List<AttributeProductResponse> attributes;
}
