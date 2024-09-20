package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;


/**
 * The persistent class for the categories database table.
 * 
 */
@Entity
@Table(name="categories")
@NamedQuery(name="Category.findAll", query="SELECT c FROM Category c")
public class Category implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="category_id")
	private int categoryId;

	@Column(name="category_name")
	private String categoryName;

	//bi-directional many-to-one association to ProductCategory
	@OneToMany(mappedBy="category")
	@JsonManagedReference("category-productCategories")
	private List<ProductCategory> productCategories;

	public Category() {
	}

	public int getCategoryId() {
		return this.categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public List<ProductCategory> getProductCategories() {
		return this.productCategories;
	}

	public void setProductCategories(List<ProductCategory> productCategories) {
		this.productCategories = productCategories;
	}

	public ProductCategory addProductCategory(ProductCategory productCategory) {
		getProductCategories().add(productCategory);
		productCategory.setCategory(this);

		return productCategory;
	}

	public ProductCategory removeProductCategory(ProductCategory productCategory) {
		getProductCategories().remove(productCategory);
		productCategory.setCategory(null);

		return productCategory;
	}

}