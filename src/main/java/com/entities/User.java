package com.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * The persistent class for the users database table.
 */
@Entity
@Table(name = "users")
@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
public class User implements UserDetails, Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private int userId;

	@Temporal(TemporalType.DATE)
	private Date birthday;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_date")
	private Date createDate;

	private String email;

	@Column(name = "full_name")
	private String fullName;

	private int gender;

	private String image;

	private String password;

	private String phone;

	private byte status;

	private String username;

    @Column(name = "provider")
    private String provider;
    
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

	@Column(name = "resetCode")
	private String ResetCode;

	public String getResetCode() {
		return ResetCode;
	}

	public void setResetCode(String resetCode) {
		ResetCode = resetCode;
	}

	public Date getResetCodeExpiration() {
		return ResetCodeExpiration;
	}

	public void setResetCodeExpiration(Date resetCodeExpiration) {
		ResetCodeExpiration = resetCodeExpiration;
	}

	@Column(name = "resetcodeexpiration")
	private Date ResetCodeExpiration;

	// Bi-directional many-to-one association to Address
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-addresses")
	private List<Address> addresses;

	// Bi-directional many-to-one association to Cart
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-carts")
	private List<Cart> carts;

	// Bi-directional many-to-one association to Feedback
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-feedbacks")
	private List<Feedback> feedbacks;

	// Bi-directional many-to-one association to Image
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-images")
	private List<Image> images;

	// Bi-directional many-to-one association to ManagePermission
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-managePermissions")
	private List<ManagePermission> managePermissions;

	// Bi-directional many-to-one association to Order
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-orders")
	private List<Order> orders;
	
	@OneToMany(mappedBy = "lastUpdatedBy")
	@JsonManagedReference("user-orders-update")
	private List<Order> orderUpdate;

	// Bi-directional many-to-one association to Reply
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-replies")
	private List<Reply> replies;

	// Bi-directional many-to-one association to UserCoupon
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-userCoupons")
	private List<UserCoupon> userCoupons;

	// Bi-directional many-to-one association to UserRole
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JsonManagedReference("user-userRoles")
	private List<UserRole> userRoles;

	// Bi-directional many-to-one association to Wishlist
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-wishlists")
	private List<Wishlist> wishlists;

	@OneToMany(mappedBy = "user")
	@JsonManagedReference("user-receipt")
	private List<Receipt> receipts;

	public List<Receipt> getReceipts() {
		return receipts;
	}

	public void setReceipts(List<Receipt> receipts) {
		this.receipts = receipts;
	}
	
	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public User() {
	}

	// Getters and Setters
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	// public String getPassword() {
	// return password;
	// }

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	// public String getUsername() {
	// return username;
	// }

	public void setUsername(String username) {
		this.username = username;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	public Address addAddress(Address address) {
		getAddresses().add(address);
		address.setUser(this);
		return address;
	}

	public Address removeAddress(Address address) {
		getAddresses().remove(address);
		address.setUser(null);
		return address;
	}

	public List<Cart> getCarts() {
		return carts;
	}

	public void setCarts(List<Cart> carts) {
		this.carts = carts;
	}

	public Cart addCart(Cart cart) {
		getCarts().add(cart);
		cart.setUser(this);
		return cart;
	}

	public Cart removeCart(Cart cart) {
		getCarts().remove(cart);
		cart.setUser(null);
		return cart;
	}

	public List<Feedback> getFeedbacks() {
		return feedbacks;
	}

	public void setFeedbacks(List<Feedback> feedbacks) {
		this.feedbacks = feedbacks;
	}

	public Feedback addFeedback(Feedback feedback) {
		getFeedbacks().add(feedback);
		feedback.setUser(this);
		return feedback;
	}

	public Feedback removeFeedback(Feedback feedback) {
		getFeedbacks().remove(feedback);
		feedback.setUser(null);
		return feedback;
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public Image addImage(Image image) {
		getImages().add(image);
		image.setUser(this);
		return image;
	}

	public Image removeImage(Image image) {
		getImages().remove(image);
		image.setUser(null);
		return image;
	}

	public List<ManagePermission> getManagePermissions() {
		return managePermissions;
	}

	public void setManagePermissions(List<ManagePermission> managePermissions) {
		this.managePermissions = managePermissions;
	}

	public ManagePermission addManagePermission(ManagePermission managePermission) {
		getManagePermissions().add(managePermission);
		managePermission.setUser(this);
		return managePermission;
	}

	public ManagePermission removeManagePermission(ManagePermission managePermission) {
		getManagePermissions().remove(managePermission);
		managePermission.setUser(null);
		return managePermission;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public Order addOrder(Order order) {
		getOrders().add(order);
		order.setUser(this);
		return order;
	}

	public Order removeOrder(Order order) {
		getOrders().remove(order);
		order.setUser(null);
		return order;
	}

	public List<Reply> getReplies() {
		return replies;
	}

	public void setReplies(List<Reply> replies) {
		this.replies = replies;
	}

	public Reply addReply(Reply reply) {
		getReplies().add(reply);
		reply.setUser(this);
		return reply;
	}

	public Reply removeReply(Reply reply) {
		getReplies().remove(reply);
		reply.setUser(null);
		return reply;
	}

	public List<UserCoupon> getUserCoupons() {
		return userCoupons;
	}

	public void setUserCoupons(List<UserCoupon> userCoupons) {
		this.userCoupons = userCoupons;
	}

	public UserCoupon addUserCoupon(UserCoupon userCoupon) {
		getUserCoupons().add(userCoupon);
		userCoupon.setUser(this);
		return userCoupon;
	}

	public UserCoupon removeUserCoupon(UserCoupon userCoupon) {
		getUserCoupons().remove(userCoupon);
		userCoupon.setUser(null);
		return userCoupon;
	}

	public List<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	public UserRole addUserRole(UserRole userRole) {
		getUserRoles().add(userRole);
		userRole.setUser(this);
		return userRole;
	}

	public UserRole removeUserRole(UserRole userRole) {
		getUserRoles().remove(userRole);
		userRole.setUser(null);
		return userRole;
	}

	public List<Wishlist> getWishlists() {
		return wishlists;
	}

	public void setWishlists(List<Wishlist> wishlists) {
		this.wishlists = wishlists;
	}

	public Wishlist addWishlist(Wishlist wishlist) {
		getWishlists().add(wishlist);
		wishlist.setUser(this);
		return wishlist;
	}

	public Wishlist removeWishlist(Wishlist wishlist) {
		getWishlists().remove(wishlist);
		wishlist.setUser(null);
		return wishlist;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Optional.ofNullable(userRoles).orElse(Collections.emptyList()).stream()
				.map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getRoleName()))
				.collect(Collectors.toList());
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public boolean isAccountNonExpired() {
		// Implement as necessary
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// Implement as necessary
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// Implement as necessary
		return true;
	}

	@Override
	public boolean isEnabled() {
		// Implement as necessary
		return status == 1;
	}
}
