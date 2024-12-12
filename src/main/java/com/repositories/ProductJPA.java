package com.repositories;

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

	@Query("SELECT o " + "FROM Product o " + "WHERE o.status=:status " + "AND o.productName LIKE:keyword "+"ORDER BY o.productId DESC")
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

//Đổi lại thành nút tra cứu hêt, phân trang đổi lại thành tiếng việt
	//Chứng chỉ tin học, ngoại ngữ
}
