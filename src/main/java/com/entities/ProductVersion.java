package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "product_version")
@NamedQuery(name = "ProductVersion.findAll", query = "SELECT p FROM ProductVersion p")
public class ProductVersion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private int quantity;

	@Column(name = "import_price")
	private BigDecimal importPrice;

	@Column(name = "retail_price")
	private BigDecimal retailPrice;

	@Column(name = "wholesale_price")
	private BigDecimal wholesalePrice;

	@Column(name = "version_name")
	private String versionName;

	@OneToMany(mappedBy = "productVersion")
	@JsonManagedReference("productVersion-attributeOptionsVersions")
	private List<AttributeOptionsVersion> attributeOptionsVersions;

	@OneToMany(mappedBy = "productVersionBean")
	@JsonManagedReference("productVersionBean-productVersion")
	private List<CartProduct> cartProducts;

	@OneToMany(mappedBy = "productVersion")
	@JsonManagedReference("productVersion-images")
	private List<Image> images;

	@OneToMany(mappedBy = "productVersionBean")
	@JsonManagedReference("productVersionBean-orderDetails")
	private List<OrderDetail> orderDetails;

	@ManyToOne
	@JoinColumn(name = "product_id")
	@JsonBackReference("product-productVersions")
	private Product product;

	@OneToMany(mappedBy = "productVersion")
	@JsonManagedReference("product_version-receipt_detail")
	private List<ReceiptDetail> receiptDetail;

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

	public BigDecimal getImportPrice() {
		return this.importPrice;
	}

	public void setImportPrice(BigDecimal importPrice) {
		this.importPrice = importPrice;
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

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
}
