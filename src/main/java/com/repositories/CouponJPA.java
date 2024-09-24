package com.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Coupon;

public interface CouponJPA extends JpaRepository<Coupon, Integer> {

	@Query("SELECT c FROM Coupon c WHERE " + "(:startDate IS NULL OR c.startDate >= :startDate) AND "
			+ "(:endDate IS NULL OR c.endDate <= :endDate) AND " + "(:discountType IS NULL OR " + "(CASE :discountType "
			+ "WHEN 'disPercent' THEN c.disPercent IS NOT NULL AND c.disPrice IS NULL "
			+ "WHEN 'disPrice' THEN c.disPrice IS NOT NULL AND c.disPercent IS NULL " + "ELSE TRUE END)) "
			+ "ORDER BY c.endDate DESC")
	Page<Coupon> findActiveCoupons(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate,
			@Param("discountType") String discountType, Pageable pageable);

	boolean existsByCouponCode(String couponCode);

	@Query("SELECT o FROM Coupon o WHERE o.couponCode =:code")
	public Coupon getCouponByCode(@Param("code") String code);
}
