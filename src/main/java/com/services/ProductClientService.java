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

	public Page<ProductDTO> getFilteredProducts(User user, String categoryName, BigDecimal minPrice,
			BigDecimal maxPrice, String color, String size, String sort, int page, int pageSize) {

		categoryName = (categoryName == null || categoryName.isEmpty()) ? null : categoryName;
		minPrice = (minPrice == null || minPrice.compareTo(BigDecimal.ZERO) == 0) ? null : minPrice;
		maxPrice = (maxPrice == null || maxPrice.compareTo(BigDecimal.ZERO) == 0) ? null : maxPrice;
		color = (color == null || color.isEmpty()) ? null : color;
		size = (size == null || size.isEmpty()) ? null : size;

		if (sort == null || (!sort.equalsIgnoreCase("ASC") && !sort.equalsIgnoreCase("DESC"))) {
			sort = "ASC";
		}

		if (page < 0) {
			page = 0;
		}
		if (pageSize <= 0) {
			pageSize = 10;
		}

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(sort.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, "retailPrice"));

		try {

			return this.FilteredProducts(user, categoryName, minPrice, maxPrice, color, size, sort, pageable);

		} catch (Exception e) {
			System.out.println("Error: " + e);

			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}
	}

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
					// Xử lý danh sách hình ảnh
					for (Image img : productVer.getImages()) {
						images.add(img.getImageUrl());
					}
				}
				productDTO.setImgName(images.get(0));
				productDTO.setMinPrice(minPrice);
				productDTO.setMaxPrice(maxPrice);
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

				BigDecimal minPrice = null;
				BigDecimal maxPrice = new BigDecimal("0.00");
				List<String> images = new ArrayList<>();
				for (ProductVersion productVer : product.getProductVersions()) {

					// Cập nhật minPrice và maxPrice
					if (minPrice == null || productVer.getRetailPrice().compareTo(minPrice) < 0) {
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
   // còn sử dụng
	public List<ProductDTO> getALLProduct(User user) {
		List<ProductDTO> productDTOs = new ArrayList<>();
		try {
			List<Product> products = productJPA.findAll();
			if (products == null || products.isEmpty()) {
				return productDTOs; // Trả về danh sách trống nếu không có sản phẩm
			}
			List<Wishlist> wishlist = null;
			if (user != null) {
				wishlist = user.getWishlists();
			}
			for (Product product : products) {
				// Tạo đối tượng ProductDTO mới
				ProductDTO productDTO = new ProductDTO();
				if (wishlist != null) {
					for (Wishlist wishlist2 : wishlist) {
						if (wishlist2.getProduct().getProductId() == product.getProductId()) {
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
					productDTO.setReviewCount(feedbacks.size());
					// Tính tổng điểm feedback
					int totalRating = 0;
					for (Feedback fd : feedbacks) {
						totalRating += fd.getRating();
					}
					double averageRating = (double) totalRating / feedbacks.size();
					productDTO.setRating(averageRating);
				} else {
					productDTO.setRating(0);
					productDTO.setReviewCount(0);
				}
				// Set danh sách categories
				List<String> categories = new ArrayList<>();
				for (ProductCategory category : product.getProductCategories()) {
					categories.add(category.getCategory().getCategoryName());
				}
				productDTO.setCategories(categories);

				// Set phiên bản sản phẩm (versions), colors, sizes
				List<String> versionName = new ArrayList<>();
				List<String> colors = new ArrayList<>();
				List<String> sizes = new ArrayList<>();
				List<String> images = new ArrayList<>();

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
						} else {
							color = productVer.getAttributeOptionsVersions().get(1).getAttributeOption()
									.getAttributeValue();
							size = productVer.getAttributeOptionsVersions().get(0).getAttributeOption()
									.getAttributeValue();
						}

						colors.add(color);
						sizes.add(size);
					}
					for (Image img : productVer.getImages()) {
						images.add(img.getImageUrl());
					}
					// Cập nhật minPrice và maxPrice
					if (minPrice == null || productVer.getRetailPrice().compareTo(minPrice) < 0) {
						minPrice = productVer.getRetailPrice();
					}
					if (productVer.getRetailPrice().compareTo(maxPrice) > 0) {
						maxPrice = productVer.getRetailPrice();
					}
				}
				productDTO.setImgName(images.get(0));
				productDTO.setVersionName(versionName);
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

	public Page<ProductDTO> FilteredProducts(User user, String categoryName, BigDecimal minPricec, BigDecimal maxPricec,
			String colorc, String sizec, String sort, Pageable pageable) {

		List<ProductDTO> productDTOs = new ArrayList<>();
		try {
			List<Product> products = productJPA.findAll();
			if (products == null || products.isEmpty()) {
				return new PageImpl<>(new ArrayList<>()); // Trả về danh sách trống nếu không có sản phẩm
			}

			List<Wishlist> wishlist = (user != null) ? user.getWishlists() : null;
			for (Product product : products) {
				boolean checkProduct = false;
				boolean checkProductPrice = false;
				// Tạo đối tượng ProductDTO mới
				ProductDTO productDTO = new ProductDTO();

				// Kiểm tra sản phẩm có nằm trong danh sách yêu thích hay không
				if (wishlist != null) {
					for (Wishlist wishlistItem : wishlist) {
						if (wishlistItem.getProduct().getProductId() == product.getProductId()) {
							productDTO.setLike(true);
							break;
						}
					}
				}

				// Xử lý các thuộc tính của ProductDTO
				productDTO.setObjectID(String.valueOf(product.getProductId()));
				productDTO.setId(String.valueOf(product.getProductId()));
				productDTO.setName(product.getProductName());
				productDTO.setDescription(product.getDescription());

				// Xử lý rating từ feedbacks
				List<Feedback> feedbacks = product.getFeedbacks();
				if (feedbacks != null && !feedbacks.isEmpty()) {
					int totalRating = feedbacks.stream().mapToInt(Feedback::getRating).sum();
					double averageRating = (double) totalRating / feedbacks.size();
					productDTO.setRating(averageRating);
					productDTO.setReviewCount(feedbacks.size());
				} else {
					productDTO.setRating(0);
					productDTO.setReviewCount(0);
				}

				// Set danh sách categories
				List<String> categories = new ArrayList<>();
				for (ProductCategory category : product.getProductCategories()) {
					categories.add(category.getCategory().getCategoryName());
				}
				productDTO.setCategories(categories);
				// Kiểm tra nếu categoryName không null và sản phẩm thuộc danh mục đó
				if (categoryName != null && categories.get(0) != null
						&& categories.get(0).trim().equalsIgnoreCase(categoryName.trim())) {
					checkProduct = true;
				}

				// Set phiên bản sản phẩm (versions), colors, sizes, images
				List<String> versionName = new ArrayList<>();
				List<String> colors = new ArrayList<>();
				List<String> sizes = new ArrayList<>();
				List<String> images = new ArrayList<>();

				BigDecimal minPrice = null;
				BigDecimal maxPrice = BigDecimal.ZERO;

				for (ProductVersion productVer : product.getProductVersions()) {
					versionName.add(productVer.getVersionName());

					// Xử lý màu sắc và kích thước
					if (productVer.getAttributeOptionsVersions().size() >= 2) {
						String color = productVer.getAttributeOptionsVersions().get(0).getAttributeOption()
								.getAttributeValue();
						String size = productVer.getAttributeOptionsVersions().get(1).getAttributeOption()
								.getAttributeValue();

						colors.add(color);
						sizes.add(size);

						// Kiểm tra điều kiện lọc theo màu và kích thước
						if ((colorc != null && colorc.equalsIgnoreCase(color))
								|| (sizec != null && sizec.equalsIgnoreCase(size))) {
							checkProduct = true;
						}
					}

					// Xử lý hình ảnh
					for (Image img : productVer.getImages()) {
						images.add(img.getImageUrl());
					}

					// Cập nhật minPrice và maxPrice
					if (minPrice == null || productVer.getRetailPrice().compareTo(minPrice) < 0) {
						minPrice = productVer.getRetailPrice();
					}
					if (productVer.getRetailPrice().compareTo(maxPrice) > 0) {
						maxPrice = productVer.getRetailPrice();
					}
				}

				productDTO.setImgName(images.isEmpty() ? null : images.get(0));
				productDTO.setVersionName(versionName);
				productDTO.setColors(colors);
				productDTO.setSizes(sizes);
				productDTO.setMinPrice(minPrice);
				productDTO.setMaxPrice(maxPrice);
				productDTO.setImages(images);

				// Kiểm tra điều kiện lọc theo minPricec và maxPricec
				if (minPricec != null && maxPricec != null) {
					if (maxPrice.compareTo(minPricec) >= 0 && maxPrice.compareTo(maxPricec) <= 0) {
						checkProductPrice = true;
					}
				} else {
					if (minPricec != null && maxPrice.compareTo(minPricec) >= 0) {
						checkProductPrice = true;
					}
					if (maxPricec != null && maxPrice.compareTo(maxPricec) <= 0) {
						checkProductPrice = true;
					}
				}

				// Thêm productDTO vào danh sách nếu thỏa mãn các điều kiện lọc
				if (checkProduct && (checkProductPrice || (minPricec == null && maxPricec == null))) {
					productDTOs.add(productDTO);
				}
				if (categoryName == null && colorc == null && sizec == null && minPricec == null && maxPricec == null) {
					productDTOs.add(productDTO);
				}
			}
			if (sort.equalsIgnoreCase("DESC")) {
				productDTOs.sort(Comparator.comparing(ProductDTO::getMaxPrice).reversed());
			} else {
				productDTOs.sort(Comparator.comparing(ProductDTO::getMaxPrice));
			}

			// Phân trang kết quả
			int start = (int) pageable.getOffset();
			int end = Math.min(start + pageable.getPageSize(), productDTOs.size());
			return new PageImpl<>(productDTOs.subList(start, end), pageable, productDTOs.size());

		} catch (Exception e) {
			System.out.println("Error: " + e);
			return new PageImpl<>(new ArrayList<>());
		}
	}

}
