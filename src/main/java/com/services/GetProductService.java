package com.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.AttributeOptionsVersion;
import com.entities.Feedback;
import com.entities.Product;
import com.entities.ProductCategory;
import com.entities.ProductSale;
import com.entities.ProductVector;
import com.entities.ProductVersion;
import com.entities.User;
import com.entities.Wishlist;
import com.repositories.ProductJPA;
import com.repositories.ProductVectorJPA;
import com.responsedto.ProductDTO;

@Service
public class GetProductService {
	@Autowired
	private ProductVectorJPA productVectorJPA;

	@Autowired
	private ProductJPA productJPA;

	public List<ProductDTO> getDetailProductFormListVector(User user, String listVector) {
		List<String> vectorList = new ArrayList<>();

		if (listVector != null && !listVector.trim().isEmpty()) {
			String[] vectors = listVector.split(";");

			for (String vector : vectors) {
				vectorList.add(vector.trim());
			}
		}

		List<Product> products = new ArrayList<>();
		int maxProducts = 12; // Cố định số lượng sản phẩm là 12

		for (String vector : vectorList) {
			if (products.size() >= maxProducts) {
				break; // Dừng lại nếu đã đủ 12 sản phẩm
			}

			String[] values = vector.split(",");
			List<BigDecimal> processedVector = new ArrayList<>();

			for (String value : values) {
				BigDecimal decimalValue = new BigDecimal(value.trim());
				processedVector.add(decimalValue);
			}

			String vectorString = String.join(", ",
					processedVector.stream().map(BigDecimal::toPlainString).toArray(String[]::new));
			System.out.println(vectorString);

			List<ProductVector> productVectors = productVectorJPA.getProductVectorByVector(vectorString);

			if (productVectors != null) {
				Optional<Product> productOptional = null;
				for (ProductVector productVector : productVectors) {
					productOptional = productJPA.findById(productVector.getProductId());
					if (productOptional.isPresent()) {
						Product product = productOptional.get();
						products.add(product);
					}
				}
			}
		}

		Set<Integer> seenIds = new LinkedHashSet<>();
		products.removeIf(product -> !seenIds.add(product.getProductId()));

		List<ProductDTO> productDTOs = new ArrayList<>();
		try {
			if (products == null || products.isEmpty()) {
				return productDTOs;
			}

			List<Wishlist> wishlist = null;
			if (user != null) {
				wishlist = user.getWishlists();
			}

			int productCount = 0; // Biến đếm số lượng productDTO đã thêm vào danh sách
			for (Product product : products) {
				if (productCount >= maxProducts) {
					break; // Dừng lại nếu đã có 12 sản phẩm trong productDTOs
				}

				ProductDTO productDTO = new ProductDTO();
				if (wishlist != null) {
					for (Wishlist wishlistItem : wishlist) {
						if (wishlistItem.getProduct().getProductId() == product.getProductId()) {
							productDTO.setLike(true);
							break;
						}
					}
				}

				List<ProductSale> productSales = product.getProductSales();
				if (productSales != null && !productSales.isEmpty()) {
					ProductSale productSale = productSales.get(0);
					Date now = new Date();

					if (productSale.getEndDate() != null && productSale.getEndDate().after(now)) {
						productDTO.setDiscount(productSale.getDiscount());
					}
				}

				productDTO.setObjectID(String.valueOf(product.getProductId()));
				productDTO.setId(String.valueOf(product.getProductId()));
				productDTO.setName(product.getProductName());
				productDTO.setDescription(product.getDescription());

				// Xử lý rating từ feedbacks
				List<Feedback> feedbacks = product.getFeedbacks();
				if (feedbacks != null && !feedbacks.isEmpty()) {
					int totalRating = 0;
					for (Feedback fd : feedbacks) {
						totalRating += fd.getRating();
					}
					double averageRating = (double) totalRating / feedbacks.size();
					productDTO.setRating(averageRating);
				} else {
					productDTO.setRating(-1); // Không có feedback, giá trị mặc định là -1
				}

				// Set danh sách categories và ID
				List<String> categories = new ArrayList<>();
				List<Integer> categoryID = new ArrayList<>();
				for (ProductCategory category : product.getProductCategories()) {
					categories.add(category.getCategory().getCategoryName());
					categoryID.add(category.getCategory().getCategoryId());
				}
				productDTO.setCategories(categories);
				productDTO.setCategoryID(categoryID);

				// Set phiên bản sản phẩm (versions), colors, sizes, images
				List<String> versionName = new ArrayList<>();
				List<String> attName = new ArrayList<>();
				List<String> images = new ArrayList<>();
				List<Integer> attId = new ArrayList<>();

				BigDecimal minPrice = null;
				BigDecimal maxPrice = new BigDecimal("0.00");

				for (ProductVersion productVer : product.getProductVersions()) {
					versionName.add(productVer.getVersionName());

					// Xử lý thuộc tính màu sắc và kích thước
					if (productVer.getAttributeOptionsVersions() != null
							&& productVer.getAttributeOptionsVersions().size() >= 1) {

						for (AttributeOptionsVersion att : productVer.getAttributeOptionsVersions()) {
							attName.add(att.getAttributeOption().getAttributeValue());
							attId.add(att.getAttributeOption().getId());
						}

					}

					// Thêm ảnh sản phẩm và cập nhật min/max price
					if (productVer.getImage() != null) {
						images.add(productVer.getImage().getImageUrl());
					}
					if (minPrice == null || productVer.getRetailPrice().compareTo(minPrice) < 0) {
						minPrice = productVer.getRetailPrice();
					}
					if (productVer.getRetailPrice().compareTo(maxPrice) > 0) {
						maxPrice = productVer.getRetailPrice();
					}
				}

				productDTO.setVersionName(versionName);
				productDTO.setAttributeName(attName);
				productDTO.setMinPrice(minPrice);
				productDTO.setMaxPrice(maxPrice);
				productDTO.setImages(images);
				productDTO.setImgName(images.isEmpty() ? null : images.get(0));
				productDTO.setAttributeId(attId);

				if (product.isStatus() && productDTOs.size() < maxProducts) {
					productDTOs.add(productDTO);
					productCount++; // Tăng biến đếm số lượng sản phẩm đã thêm
				}
			}

			return productDTOs;
		} catch (Exception e) {
			System.out.println("Error: " + e);
			return productDTOs;
		}
	}
}
