package com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Category;

public interface CategoryJPA extends JpaRepository<Category, Integer> {
	@Query("SELECT o.category FROM ProductCategory o WHERE  o.product.productId=:productId")
	public List<Category> getAllCategoryByProduct(@Param("productId") int productId);
}
