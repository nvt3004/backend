package com.repositories;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Coupon;
import com.entities.UserCoupon;

public interface UserCouponJPA extends JpaRepository<UserCoupon, Integer> {

	@Query("SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END FROM UserCoupon uc WHERE uc.coupon.id = :couponId")
	boolean existsByCouponId(@Param("couponId") Integer couponId);

	@Query("SELECT o.coupon FROM UserCoupon o WHERE o.user.userId=:userId" +  " AND o.status = true")
	List<Coupon> findAllCouponByUser(@Param("userId") int userId);

	@Query("SELECT o FROM UserCoupon o WHERE o.coupon.couponId=:id AND o.user.userId=:userId "
			+ "AND CURRENT_TIMESTAMP >= o.coupon.startDate " + "AND CURRENT_TIMESTAMP <= o.coupon.endDate "
			+ "AND o.coupon.quantity > 0 " + "AND o.coupon.status = true")
	UserCoupon findUsercouponByCoupon(@Param("id") int id, @Param("userId") int userId);
	
	@Query("SELECT o FROM Coupon o WHERE o.couponCode=:code "
			+ "AND CURRENT_TIMESTAMP >= o.startDate " + "AND CURRENT_TIMESTAMP <= o.endDate "
			+ "AND o.quantity > 0 " + "AND o.status = true")
	Coupon findCouponByCode(@Param("code") String code);
	
	@Query("SELECT COUNT(o) FROM UserCoupon o WHERE o.status = true AND o.coupon.couponId =:couponId")
	Integer countQuatityCoupon(@Param("couponId") Integer id);

	@Query("SELECT o FROM Coupon o WHERE o.couponCode=:code "
			+ "AND  o.startDate <=:dateNow " + "AND  o.endDate >=:dateNow "
			+ "AND o.quantity > 0 " + "AND o.status = true")
	Coupon findCouponByCode(@Param("code") String code, LocalDateTime dateNow);
}