package com.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.entities.OrderStatus;

public interface OrderStatusJPA extends JpaRepository<OrderStatus, Integer> {

	@Transactional
	@Modifying
	@Query("UPDATE Order o SET o.orderStatus = :newOrderStatus WHERE o.orderId = :orderId")
	int updateOrderStatus(@Param("orderId") int orderId, @Param("newOrderStatus") OrderStatus newOrderStatus);

	Optional<OrderStatus> findByStatusName(String statusName);

	@Query("SELECT s FROM OrderStatus s ORDER BY s.sortOrder ASC")
	List<OrderStatus> getStatusOrder();

	@Query("SELECT os FROM OrderStatus os WHERE LOWER(os.statusName) = LOWER(:statusName)")
	Optional<OrderStatus> findByStatusNameIgnoreCase(@Param("statusName") String statusName);
}
