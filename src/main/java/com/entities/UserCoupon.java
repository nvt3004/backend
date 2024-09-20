package com.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;


/**
 * The persistent class for the user_coupons database table.
 * 
 */
@Entity
@Table(name="user_coupons")
@NamedQuery(name="UserCoupon.findAll", query="SELECT u FROM UserCoupon u")
public class UserCoupon implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="user_coupon_id")
	private int userCouponId;

	//bi-directional many-to-one association to Coupon
	@ManyToOne
	@JoinColumn(name="coupon_id")
	@JsonBackReference("coupon-userCoupons")
	private Coupon coupon;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="user_id")
	@JsonBackReference("user-userCoupons")
	private User user;

	public UserCoupon() {
	}

	public int getUserCouponId() {
		return this.userCouponId;
	}

	public void setUserCouponId(int userCouponId) {
		this.userCouponId = userCouponId;
	}

	public Coupon getCoupon() {
		return this.coupon;
	}

	public void setCoupon(Coupon coupon) {
		this.coupon = coupon;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}