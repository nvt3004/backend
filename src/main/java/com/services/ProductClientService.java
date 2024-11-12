package com.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.entities.AttributeOption;
import com.entities.Category;
import com.entities.Feedback;
import com.entities.Image;
import com.entities.Product;
import com.entities.ProductCategory;
import com.entities.ProductSale;
import com.entities.ProductVersion;
import com.entities.User;
import com.entities.Wishlist;
import com.repositories.AttributeOptionJPA;
import com.repositories.CategoryJPA;
import com.repositories.ProductJPA;
import com.repositories.UserJPA;
import com.responsedto.ProductDTO;

@Service
public class ProductClientService {
	@Autowired
	ProductJPA productJPA;
	@Autowired
	UserJPA userJPA;
	@Autowired
	CategoryJPA categoryJPA;
	@Autowired
	AttributeOptionJPA attributeOptionJPA;
	@Autowired
	WishlistService wishlistService;

	public List<ProductDTO> getProductWish(User user) {
		List<Wishlist> wls = wishlistService.getAllWisListByUser(user);

		List<ProductDTO> productDTOs = new ArrayList<>();

		for (Wishlist wishlist : wls) {

			ProductDTO productDTO = new ProductDTO();
			productDTO.setId(String.valueOf(wishlist.getProduct().getProductId()));
			productDTO.setName(wishlist.getProduct().getProductName());
			BigDecimal minPrice = null;
			BigDecimal maxPrice = new BigDecimal("0.00");
			List<String> images = new ArrayList<>();
			for (ProductVersion productVer : wishlist.getProduct().getProductVersions()) {

				// Cập nhật minPrice và maxPrice
				if (minPrice == null || productVer.getRetailPrice().compareTo(minPrice) < 0) {
					minPrice = productVer.getRetailPrice();
				}
				if (productVer.getRetailPrice().compareTo(maxPrice) > 0) {
					maxPrice = productVer.getRetailPrice();
				}

				images.add(productVer.getImage() == null ? null : productVer.getImage().getImageUrl());
			}

			productDTO.setMinPrice(minPrice);
			productDTO.setMaxPrice(maxPrice);
			productDTO.setImgName(images.get(0));
			productDTO.setImages(images);
			productDTO.setLike(true);
			// Thêm productDTO vào danh sách
			productDTOs.add(productDTO);
		}
		return productDTOs;

	}

	public List<AttributeOption> getListByAttributeNameProduct(String attributeName) {
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
		return getListByAttributeNameProduct("color");
	}

	public List<AttributeOption> getListSize() {
		return getListByAttributeNameProduct("size");
	}

	public List<Category> getListCategory() {
		List<Category> list = categoryJPA.findAll();
		for (Category category : list) {
			category.setProductCategories(null);
		}
		return list;
	}

	// còn sử dụng
	public List<ProductDTO> getALLProduct(User user) {
		List<ProductDTO> productDTOs = new ArrayList<>();
		try {
			List<Product> products = productJPA.findAll();
			if (products == null || products.isEmpty()) {
				return productDTOs;
			}

			List<Wishlist> wishlist = null;
			if (user != null) {
				wishlist = user.getWishlists();
			}

			for (Product product : products) {
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
				List<String> colors = new ArrayList<>();
				List<String> sizes = new ArrayList<>();
				List<String> images = new ArrayList<>();
				List<Integer> colorID = new ArrayList<>();
				List<Integer> sizeID = new ArrayList<>();

				BigDecimal minPrice = null;
				BigDecimal maxPrice = new BigDecimal("0.00");

				for (ProductVersion productVer : product.getProductVersions()) {
					versionName.add(productVer.getVersionName());

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
							colorID.add(productVer.getAttributeOptionsVersions().get(0).getAttributeOption().getId());
							sizeID.add(productVer.getAttributeOptionsVersions().get(1).getAttributeOption().getId());
						} else {
							color = productVer.getAttributeOptionsVersions().get(1).getAttributeOption()
									.getAttributeValue();
							size = productVer.getAttributeOptionsVersions().get(0).getAttributeOption()
									.getAttributeValue();
							colorID.add(productVer.getAttributeOptionsVersions().get(1).getAttributeOption().getId());
							sizeID.add(productVer.getAttributeOptionsVersions().get(0).getAttributeOption().getId());
						}

						colors.add(color);
						sizes.add(size);
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
				productDTO.setColors(colors);
				productDTO.setSizes(sizes);
				productDTO.setMinPrice(minPrice);
				productDTO.setMaxPrice(maxPrice);
				productDTO.setImages(images);
				productDTO.setImgName(images.isEmpty() ? null : images.get(0));
				productDTO.setColorID(colorID);
				productDTO.setSizeID(sizeID);
				if (product.isStatus()) {
					productDTOs.add(productDTO);
				}

			}
			return productDTOs;
		} catch (Exception e) {
			System.out.println("Error: " + e);
			return productDTOs;
		}
	}
}
