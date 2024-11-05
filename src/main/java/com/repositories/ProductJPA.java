package com.repositories;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Product;
import com.responsedto.ProductDTO;

public interface ProductJPA extends JpaRepository<Product, Integer> {
	@Query("SELECT o "
			+ "FROM Product o "
			+ "WHERE o.status=:status "
			+ "AND o.productName LIKE:keyword")
	public Page<Product> getAllProductByKeyword(@Param("status") boolean status,@Param("keyword") String keyword, Pageable pageable);
	
	@Query("SELECT DISTINCT o "
	        + "FROM Product o "
	        + "JOIN o.productCategories pc "
	        + "JOIN pc.category c "
	        + "WHERE o.status = :status "
	        + "AND o.productName LIKE %:keyword% "
	        + "AND c.categoryId = :idCat")
	public Page<Product> getAllProductByKeywordAndCategory(@Param("status") boolean status,@Param("keyword") String keyword,@Param("idCat") int idCat, Pageable pageable);
}
