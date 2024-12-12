package com.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "product_vector")
public class ProductVector {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // ID tự tăng
	@Column(name = "id")
	private Integer id;

	@Column(name = "product_id", nullable = false)
	private Integer productId;

	@Column(name = "image_vector", nullable = false, columnDefinition = "TEXT")
	private String imageVector;

	// Constructors
	public ProductVector() {
	}

	public ProductVector(Integer productId, String imageVector) {
		this.productId = productId;
		this.imageVector = imageVector;
	}

	// Getters and Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getImageVector() {
		return imageVector;
	}

	public void setImageVector(String imageVector) {
		this.imageVector = imageVector;
	}
}
