package com.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Coupon;
import com.entities.User;
import com.entities.UserCoupon;
import com.repositories.CouponJPA;
import com.repositories.UserCouponJPA;
import com.repositories.UserJPA;
import com.responsedto.UserCouponResponse;
import com.responsedto.UserResponseDTO;

@Service
public class UserCouponService {
	@Autowired
	UserCouponJPA userCouponJPA;

	@Autowired
	CouponJPA couponJPA;

	@Autowired
	UserJPA userJPA;

	public UserCoupon createUserCoupon(UserCoupon userCoupon) {
		return userCouponJPA.save(userCoupon);
	}

	public boolean deleteUserCoupon(UserCoupon userCoupon) {
		try {
			userCouponJPA.delete(userCoupon);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<UserCouponResponse> getAllCouponByUser(int userId) {
		List<Coupon> coupons = userCouponJPA.findAllCouponByUser(userId);

		return coupons.stream().map(this::createUserCouponResponse).collect(Collectors.toList());
	}

	private UserCouponResponse createUserCouponResponse(Coupon coupon) {
		UserCouponResponse response = new UserCouponResponse();

		response.setId(coupon.getCouponId());
		response.setCouponCode(coupon.getCouponCode());
		response.setPercent(coupon.getDisPercent());
		response.setPrice(coupon.getDisPrice());
		response.setStartDate(coupon.getStartDate().minusHours(7));
		response.setEndDate(coupon.getEndDate().minusHours(7));
		response.setQuantity(coupon.getQuantity());

		return response;
	}

	public boolean checkQuantityCoupon(Integer id) {
		Coupon coupon = couponJPA.findById(id).get();
		Integer couponUsed = userCouponJPA.countQuatityCoupon(id);

		return coupon.getQuantity() > couponUsed;
	}
}
