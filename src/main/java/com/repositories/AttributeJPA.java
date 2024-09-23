package com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Attribute;
import com.entities.AttributeOption;

public interface AttributeJPA extends JpaRepository<Attribute, Integer> {

	@Query("SELECT DISTINCT o.attributeOption FROM AttributeOptionsVersion o WHERE o.productVersion.product.productId =:productId")
	public List<AttributeOption> getAttributeByProduct(@Param("productId") int productId);
}
