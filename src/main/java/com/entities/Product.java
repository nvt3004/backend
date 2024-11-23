package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "products")
@NamedQuery(name = "Product.findAll", query = "SELECT p FROM Product p")
public class Product implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private int productId;

	@Lob
	private String description;

	@Column(name = "product_img")
	private String productImg;

	@Column(name = "product_name")
	private String productName;

	@Column(name = "product_price")
	private BigDecimal productPrice;

	private Boolean status;

	@OneToMany(fetch = FetchType.EAGER,mappedBy = "product")
	@JsonManagedReference("product-feedbacks")
	private List<Feedback> feedbacks;

	@OneToMany(fetch = FetchType.EAGER,mappedBy = "product")
	@JsonManagedReference("product-productCategories")
	private List<ProductCategory> productCategories;

	@OneToMany(fetch = FetchType.EAGER,mappedBy = "product")
	@JsonManagedReference("product-productSales")
	private List<ProductSale> productSales;

	@OneToMany(fetch = FetchType.EAGER,mappedBy = "product")
	@JsonManagedReference("product-productVersions")
	private List<ProductVersion> productVersions;

	@OneToMany(mappedBy = "product")
	@JsonManagedReference("product-wishlists")
	private List<Wishlist> wishlists;

	public Product() {
	}

	public int getProductId() {
		return this.productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProductImg() {
		return this.productImg;
	}

	public void setProductImg(String productImg) {
		this.productImg = productImg;
	}

	public String getProductName() {
		return this.productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public BigDecimal getProductPrice() {
		return this.productPrice;
	}

	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}

	public boolean isStatus() {
		return this.status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public List<Feedback> getFeedbacks() {
		return this.feedbacks;
	}

	public void setFeedbacks(List<Feedback> feedbacks) {
		this.feedbacks = feedbacks;
	}

	public Feedback addFeedback(Feedback feedback) {
		getFeedbacks().add(feedback);
		feedback.setProduct(this);

		return feedback;
	}

	public Feedback removeFeedback(Feedback feedback) {
		getFeedbacks().remove(feedback);
		feedback.setProduct(null);

		return feedback;
	}

	public List<ProductCategory> getProductCategories() {
		return this.productCategories;
	}

	public void setProductCategories(List<ProductCategory> productCategories) {
		this.productCategories = productCategories;
	}

	public ProductCategory addProductCategory(ProductCategory productCategory) {
		getProductCategories().add(productCategory);
		productCategory.setProduct(this);

		return productCategory;
	}

	public ProductCategory removeProductCategory(ProductCategory productCategory) {
		getProductCategories().remove(productCategory);
		productCategory.setProduct(null);

		return productCategory;
	}

	public List<ProductSale> getProductSales() {
		return this.productSales;
	}

	public void setProductSales(List<ProductSale> productSales) {
		this.productSales = productSales;
	}

	public ProductSale addProductSale(ProductSale productSale) {
		getProductSales().add(productSale);
		productSale.setProduct(this);

		return productSale;
	}

	public ProductSale removeProductSale(ProductSale productSale) {
		getProductSales().remove(productSale);
		productSale.setProduct(null);

		return productSale;
	}

	public List<ProductVersion> getProductVersions() {
		return this.productVersions;
	}

	public void setProductVersions(List<ProductVersion> productVersions) {
		this.productVersions = productVersions;
	}

	public ProductVersion addProductVersion(ProductVersion productVersion) {
		getProductVersions().add(productVersion);
		productVersion.setProduct(this);

		return productVersion;
	}

	public ProductVersion removeProductVersion(ProductVersion productVersion) {
		getProductVersions().remove(productVersion);
		productVersion.setProduct(null);

		return productVersion;
	}

	public List<Wishlist> getWishlists() {
		return this.wishlists;
	}

	public void setWishlists(List<Wishlist> wishlists) {
		this.wishlists = wishlists;
	}

	public Wishlist addWishlist(Wishlist wishlist) {
		getWishlists().add(wishlist);
		wishlist.setProduct(this);

		return wishlist;
	}

	public Wishlist removeWishlist(Wishlist wishlist) {
		getWishlists().remove(wishlist);
		wishlist.setProduct(null);

		return wishlist;
	}

}
