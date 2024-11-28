package com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.entities.OrderDetail;


public interface OrderDetailJPA extends JpaRepository<OrderDetail, Integer> {

	@Query("""
		    SELECT od 
		    FROM OrderDetail od 
		    WHERE od.order.orderId = :orderId
		""")
		List<OrderDetail> findByOrderDetailByOrderId(@Param("orderId") Integer orderId);

		@Modifying
		@Transactional
		@Query("""
		    DELETE FROM OrderDetail od 
		    WHERE od.orderDetailId = :orderDetailId
		""")
		int deleteOrderDetailsByOrderDetailId(@Param("orderDetailId") int orderDetailId);

}
