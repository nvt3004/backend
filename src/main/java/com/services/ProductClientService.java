package com.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.AttributeOption;
import com.entities.AttributeOptionsVersion;
import com.entities.Category;
import com.entities.Feedback;
import com.entities.Product;
import com.entities.ProductCategory;
import com.entities.ProductVersion;
import com.entities.User;
import com.entities.Wishlist;
import com.repositories.AttributeOptionJPA;
import com.repositories.AttributeOptionsVersionJPA;
import com.repositories.CategoryJPA;
import com.repositories.FeedbackJPA;
import com.repositories.ProductCategoryJPA;
import com.repositories.ProductJPA;
import com.repositories.ProductVersionJPA;
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
	FeedbackJPA feedbackJPA;
	@Autowired
	ProductCategoryJPA categoryJPA2;
	@Autowired
	ProductVersionJPA productVersionJPA;
	@Autowired
	AttributeOptionsVersionJPA attributeOptionsVersionJPA;
	
	
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
	public List<ProductDTO> getAllProducts(User user) {
	    List<ProductDTO> productDTOs = new ArrayList<>();
	    try {
	        // Lấy tất cả sản phẩm
	        List<Product> products = productJPA.findAll();
	        if (products == null || products.isEmpty()) {
	            return productDTOs;
	        }

	        List<Wishlist> wishlist = user != null ? user.getWishlists() : null;

	        for (Product product : products) {
				ProductDTO productDTO = new ProductDTO();
	        	productDTO.setObjectID(String.valueOf(product.getProductId()));
				productDTO.setId(String.valueOf(product.getProductId()));
				productDTO.setName(product.getProductName());
				productDTO.setDescription(product.getDescription());
	            if (wishlist != null) {
	                productDTO.setLike(wishlist.stream()
	                    .anyMatch(wishlistItem -> wishlistItem.getProduct().getProductId() == product.getProductId()));
	            }

	            // Truy vấn rating của Feedback theo productId
	            Double averageRating = feedbackJPA.getAverageRatingByProductId(product.getProductId());
	            productDTO.setRating(averageRating != null ? averageRating : -1);

	            // Truy vấn danh mục (category) theo productId
	            List<ProductCategory> productCategories = categoryJPA2.findByProductId(product.getProductId());
	            productDTO.setCategories(productCategories.stream()
	                .map(category -> category.getCategory().getCategoryName())
	                .collect(Collectors.toList()));
	            productDTO.setCategoryID(productCategories.stream()
	                .map(category -> category.getCategory().getCategoryId())
	                .collect(Collectors.toList()));

	            // Truy vấn phiên bản sản phẩm (versions), colors, sizes, images
	            List<ProductVersion> productVersions = productVersionJPA.findByProductId(product.getProductId());
	            List<String> versionName = new ArrayList<>();
	            List<String> colors = new ArrayList<>();
	            List<String> sizes = new ArrayList<>();
	            List<String> images = new ArrayList<>();
	            List<Integer> colorID = new ArrayList<>();
	            List<Integer> sizeID = new ArrayList<>();
	            BigDecimal minPrice = null;
	            BigDecimal maxPrice = new BigDecimal("0.00");

	            for (ProductVersion productVer : productVersions) {
	                versionName.add(productVer.getVersionName());

	                // Lấy color và size từ AttributeOptionsVersions
	                List<AttributeOptionsVersion> options = attributeOptionsVersionJPA.findByProductVersionId(productVer.getId());
	                if (options.size() >= 2) {
	                    AttributeOptionsVersion colorOption = options.get(0);
	                    AttributeOptionsVersion sizeOption = options.get(1);

	                    if (colorOption.getAttributeOption().getAttribute().getAttributeName().equalsIgnoreCase("color")) {
	                        colors.add(colorOption.getAttributeOption().getAttributeValue());
	                        colorID.add(colorOption.getAttributeOption().getId());
	                        sizes.add(sizeOption.getAttributeOption().getAttributeValue());
	                        sizeID.add(sizeOption.getAttributeOption().getId());
	                    } else {
	                        sizes.add(colorOption.getAttributeOption().getAttributeValue());
	                        sizeID.add(colorOption.getAttributeOption().getId());
	                        colors.add(sizeOption.getAttributeOption().getAttributeValue());
	                        colorID.add(sizeOption.getAttributeOption().getId());
	                    }
	                }

	                // Thêm ảnh và cập nhật min/max price
	                if (productVer.getImage() != null) {
	                    images.add(productVer.getImage().getImageUrl());
	                }
	                minPrice = minPrice == null ? productVer.getRetailPrice() : minPrice.min(productVer.getRetailPrice());
	                maxPrice = maxPrice.max(productVer.getRetailPrice());
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
