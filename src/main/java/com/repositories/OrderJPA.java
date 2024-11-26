package com.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.entities.Order;
import com.entities.OrderStatus;
import com.entities.Product;

public interface OrderJPA extends JpaRepository<Order, Integer> {

	@Query("""
		    SELECT o FROM Order o
		    WHERE (:keyword IS NULL OR :keyword = '' OR 
		           (o.fullname LIKE CONCAT('%', :keyword, '%') OR 
		            o.address LIKE CONCAT('%', :keyword, '%') OR 
		            o.phone LIKE CONCAT('%', :keyword, '%')))
		      AND (:statusId IS NULL OR o.orderStatus.statusId = :statusId)
		    ORDER BY o.orderStatus.sortOrder ASC, o.orderDate DESC
		    """)
		Page<Order> findOrdersByCriteria(
		    @Param("keyword") String keyword, 
		    @Param("statusId") Integer statusId, 
		    Pageable pageable);


	@Query("""
		    SELECT o FROM Order o
		    JOIN o.orderDetails od
		    JOIN od.productVersionBean pv
		    JOIN pv.product p
		    WHERE (:username IS NULL OR o.user.username = :username)
		      AND (:keyword IS NULL OR :keyword = '' OR 
		           (CAST(o.orderId AS string) LIKE CONCAT('%', :keyword, '%') OR 
		            p.productName LIKE CONCAT('%', :keyword, '%') OR 
		            o.address LIKE CONCAT('%', :keyword, '%')))
		      AND (:statusId IS NULL OR o.orderStatus.statusId = :statusId)
		    ORDER BY o.orderDate DESC
		    """)
		Page<Order> findOrdersByUsername(
		    @Param("username") String username, 
		    @Param("keyword") String keyword,
		    @Param("statusId") Integer statusId, 
		    Pageable pageable);

	@Transactional
	@Modifying
	@Query("UPDATE Order o SET o.orderStatus = :newOrderStatus WHERE o.orderId = :orderId")
	int updateOrderStatus(@Param("orderId") int orderId, @Param("newOrderStatus") OrderStatus newOrderStatus);

	@Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.coupon.id = :couponId")
	boolean existsByCouponId(@Param("couponId") Integer couponId);

	@Query("SELECT CASE WHEN COUNT(od) = 0 THEN true ELSE false END FROM OrderDetail od WHERE od.order.orderId = :orderId")
	boolean existsByOrderDetail(@Param("orderId") Integer orderId);

	@Query("SELECT p FROM Product p " +
		       "JOIN ProductVersion pv ON p.productId = pv.product.productId " +
		       "JOIN OrderDetail od ON pv.id = od.productVersionBean.id " +
		       "JOIN Order o ON o.orderId = od.order.orderId " +  // Thêm khoảng trống trước WHERE
		       "WHERE o.user.userId = :userId " +
		       "GROUP BY p.productId")
		public List<Product> getProductsByUserId(@Param("userId") int userId);
	@Query("SELECT o FROM Order o WHERE o.orderDate < :createdAt AND o.orderStatus.statusName = :statusName")
	List<Order> findAllByCreatedAtBeforeAndOrderStatusStatusName(@Param("createdAt") Date createdAt,
			@Param("statusName") String statusName);
	
	@Query("""
		       SELECT CASE WHEN COUNT(od) > 0 THEN true ELSE false END 
		       FROM Order o 
		       LEFT JOIN o.orderDetails od 
		       WHERE o.orderId = :orderId
		       """)
		boolean existsOrderDetailByOrderId(@Param("orderId") Integer orderId);


}
