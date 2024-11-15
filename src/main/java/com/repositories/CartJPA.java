package com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Cart;
import com.entities.CartProduct;
import com.entities.Product;

public interface CartJPA extends JpaRepository<Cart, Integer> {
	@Query("SELECT o FROM Cart o WHERE o.user.userId=:userId")
	public Cart getCartByUser(@Param("userId") int userId);
	
	@Query("SELECT o FROM CartProduct o WHERE o.cart.user.userId =:userId ORDER BY o.cartPrdId DESC")
	public List<CartProduct> getAllCartItemByUser(@Param("userId") int userId);
	
	@Query("SELECT p FROM Product p " +
		       "JOIN ProductVersion pv ON p.productId = pv.product.productId " +
		       "JOIN CartProduct cp ON pv.id = cp.productVersionBean.id " +
		       "JOIN Cart c ON cp.cart.cartId = c.cartId " +
		       "WHERE c.user.userId = :userId " +
		       "GROUP BY p.productId")
		public List<Product> getProductsByUserId(@Param("userId") int userId);

}
