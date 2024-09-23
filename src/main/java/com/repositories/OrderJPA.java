package com.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.entities.Order;
import com.entities.OrderStatus;

public interface OrderJPA extends JpaRepository<Order, Integer> {

	@Query("SELECT o FROM Order o " + "WHERE (:isAdminOrder IS NULL OR o.isAdminOrder = :isAdminOrder) AND "
			+ "(:keyword IS NULL OR :keyword = '' OR (o.fullname LIKE CONCAT('%', :keyword, '%') "
			+ "OR o.address LIKE CONCAT('%', :keyword, '%') " + "OR o.phone LIKE CONCAT('%', :keyword, '%'))) AND "
			+ "(:statusId IS NULL OR o.orderStatus.statusId = :statusId) "
			+ "ORDER BY o.orderStatus.sortOrder ASC")
	Page<Order> findOrdersByCriteria(@Param("isAdminOrder") Boolean isAdminOrder, @Param("keyword") String keyword,
			@Param("statusId") Integer statusId, Pageable pageable);

	@Query("SELECT o FROM Order o " + "WHERE (:username IS NULL OR o.user.username = :username) AND "
			+ "(:keyword IS NULL OR :keyword = '' OR (o.fullname LIKE CONCAT('%', :keyword, '%') "
			+ "OR o.address LIKE CONCAT('%', :keyword, '%') " + "OR o.phone LIKE CONCAT('%', :keyword, '%'))) AND "
			+ "(:statusId IS NULL OR o.orderStatus.statusId = :statusId) " + "ORDER BY o.orderDate DESC")
	Page<Order> findOrdersByUsername(@Param("username") String username, @Param("keyword") String keyword,
			@Param("statusId") Integer statusId, Pageable pageable);

	@Transactional
	@Modifying
	@Query("UPDATE Order o SET o.orderStatus = :newOrderStatus WHERE o.orderId = :orderId")
	int updateOrderStatus(@Param("orderId") int orderId, @Param("newOrderStatus") OrderStatus newOrderStatus);

	@Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.coupon.id = :couponId")
	boolean existsByCouponId(@Param("couponId") Integer couponId);

	@Query("SELECT CASE WHEN COUNT(od) = 0 THEN true ELSE false END FROM OrderDetail od WHERE od.order.orderId = :orderId") 
	boolean existsByOrderDetail(@Param("orderId") Integer orderId);


}
