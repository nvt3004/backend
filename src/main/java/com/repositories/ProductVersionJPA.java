package com.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.ProductVersion;

public interface ProductVersionJPA extends JpaRepository<ProductVersion, Integer> {

	@Query("SELECT pv FROM ProductVersion pv " +
		       "JOIN pv.product p " +
		       "JOIN pv.attributeOptionsVersions aov " +
		       "JOIN aov.attributeOption ao " +
		       "JOIN ao.attribute a " +
		       "WHERE p.id = :productId " +
		       "AND EXISTS (" +
		       "    SELECT 1 " +
		       "    FROM pv.attributeOptionsVersions aov1 " +
		       "    JOIN aov1.attributeOption ao1 " +
		       "    JOIN ao1.attribute a1 " +
		       "    WHERE a1.attributeName = 'Color' AND ao1.id = :colorId " +
		       ") " +
		       "AND EXISTS (" +
		       "    SELECT 1 " +
		       "    FROM pv.attributeOptionsVersions aov2 " +
		       "    JOIN aov2.attributeOption ao2 " +
		       "    JOIN ao2.attribute a2 " +
		       "    WHERE a2.attributeName = 'Size' AND ao2.id = :sizeId " +
		       ")")
		Optional<ProductVersion> findByProductAttributes(@Param("productId") Integer productId,
		                                                 @Param("colorId") Integer colorId,
		                                                 @Param("sizeId") Integer sizeId);


	@Query("SELECT pv FROM ProductVersion pv WHERE pv.id =:productVersionId")
	List<ProductVersion> findProductVersionById(@Param("productVersionId") Integer productVersionId);

	@Query(value = "SELECT o.productVersion FROM AttributeOptionsVersion o WHERE o.productVersion.product.productId =:idProduct AND o.attributeOption.attributeValue LIKE :attributeValue")
	public List<ProductVersion> getVersion(@Param("attributeValue") String attributeValue,
			@Param("idProduct") int idProduct);

	@Query("SELECT pv FROM ProductVersion pv WHERE pv.product.productId = :productId")
	List<ProductVersion> findByProductId(@Param("productId") Integer productId);
}
