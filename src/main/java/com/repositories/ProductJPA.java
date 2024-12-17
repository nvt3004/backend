package com.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Product;
import com.responsedto.ReportProductInventoryResponse;

public interface ProductJPA extends JpaRepository<Product, Integer> {
	@Query("SELECT o " + "FROM Product o " + "WHERE o.status=:status " + "AND o.productName LIKE:keyword")
	public Page<Product> getAllProductByKeyword(@Param("status") boolean status, @Param("keyword") String keyword,
			Pageable pageable);

	@Query("SELECT o " + "FROM Product o " + "WHERE o.status=:status " + "AND o.productName LIKE:keyword "
			+ "ORDER BY o.productId DESC")
	public List<Product> getAllProductByKeyword(@Param("status") boolean status, @Param("keyword") String keyword);

	@Query("SELECT DISTINCT o " + "FROM Product o " + "JOIN o.productCategories pc " + "JOIN pc.category c "
			+ "WHERE o.status = :status " + "AND o.productName LIKE %:keyword% " + "AND c.categoryId = :idCat")
	public Page<Product> getAllProductByKeywordAndCategory(@Param("status") boolean status,
			@Param("keyword") String keyword, @Param("idCat") int idCat, Pageable pageable);

	@Query(value = """
			    SELECT
			        vs.version_name,
			        vs.quantity - SUM(IF(ISNULL(od.order_id), 0, odt.quantity)) AS quantity
			    FROM
			        product_version vs
			    LEFT JOIN
			        order_details odt ON odt.product_version = vs.id
			    LEFT JOIN
			        orders od ON od.order_id = odt.order_id
			               AND od.status_id NOT IN (1, 6, 5)  -- Trạng thái tạm, chờ xác nhận, hủy
			    WHERE
			        vs.status = true
			    GROUP BY
			        vs.id, vs.quantity
			    ORDER BY
			        vs.quantity - SUM(IF(ISNULL(od.order_id), 0, odt.quantity)) DESC
			    LIMIT 5
			""", nativeQuery = true)
	List<Object[]> getTopProductsWithHighestStock();

	@Query("SELECT o FROM Product o ORDER BY o.productId DESC")
	public List<Product> getTopProducts(Pageable pageable);

//Đổi lại thành nút tra cứu hêt, phân trang đổi lại thành tiếng việt
	// Chứng chỉ tin học, ngoại ngữ

	@Query("SELECT DISTINCT p FROM Product p " +
		       "LEFT JOIN p.productCategories pc " +
		       "LEFT JOIN p.productVersions pv " +
		       "LEFT JOIN pv.attributeOptionsVersions aov " +
		       "WHERE (:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
		       "AND (:categoryID IS NULL OR pc.category.categoryId = :categoryID OR pc IS NULL) " +
		       "AND (:minPrice IS NULL OR pv.retailPrice >= :minPrice OR pv IS NULL) " +
		       "AND (:maxPrice IS NULL OR pv.retailPrice <= :maxPrice OR pv IS NULL) " +
		       "AND (:attributeIds IS NULL OR aov.attributeOption.id IN :attributeIds OR aov IS NULL) " +
		       "AND p.status = true")
		List<Product> findProducts(@Param("search") String search, 
		                           @Param("categoryID") Integer categoryID,
		                           @Param("attributeIds") List<Integer> attributeIds, 
		                           @Param("minPrice") BigDecimal minPrice,
		                           @Param("maxPrice") BigDecimal maxPrice);


}
