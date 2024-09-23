package com.models;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
	private Integer id;
	private String name;
	private Integer supplierId;
	private BigDecimal price;
	private String image;
	private String description;
	private List<VersionDTO> versions;
	private List<CategoryDTO> categories;
}
