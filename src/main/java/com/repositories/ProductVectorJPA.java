package com.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.ProductVector;

public interface ProductVectorJPA extends JpaRepository<ProductVector, Integer> {

	// CREATE FULLTEXT INDEX idx_image_vector_fulltext
	// ON product_vector(image_vector);
	// chạy câu lệnh trên trong db
	@Query(value = "SELECT * FROM product_vector pv WHERE TRIM(pv.image_vector) LIKE TRIM(CONCAT('%', :keyword, '%'))", nativeQuery = true)
	List<ProductVector> getProductVectorByVector(@Param("keyword") String keyword);

}
