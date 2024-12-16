package com.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.entities.ProductVersion;
import com.responsedto.StockQuantityDTO;
import org.springframework.transaction.annotation.Transactional;

public interface ProductVersionJPA extends JpaRepository<ProductVersion, Integer> {

	@Query("SELECT pv FROM ProductVersion pv " + "JOIN pv.product p " + "JOIN pv.attributeOptionsVersions aov "
			+ "JOIN aov.attributeOption ao " + "JOIN ao.attribute a " + "WHERE p.id = :productId " + "AND EXISTS ("
			+ "    SELECT 1 " + "    FROM pv.attributeOptionsVersions aov1 " + "    JOIN aov1.attributeOption ao1 "
			+ "    JOIN ao1.attribute a1 " + "    WHERE a1.attributeName = 'Color' AND ao1.id = :colorId " + ") "
			+ "AND EXISTS (" + "    SELECT 1 " + "    FROM pv.attributeOptionsVersions aov2 "
			+ "    JOIN aov2.attributeOption ao2 " + "    JOIN ao2.attribute a2 "
			+ "    WHERE a2.attributeName = 'Size' AND ao2.id = :sizeId " + ")")
	Optional<ProductVersion> findByProductAttributes(@Param("productId") Integer productId,
			@Param("colorId") Integer colorId, @Param("sizeId") Integer sizeId);

	@Query("SELECT pv FROM ProductVersion pv WHERE pv.id =:productVersionId")
	List<ProductVersion> findProductVersionById(@Param("productVersionId") Integer productVersionId);

	@Query(value = "SELECT o.productVersion FROM AttributeOptionsVersion o WHERE o.productVersion.product.productId =:idProduct AND o.attributeOption.attributeValue LIKE :attributeValue")
	public List<ProductVersion> getVersion(@Param("attributeValue") String attributeValue,
			@Param("idProduct") int idProduct);

	@Query("SELECT pv FROM ProductVersion pv WHERE pv.product.productId = :productId")
	List<ProductVersion> findByProductId(@Param("productId") Integer productId);

	@Query("SELECT SUM(od.quantity) FROM Order o JOIN o.orderDetails od JOIN od.productVersionBean pv WHERE o.orderStatus.statusName = 'Processed' AND pv.id = :productVersionId")
	Integer getTotalQuantityByProductVersionInProcessedOrders(@Param("productVersionId") Integer productVersionId);

	@Query("SELECT SUM(od.quantity) FROM Order o JOIN o.orderDetails od JOIN od.productVersionBean pv WHERE o.orderStatus.statusName = 'Cancelled' AND pv.id = :productVersionId")
	Integer getTotalQuantityByProductVersionInCancelledOrders(@Param("productVersionId") Integer productVersionId);

	@Query("SELECT SUM(od.quantity) FROM Order o JOIN o.orderDetails od JOIN od.productVersionBean pv WHERE o.orderStatus.statusName = 'Shipped' AND pv.id = :productVersionId")
	Integer getTotalQuantityByProductVersionInShippedOrders(@Param("productVersionId") Integer productVersionId);

	@Query("SELECT SUM(od.quantity) FROM Order o JOIN o.orderDetails od JOIN od.productVersionBean pv WHERE o.orderStatus.statusName = 'Delivered' AND pv.id = :productVersionId")
	Integer getTotalQuantityByProductVersionInDeliveredOrders(@Param("productVersionId") Integer productVersionId);

	@Query("SELECT SUM(od.quantity) FROM Order o JOIN o.orderDetails od JOIN od.productVersionBean pv WHERE o.orderStatus.statusName = 'Waitingforconfirmation' AND pv.id = :productVersionId")
	Integer getTotalQuantityByProductVersionInWaitingForConfirmationOrders(@Param("productVersionId") Integer productVersionId);

	@Procedure(name = "ProductVersion.rp_stock_quantity")
	Integer getTotalStockQuantityVersion(@Param("versionId") int versionId);
}
