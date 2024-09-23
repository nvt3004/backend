package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;


/**
 * The persistent class for the suppliers database table.
 * 
 */
@Entity
@Table(name="suppliers")
@NamedQuery(name="Supplier.findAll", query="SELECT s FROM Supplier s")
public class Supplier implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="supplier_id")
	private int supplierId;

	@Lob
	private String address;

	@Column(name="contact_name")
	private String contactName;

	private String email;

	private String phone;

	private Boolean status;

	@Column(name="supplier_name")
	private String supplierName;
	
	@OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
	@JsonManagedReference("supplier-receipt")
	@JsonIgnore
	private List<Receipt> receipts;
	public Supplier() {
	}

	public int getSupplierId() {
		return this.supplierId;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContactName() {
		return this.contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Boolean getStatus() {
		return this.status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getSupplierName() {
		return this.supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

}