package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Feedback;

public interface FeedbackJPA extends JpaRepository<Feedback, Integer> {
	@Query("SELECT COUNT(DISTINCT o.order.orderId) AS countBuy " + "FROM OrderDetail o "
			+ "WHERE o.productVersionBean.product.productId =:idProduct " + "AND o.order.user.userId =:idUser "
			+ "AND o.order.orderStatus.statusName LIKE 'Delivered'")
	public Integer countPurchased(@Param("idUser") int idUser, @Param("idProduct") int idProduct);

	@Query("SELECT COUNT(o) AS countUserFeedback " + "FROM Feedback o " + "WHERE o.user.userId=:idUser "
			+ "AND o.product.productId=:idProduct")
	public Integer countFeedback(@Param("idUser") int idUser,@Param("idProduct") int idProduct);
	
}
