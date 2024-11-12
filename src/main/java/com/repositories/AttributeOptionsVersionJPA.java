package com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.AttributeOptionsVersion;

public interface AttributeOptionsVersionJPA extends JpaRepository<AttributeOptionsVersion, Integer> {
	@Query("SELECT a FROM AttributeOptionsVersion a WHERE a.productVersion.id = :productId")
	List<AttributeOptionsVersion> findByProductVersionId(@Param("productId") int productId);

}
