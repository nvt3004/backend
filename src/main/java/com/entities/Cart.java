package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;


/**
 * The persistent class for the carts database table.
 * 
 */
@Entity
@Table(name="carts")
@NamedQuery(name="Cart.findAll", query="SELECT c FROM Cart c")
public class Cart implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="cart_id")
	private int cartId;

	//bi-directional many-to-one association to CartProduct
	@OneToMany(mappedBy="cart")
	@JsonManagedReference("cart-cartProducts")
	private List<CartProduct> cartProducts;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="user_id")
	@JsonBackReference("user-carts")
	private User user;

	public Cart() {
	}

	public int getCartId() {
		return this.cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public List<CartProduct> getCartProducts() {
		return this.cartProducts;
	}

	public void setCartProducts(List<CartProduct> cartProducts) {
		this.cartProducts = cartProducts;
	}

	public CartProduct addCartProduct(CartProduct cartProduct) {
		getCartProducts().add(cartProduct);
		cartProduct.setCart(this);

		return cartProduct;
	}

	public CartProduct removeCartProduct(CartProduct cartProduct) {
		getCartProducts().remove(cartProduct);
		cartProduct.setCart(null);

		return cartProduct;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}