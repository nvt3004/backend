package com.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * The persistent class for the orders database table.
 * 
 */
@Entity
@Table(name = "orders")
@NamedQuery(name = "Order.findAll", query = "SELECT o FROM Order o")
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private int orderId;

	private String address;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "delivery_date")
	private Date deliveryDate;

	@Column(name = "dis_percent")
	private BigDecimal disPercent;

	@Column(name = "dis_price")
	private BigDecimal disPrice;

	private String fullname;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "order_date", nullable = false, updatable = false)
	private Date orderDate;

	@Column(name = "creator_is_admin")
	private Boolean isAdminOrder;

	private String phone;
	
	@Column(name = "shipping_fee")
	private BigDecimal shippingFee;

	// bi-directional many-to-one association to OrderDetail
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "order")
	@JsonManagedReference("order-orderDetails")
	private List<OrderDetail> orderDetails;

	// bi-directional many-to-one association to OrderStatus
	@ManyToOne
	@JoinColumn(name = "status_id")
	@JsonBackReference("orderStatus-orders")
	private OrderStatus orderStatus;

	@ManyToOne
	@JoinColumn(name = "coupon_id")
	@JsonBackReference("coupon-orders")
	private Coupon coupon;

	// bi-directional many-to-one association to Payment
	@OneToOne(mappedBy = "order",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference("order-payment")
	private Payment payment;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonBackReference("user-orders")
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "last_updated_by")
	@JsonBackReference("user-orders-update")
	private User lastUpdatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_updated_date")
	private Date lastUpdatedDate;

	public User getLastUpdatedBy() {
	    return lastUpdatedBy;
	}

	public void setLastUpdatedBy(User lastUpdatedBy) {
	    this.lastUpdatedBy = lastUpdatedBy;
	}

	public Date getLastUpdatedDate() {
	    return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
	    this.lastUpdatedDate = lastUpdatedDate;
	}


	public Order() {
	}

	public BigDecimal getShippingFee() {
		return this.shippingFee;
	}

	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = shippingFee;
	}

	public int getOrderId() {
		return this.orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Coupon getCoupon() {
		return this.coupon;
	}

	public void setCoupon(Coupon coupon) {
		this.coupon = coupon;
	}

	public Date getDeliveryDate() {
		return this.deliveryDate;
	}

	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
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

	public Boolean isCreator() {
		return this.isAdminOrder;
	}

	public void setIsCreator(Boolean isCreator) {
		this.isAdminOrder = isCreator;
	}

	public String getFullname() {
		return this.fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public Date getOrderDate() {
		return this.orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<OrderDetail> getOrderDetails() {
		return this.orderDetails;
	}

	public void setOrderDetails(List<OrderDetail> orderDetails) {
		this.orderDetails = orderDetails;
	}

	public OrderDetail addOrderDetail(OrderDetail orderDetail) {
		getOrderDetails().add(orderDetail);
		orderDetail.setOrder(this);

		return orderDetail;
	}

	public OrderDetail removeOrderDetail(OrderDetail orderDetail) {
		getOrderDetails().remove(orderDetail);
		orderDetail.setOrder(null);

		return orderDetail;
	}

	public OrderStatus getOrderStatus() {
		return this.orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Payment getPayments() {
		return this.payment;
	}

	public void setPayments(Payment payment) {
		this.payment = payment;
	}

}