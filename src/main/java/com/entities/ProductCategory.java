package com.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

// image bỏ quan hệ pd với image, bỏ ảnh pd lớn
/**
 * The persistent class for the product_categories database table.
 * 
 */
@Entity
@Table(name="product_categories")
@NamedQuery(name="ProductCategory.findAll", query="SELECT p FROM ProductCategory p")
public class ProductCategory implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="prd_cat_id")
	private int prdCatId;

	//bi-directional many-to-one association to Category
	@ManyToOne
	@JoinColumn(name="category_id")
	@JsonBackReference("category-productCategories")
	private Category category;

	//bi-directional many-to-one association to Product
	@ManyToOne
	@JoinColumn(name="product_id")
	@JsonBackReference("product-productCategories")
	private Product product;

	public ProductCategory() {
	}

	public int getPrdCatId() {
		return this.prdCatId;
	}

	public void setPrdCatId(int prdCatId) {
		this.prdCatId = prdCatId;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

}