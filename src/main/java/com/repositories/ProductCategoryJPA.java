package com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.entities.ProductCategory;

public interface ProductCategoryJPA extends JpaRepository<ProductCategory, Integer> {
	@Query("SELECT o FROM ProductCategory o WHERE  o.product.productId=:productId")
	public List<ProductCategory> getAllProductCategoryByProductId(@Param("productId") int productId);
	
	@Query("SELECT c FROM ProductCategory c WHERE c.product.productId = :productId")
	List<ProductCategory> findByProductId(@Param("productId") int productId);

}
