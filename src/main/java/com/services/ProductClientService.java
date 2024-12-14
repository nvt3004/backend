package com.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.AttributeOption;
import com.entities.AttributeOptionsVersion;
import com.entities.Category;
import com.entities.Feedback;
import com.entities.Product;
import com.entities.ProductCategory;
import com.entities.ProductSale;
import com.entities.ProductVersion;
import com.entities.User;
import com.entities.Wishlist;
import com.repositories.AttributeOptionJPA;
import com.repositories.CartJPA;
import com.repositories.CategoryJPA;
import com.repositories.OrderJPA;
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
	@Autowired
	CartJPA cartJPA;
	@Autowired
	OrderJPA orderJPA;
	@Autowired
	AlgoliaProductService algoliaProductService;

	public void deleteProductOnAlgolia(Product product) {
		algoliaProductService.deleteProductFromAlgoliaAsync(String.valueOf(product.getProductId()));

	}

	public void updateProductOnAlgolia(Product product) {
		algoliaProductService.deleteProductFromAlgoliaAsync(String.valueOf(product.getProductId()));

		ProductDTO productDTO = new ProductDTO();

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

			productDTO.setVersionName(versionName);

			productDTO.setAttributeName(attName);

			productDTO.setMinPrice(minPrice);
			productDTO.setMaxPrice(maxPrice);
			productDTO.setImages(images);
			productDTO.setImgName(images.isEmpty() ? null : images.get(0));

			productDTO.setAttributeId(attId);

			if (product.isStatus()) {
				algoliaProductService.addProductToAlgoliaAsync(productDTO);
			}
		}
	}

	public List<ProductDTO> getRecommendedProducts(User user) {

		List<ProductDTO> topProducts = algoliaProductService.getTop50Products();
		topProducts = getRandomProducts(topProducts, 25);

		if (user == null) {
			System.out.println("User is null. Returning top products only.");
			return topProducts.stream().limit(24).collect(Collectors.toList());
		}

		List<Product> prodFromCart = cartJPA.getProductsByUserId(user.getUserId());
		List<Product> prodFromOrder = orderJPA.getProductsByUserId(user.getUserId());

		List<ProductDTO> wishlistProducts = getProductWish(user);
		List<ProductDTO> productsFromCart = getProductCart(prodFromCart);
		List<ProductDTO> productsFromOrder = getProductCart(prodFromOrder);

		wishlistProducts = getRandomProducts(wishlistProducts, 25);
		productsFromCart = getRandomProducts(productsFromCart, 25);
		productsFromOrder = getRandomProducts(productsFromOrder, 25);

		Set<String> uniqueProductIds = new HashSet<>();
		List<ProductDTO> combinedProducts = new ArrayList<>();

		for (ProductDTO product : Stream
				.concat(Stream.concat(topProducts.stream(), wishlistProducts.stream()),
						Stream.concat(productsFromCart.stream(), productsFromOrder.stream()))
				.collect(Collectors.toList())) {
			if (uniqueProductIds.add(product.getId())) {
				combinedProducts.add(product);
			}
		}
		Collections.shuffle(combinedProducts);
		return combinedProducts.stream().limit(24).collect(Collectors.toList());
	}

	private List<ProductDTO> getRandomProducts(List<ProductDTO> productList, int limit) {
		Collections.shuffle(productList);
		return productList.stream().limit(limit).collect(Collectors.toList());
	}

	public List<ProductDTO> getProductCart(List<Product> prods) {
		List<ProductDTO> productDTOs = new ArrayList<>();

		for (Product pro : prods) {

			ProductDTO productDTO = new ProductDTO();
			productDTO.setId(String.valueOf(pro.getProductId()));
			productDTO.setName(pro.getProductName());

			BigDecimal minPrice = null;
			BigDecimal maxPrice = BigDecimal.ZERO;
			List<String> images = new ArrayList<>();

			for (ProductVersion productVer : pro.getProductVersions()) {

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
			productDTO.setImgName(images.isEmpty() ? null : images.get(0));
			productDTO.setImages(images);
			productDTO.setLike(true);

			productDTOs.add(productDTO);
		}
		return productDTOs;
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

	public List<String> getListAttName() {
		List<String> attName = new ArrayList<String>();
		for (AttributeOption attOp : attributeOptionJPA.findAll()) {
			attName.add(attOp.getAttributeValue());
		}
		return attName;

	}

	public List<Integer> getListAttId() {
		List<Integer> attName = new ArrayList<Integer>();
		for (AttributeOption attOp : attributeOptionJPA.findAll()) {
			attName.add(attOp.getId());
		}
		return attName;
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
