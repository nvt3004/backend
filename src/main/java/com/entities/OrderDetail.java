package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;


/**
 * The persistent class for the order_details database table.
 * 
 */
@Entity
@Table(name="order_details")
@NamedQuery(name="OrderDetail.findAll", query="SELECT o FROM OrderDetail o")
public class OrderDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="order_detail_id")
	private int orderDetailId;

	private BigDecimal price;

	private int quantity;

	//bi-directional many-to-one association to Order
	@ManyToOne
	@JoinColumn(name="order_id")
	@JsonBackReference("order-orderDetails")
	private Order order;

	//bi-directional many-to-one association to ProductVersion
	@ManyToOne
	@JoinColumn(name="product_version")
	@JsonBackReference("productVersionBean-orderDetails")
	private ProductVersion productVersionBean;

	public OrderDetail() {
	}

	public int getOrderDetailId() {
		return this.orderDetailId;
	}

	public void setOrderDetailId(int orderDetailId) {
		this.orderDetailId = orderDetailId;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Order getOrder() {
		return this.order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public ProductVersion getProductVersionBean() {
		return this.productVersionBean;
	}

	public void setProductVersionBean(ProductVersion productVersionBean) {
		this.productVersionBean = productVersionBean;
	}

}