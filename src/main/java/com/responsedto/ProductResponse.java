package com.responsedto;
import java.math.BigDecimal;
import java.util.List;

import com.models.CategoryDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
	private long id;
	private String productName;
	private BigDecimal price;
	private String image;
	private double discount;
	private List<CategoryDTO> categories;
	private List<ProductVersionResponse> versions;
}
