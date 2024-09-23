package com.services;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.AttributeOption;
import com.entities.Category;
import com.entities.Feedback;
import com.entities.Image;
import com.entities.Product;
import com.entities.ProductCategory;
import com.entities.ProductVersion;
import com.repositories.AttributeOptionJPA;
import com.repositories.CategoryJPA;
import com.repositories.ProductJPA;
import com.repositories.ProductRepository;
import com.responsedto.ProductDTO;

@Service
public class ProductInforService {
	@Autowired
	ProductJPA productJPA;
	@Autowired
	CategoryJPA categoryJPA;
	@Autowired
	AttributeOptionJPA attributeOptionJPA;
	@Autowired
	private ProductRepository productRepository;

	public List<ProductDTO> getFilteredProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
			Integer color, Integer size, String sortPrice) {
		if ((categoryId == null) || (categoryId == 0 || categoryId < 0)) {
			categoryId = null;
		}
		if ((minPrice == null) || (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) == 0)) {
			minPrice = null;
		}
		if ((maxPrice == null) || (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) == 0)) {
			maxPrice = null;
		}
		if ((color == null) || (color == 0 || color < 0)) {
			color = null;
		}
		if ((size == null) || (size == 0 || size < 0)) {
			size = null;
		}

		if (sortPrice == null || sortPrice.isEmpty()) {
			sortPrice = "ASC";
		}
		return productRepository.callGetFilteredProducts(categoryId, minPrice, maxPrice, color, size, sortPrice);
	}

	public List<ProductDTO> getListProductByCategoryId(int id) {
		if (id == -1) {
			return getALLProduct();
		}
		Optional<Category> category = categoryJPA.findById(id);

		List<ProductDTO> productDTOs = new ArrayList<>();
		if (category != null) {
			List<ProductCategory> list = category.get().getProductCategories();
			for (ProductCategory productCategory : list) {

				ProductDTO productDTO = new ProductDTO();
				productDTO.setId(String.valueOf(productCategory.getProduct().getProductId()));
				productDTO.setName(productCategory.getProduct().getProductName());
				productDTO.setImg(productCategory.getProduct().getProductImg());
				BigDecimal minPrice = new BigDecimal("0.00");
				BigDecimal maxPrice = new BigDecimal("0.00");
				List<String> images = new ArrayList<>();
				for (ProductVersion productVer : productCategory.getProduct().getProductVersions()) {

					// Cập nhật minPrice và maxPrice
					if (minPrice.equals(BigDecimal.ZERO) || productVer.getRetailPrice().compareTo(minPrice) < 0) {
						minPrice = productVer.getRetailPrice();
					}
					if (productVer.getRetailPrice().compareTo(maxPrice) > 0) {
						maxPrice = productVer.getRetailPrice();
					}
					// Xử lý danh sách hình ảnh
					for (Image img : productVer.getImages()) {
						images.add(img.getImageUrl());
					}
				}
				productDTO.setMinPrice(minPrice);
				productDTO.setMaxPrice(maxPrice);
				productDTO.setImages(images);
				// Thêm productDTO vào danh sách
				productDTOs.add(productDTO);
			}
			return productDTOs;
		}
		return productDTOs;

	}

	public List<AttributeOption> getListByAttributeName(String attributeName) {
		List<AttributeOption> attributeOptions = new ArrayList<>();
		for (AttributeOption attOp : attributeOptionJPA.findAll()) {
			if (attOp.getAttribute().getAttributeName().equalsIgnoreCase(attributeName)) {
				attributeOptions.add(attOp);
			}
			attOp.setAttributeOptionsVersions(null);
		}
		return attributeOptions;
	}

	public List<AttributeOption> getListColor() {
		return getListByAttributeName("color");
	}

	public List<AttributeOption> getListSize() {
		return getListByAttributeName("size");
	}

	public List<Category> getListCategory() {
		return categoryJPA.findAll();
	}

	public List<ProductDTO> getALLProduct() {
		List<ProductDTO> productDTOs = new ArrayList<>();
		try {
			List<Product> products = productJPA.findAll();
			if (products == null || products.isEmpty()) {
				return productDTOs; // Trả về danh sách trống nếu không có sản phẩm
			}
			for (Product product : products) {
				// Tạo đối tượng ProductDTO mới

				ProductDTO productDTO = new ProductDTO();
				productDTO.setId(String.valueOf(product.getProductId()));
				productDTO.setName(product.getProductName());
				productDTO.setImg(product.getProductImg());
				BigDecimal minPrice = new BigDecimal("0.00");
				BigDecimal maxPrice = new BigDecimal("0.00");
				List<String> images = new ArrayList<>();
				for (ProductVersion productVer : product.getProductVersions()) {

					// Cập nhật minPrice và maxPrice
					if (minPrice.equals(BigDecimal.ZERO) || productVer.getRetailPrice().compareTo(minPrice) < 0) {
						minPrice = productVer.getRetailPrice();
					}
					if (productVer.getRetailPrice().compareTo(maxPrice) > 0) {
						maxPrice = productVer.getRetailPrice();
					}
					// Xử lý danh sách hình ảnh
					for (Image img : productVer.getImages()) {
						images.add(img.getImageUrl());
					}
				}
				productDTO.setMinPrice(minPrice);
				productDTO.setMaxPrice(maxPrice);
				productDTO.setImages(images);
				// Thêm productDTO vào danh sách
				productDTOs.add(productDTO);
			}
			return productDTOs;
		} catch (Exception e) {
			System.out.println("Error: " + e);
			return productDTOs;
		}
	}

	public List<ProductDTO> getALLInforProduct() {
		List<ProductDTO> productDTOs = new ArrayList<>();
		try {
			List<Product> products = productJPA.findAll();
			if (products == null || products.isEmpty()) {
				return productDTOs; // Trả về danh sách trống nếu không có sản phẩm
			}
			for (Product product : products) {
				// Tạo đối tượng ProductDTO mới
				ProductDTO productDTO = new ProductDTO();
				productDTO.setObjectID(String.valueOf(product.getProductId()));
				productDTO.setId(String.valueOf(product.getProductId()));
				productDTO.setName(product.getProductName());
				productDTO.setImg(product.getProductImg());
				productDTO.setDescription(product.getDescription());

				// Xử lý rating từ feedbacks
				List<Feedback> feedbacks = product.getFeedbacks();

				if (feedbacks != null && !feedbacks.isEmpty()) {
					// Tính tổng điểm feedback
					int totalRating = 0;
					for (Feedback fd : feedbacks) {
						totalRating += fd.getRating();
					}
					double averageRating = (double) totalRating / feedbacks.size();
					productDTO.setRating(averageRating);
				} else {
					productDTO.setRating(0);
				}
				// Set danh sách categories
				List<String> categories = new ArrayList<>();
				for (ProductCategory category : product.getProductCategories()) {
					categories.add(category.getCategory().getCategoryName());
				}
				productDTO.setCategories(categories);

				// Set phiên bản sản phẩm (versions), colors, sizes
				List<ProductDTO.Version> versions = new ArrayList<>();
				List<String> colors = new ArrayList<>();
				List<String> sizes = new ArrayList<>();
				List<String> images = new ArrayList<>();

				BigDecimal minPrice = new BigDecimal("0.00");
				BigDecimal maxPrice = new BigDecimal("0.00");

				for (ProductVersion productVer : product.getProductVersions()) {
					ProductDTO.Version version = new ProductDTO.Version();
					version.setVersionName(productVer.getVersionName());

					// Xử lý thuộc tính màu sắc và kích thước
					if (productVer.getAttributeOptionsVersions() != null
							&& productVer.getAttributeOptionsVersions().size() >= 2) {
						String color = null;
						String size = null;

						// Phân biệt giữa color và size
						if (productVer.getAttributeOptionsVersions().get(0).getAttributeOption().getAttribute()
								.getAttributeName().toLowerCase().equals("color")) {
							color = productVer.getAttributeOptionsVersions().get(0).getAttributeOption()
									.getAttributeValue();
							size = productVer.getAttributeOptionsVersions().get(1).getAttributeOption()
									.getAttributeValue();
						} else {
							color = productVer.getAttributeOptionsVersions().get(1).getAttributeOption()
									.getAttributeValue();
							size = productVer.getAttributeOptionsVersions().get(0).getAttributeOption()
									.getAttributeValue();
						}

						version.setColorName(color);
						version.setSize(size);
						colors.add(color);
						sizes.add(size);
					}

					version.setQuantity(productVer.getQuantity());
					version.setPrice(productVer.getRetailPrice());
					versions.add(version);

					// Xử lý danh sách hình ảnh
					for (Image img : productVer.getImages()) {
						images.add(img.getImageUrl());
					}

					// Cập nhật minPrice và maxPrice
					if (minPrice.equals(BigDecimal.ZERO) || productVer.getRetailPrice().compareTo(minPrice) < 0) {
						minPrice = productVer.getRetailPrice();
					}
					if (productVer.getRetailPrice().compareTo(maxPrice) > 0) {
						maxPrice = productVer.getRetailPrice();
					}
				}

				productDTO.setVersions(versions);
				productDTO.setColors(colors);
				productDTO.setSizes(sizes);
				productDTO.setMinPrice(minPrice);
				productDTO.setMaxPrice(maxPrice);
				productDTO.setImages(images);

				// Thêm productDTO vào danh sách
				productDTOs.add(productDTO);
			}
			return productDTOs;
		} catch (Exception e) {
			System.out.println("Error: " + e);
			return productDTOs;
		}
	}

}
