package com.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Coupon;

public interface CouponJPA extends JpaRepository<Coupon, Integer> {

	@Query("""
		    SELECT c 
		    FROM Coupon c 
		    WHERE (:startDate IS NULL OR c.startDate >= :startDate) 
		      AND (:endDate IS NULL OR c.endDate <= :endDate) 
		      AND (:discountType IS NULL OR 
		           (CASE :discountType 
		               WHEN 'disPercent' THEN c.disPercent IS NOT NULL AND c.disPrice IS NULL 
		               WHEN 'disPrice' THEN c.disPrice IS NOT NULL AND c.disPercent IS NULL 
		               ELSE TRUE 
		           END)) 
		    ORDER BY c.endDate DESC, c.id DESC
		    """)
		Page<Coupon> findActiveCoupons(
		    @Param("startDate") LocalDateTime startDate, 
		    @Param("endDate") LocalDateTime endDate,
		    @Param("discountType") String discountType, 
		    Pageable pageable
		);

		@Query("""
		    SELECT o 
		    FROM Coupon o 
		    WHERE o.couponCode = :code
		    """)
		Coupon getCouponByCode(@Param("code") String code);

		@Query("""
		    SELECT o 
		    FROM Coupon o 
		    LEFT JOIN UserCoupon uc 
		        ON o.couponId = uc.coupon.couponId 
		       AND uc.user.userId = :userId 
		    WHERE o.startDate<=:dateNow AND o.endDate > :dateNow 
		      AND uc.coupon.couponId IS NULL AND o.quantity > 0 AND o.status =:status
		    """)
		List<Coupon> getCouponHomeByUser(
		    @Param("userId") Integer userId, 
		    @Param("dateNow") LocalDateTime dateNow,
			boolean status
		);

		@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Coupon c WHERE c.couponCode = :couponCode")
		boolean existsByCouponCode(@Param("couponCode") String couponCode);

}
