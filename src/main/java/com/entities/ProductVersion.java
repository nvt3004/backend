package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;


/**
 * The persistent class for the product_version database table.
 * 
 */
@Entity
@Table(name="product_version")
@NamedQuery(name="ProductVersion.findAll", query="SELECT p FROM ProductVersion p")
public class ProductVersion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private int quantity;

	@Column(name="retail_price")
	private BigDecimal retailPrice;

	@Column(name="wholesale_price")
	private BigDecimal wholesalePrice;
	
	@Column(name = "version_name")
	private String versionName;
	
	@Column(name = "status")
	private boolean status;

	//bi-directional many-to-one association to AttributeOptionsVersion
	@OneToMany(mappedBy="productVersion")
	@JsonManagedReference

	private List<AttributeOptionsVersion> attributeOptionsVersions;

	//bi-directional many-to-one association to CartProduct
	@OneToMany(mappedBy="productVersionBean")
	@JsonManagedReference

	private List<CartProduct> cartProducts;

	//bi-directional many-to-one association to Image
	@OneToMany(mappedBy="productVersion")
	@JsonManagedReference

	private List<Image> images;

	//bi-directional many-to-one association to OrderDetail
	@OneToMany(mappedBy="productVersionBean")
	@JsonManagedReference

	private List<OrderDetail> orderDetails;

	//bi-directional many-to-one association to Product
	@ManyToOne
	@JoinColumn(name="product_id")
	@JsonManagedReference

	private Product product;

	//bi-directional many-to-one association to Warehous
	@OneToMany(mappedBy="productVersionBean")
	@JsonManagedReference

	private List<Warehous> warehouses;

	public ProductVersion() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getRetailPrice() {
		return this.retailPrice;
	}

	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}

	public BigDecimal getWholesalePrice() {
		return this.wholesalePrice;
	}

	public void setWholesalePrice(BigDecimal wholesalePrice) {
		this.wholesalePrice = wholesalePrice;
	}

	public List<AttributeOptionsVersion> getAttributeOptionsVersions() {
		return this.attributeOptionsVersions;
	}

	public void setAttributeOptionsVersions(List<AttributeOptionsVersion> attributeOptionsVersions) {
		this.attributeOptionsVersions = attributeOptionsVersions;
	}

	public AttributeOptionsVersion addAttributeOptionsVersion(AttributeOptionsVersion attributeOptionsVersion) {
		getAttributeOptionsVersions().add(attributeOptionsVersion);
		attributeOptionsVersion.setProductVersion(this);

		return attributeOptionsVersion;
	}

	public AttributeOptionsVersion removeAttributeOptionsVersion(AttributeOptionsVersion attributeOptionsVersion) {
		getAttributeOptionsVersions().remove(attributeOptionsVersion);
		attributeOptionsVersion.setProductVersion(null);

		return attributeOptionsVersion;
	}

	public List<CartProduct> getCartProducts() {
		return this.cartProducts;
	}

	public void setCartProducts(List<CartProduct> cartProducts) {
		this.cartProducts = cartProducts;
	}

	public CartProduct addCartProduct(CartProduct cartProduct) {
		getCartProducts().add(cartProduct);
		cartProduct.setProductVersionBean(this);

		return cartProduct;
	}

	public CartProduct removeCartProduct(CartProduct cartProduct) {
		getCartProducts().remove(cartProduct);
		cartProduct.setProductVersionBean(null);

		return cartProduct;
	}

	public List<Image> getImages() {
		return this.images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public Image addImage(Image image) {
		getImages().add(image);
		image.setProductVersion(this);

		return image;
	}

	public Image removeImage(Image image) {
		getImages().remove(image);
		image.setProductVersion(null);

		return image;
	}

	public List<OrderDetail> getOrderDetails() {
		return this.orderDetails;
	}

	public void setOrderDetails(List<OrderDetail> orderDetails) {
		this.orderDetails = orderDetails;
	}

	public OrderDetail addOrderDetail(OrderDetail orderDetail) {
		getOrderDetails().add(orderDetail);
		orderDetail.setProductVersionBean(this);

		return orderDetail;
	}

	public OrderDetail removeOrderDetail(OrderDetail orderDetail) {
		getOrderDetails().remove(orderDetail);
		orderDetail.setProductVersionBean(null);

		return orderDetail;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public List<Warehous> getWarehouses() {
		return this.warehouses;
	}

	public void setWarehouses(List<Warehous> warehouses) {
		this.warehouses = warehouses;
	}

	public Warehous addWarehous(Warehous warehous) {
		getWarehouses().add(warehous);
		warehous.setProductVersionBean(this);

		return warehous;
	}

	public Warehous removeWarehous(Warehous warehous) {
		getWarehouses().remove(warehous);
		warehous.setProductVersionBean(null);

		return warehous;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

}