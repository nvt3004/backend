package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.CartProduct;
import com.entities.ProductVersion;

public interface CartProductJPA extends JpaRepository<CartProduct, Integer> {
	@Query("SELECT o FROM CartProduct o WHERE o.productVersionBean.id =:versionId AND o.cart.user.userId=:userId AND o.cartPrdId !=:cartItemId")
	public CartProduct getVersionInCartByUserUp(@Param("versionId") int versionId, @Param("userId") int userId, @Param("cartItemId") int cartItemId);
	
	@Query("SELECT o FROM CartProduct o WHERE o.productVersionBean.id =:versionId AND o.cart.user.userId=:userId")
	public CartProduct getVersionInCartByUser(@Param("versionId") int versionId, @Param("userId") int userId);
}
