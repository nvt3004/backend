package com.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;


/**
 * The persistent class for the attribute_options_version database table.
 * 
 */
@Entity
@Table(name="attribute_options_version")
@NamedQuery(name="AttributeOptionsVersion.findAll", query="SELECT a FROM AttributeOptionsVersion a")
public class AttributeOptionsVersion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	//bi-directional many-to-one association to AttributeOption
	@ManyToOne
	@JoinColumn(name="attribute_options_id")
	@JsonBackReference("attributeOption-attributeOptionsVersions")
	private AttributeOption attributeOption;

	//bi-directional many-to-one association to ProductVersion
	@ManyToOne
	@JoinColumn(name="version_id")
	@JsonBackReference("productVersion-attributeOptionsVersions")
	private ProductVersion productVersion;

	public AttributeOptionsVersion() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public AttributeOption getAttributeOption() {
		return this.attributeOption;
	}

	public void setAttributeOption(AttributeOption attributeOption) {
		this.attributeOption = attributeOption;
	}

	public ProductVersion getProductVersion() {
		return this.productVersion;
	}

	public void setProductVersion(ProductVersion productVersion) {
		this.productVersion = productVersion;
	}

}