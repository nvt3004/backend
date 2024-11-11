package com.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Category;

public interface CategoryJPA extends JpaRepository<Category, Integer> {
	@Query("SELECT o.category FROM ProductCategory o WHERE  o.product.productId=:productId")
	public List<Category> getAllCategoryByProduct(@Param("productId") int productId);
	
	@Query("SELECT o FROM Category o WHERE o.categoryName =:name")
	public Category getCategoryByName(@Param("name") String name);
	
	@Query("SELECT o FROM Category o WHERE o.categoryName LIKE:keyword")
	public Page<Category> getAllCategoryByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
