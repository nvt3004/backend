package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;


/**
 * The persistent class for the cart_products database table.
 * 
 */
@Entity
@Table(name="cart_products")
@NamedQuery(name="CartProduct.findAll", query="SELECT c FROM CartProduct c")
public class CartProduct implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="cart_prd_id")
	private int cartPrdId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="added_date")
	private Date addedDate;

	private int quantity;

	//bi-directional many-to-one association to Cart
	@ManyToOne
	@JoinColumn(name="cart_id")
	@JsonBackReference("cart-cartProducts")
	private Cart cart;

	//bi-directional many-to-one association to ProductVersion
	@ManyToOne
	@JoinColumn(name="product_version")
	@JsonBackReference("productVersionBean-productVersion")
	private ProductVersion productVersionBean;

	public CartProduct() {
	}

	public int getCartPrdId() {
		return this.cartPrdId;
	}

	public void setCartPrdId(int cartPrdId) {
		this.cartPrdId = cartPrdId;
	}

	public Date getAddedDate() {
		return this.addedDate;
	}

	public void setAddedDate(Date addedDate) {
		this.addedDate = addedDate;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Cart getCart() {
		return this.cart;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public ProductVersion getProductVersionBean() {
		return this.productVersionBean;
	}

	public void setProductVersionBean(ProductVersion productVersionBean) {
		this.productVersionBean = productVersionBean;
	}

}