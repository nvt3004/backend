package com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Cart;
import com.entities.CartProduct;

public interface CartJPA extends JpaRepository<Cart, Integer> {
	@Query("SELECT o FROM Cart o WHERE o.user.userId=:userId")
	public Cart getCartByUser(@Param("userId") int userId);
	
	@Query("SELECT o FROM CartProduct o WHERE o.cart.user.userId =:userId")
	public List<CartProduct> getAllCartItemByUser(@Param("userId") int userId);
}
