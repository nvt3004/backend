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
	private String objectID; // Định danh duy nhất cho Algolia
	private String id;
	private String name;
	private String img;
	private String description;
	private double rating;
	private List<String> categories;
	private List<Version> versions;
	// Các trường phụ cho Algolia
	private List<String> colors; // Danh sách màu sắc
	private List<String> sizes; // Danh sách kích thước
	private BigDecimal minPrice; // Giá thấp nhất trong các phiên bản
	private BigDecimal maxPrice; // Giá cao nhất trong các phiên bản
	private List<String> images; // Danh sách hình ảnh

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Version {
		private String versionName;
		private String colorName;
		private String size;
		private int quantity;
		private BigDecimal price;
	}
}