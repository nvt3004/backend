package com.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


/**
 * The persistent class for the coupons database table.
 * 
 */
@Entity
@Table(name = "coupons")
@NamedQuery(name = "Coupon.findAll", query = "SELECT c FROM Coupon c")
public class Coupon implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "coupon_id")
	private int couponId;

	@Column(name = "coupon_code")
	private String couponCode;
	
	@OneToMany(mappedBy = "coupon", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("coupon-orders")
	@JsonIgnore
	private List<Order> orders;

	@Lob
	private String description;

	@Column(name = "dis_percent")
	private BigDecimal disPercent;

	@Column(name = "dis_price")
	private BigDecimal disPrice;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	private int quantity;

	@Column(name = "start_date")
	private LocalDateTime startDate;
	
	private boolean status;
	
	@OneToMany(mappedBy="coupon")
	@JsonManagedReference("coupon-userCoupons")
	@JsonIgnore
	private List<UserCoupon> userCoupons;
	
	@Column(name = "created_date", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;
	
	public Coupon() {
	}

	public int getCouponId() {
		return this.couponId;
	}

	public void setCouponId(int couponId) {
		this.couponId = couponId;
	}

	public String getCouponCode() {
		return this.couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getDisPercent() {
		return this.disPercent;
	}

	public void setDisPercent(BigDecimal disPercent) {
		this.disPercent = disPercent;
	}

	public BigDecimal getDisPrice() {
		return this.disPrice;
	}

	public void setDisPrice(BigDecimal disPrice) {
		this.disPrice = disPrice;
	}

	public LocalDateTime getEndDate() {
		return this.endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public LocalDateTime getCreateDate() {
		return this.createdDate;
	}

	public void setCreateDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}
	
	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public LocalDateTime getStartDate() {
		return this.startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public List<UserCoupon> getUserCoupons() {
		return this.userCoupons;
	}

	public void setUserCoupons(List<UserCoupon> userCoupons) {
		this.userCoupons = userCoupons;
	}

	public UserCoupon addUserCoupon(UserCoupon userCoupon) {
		getUserCoupons().add(userCoupon);
		userCoupon.setCoupon(this);

		return userCoupon;
	}

	public UserCoupon removeUserCoupon(UserCoupon userCoupon) {
		getUserCoupons().remove(userCoupon);
		userCoupon.setCoupon(null);

		return userCoupon;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	

}