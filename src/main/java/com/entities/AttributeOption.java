package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;


/**
 * The persistent class for the attribute_options database table.
 * 
 */
@Entity
@Table(name="attribute_options")
@NamedQuery(name="AttributeOption.findAll", query="SELECT a FROM AttributeOption a")
public class AttributeOption implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name="attribute_value")
	private String attributeValue;

	//bi-directional many-to-one association to Attribute
	@ManyToOne
	@JsonBackReference("attribute-attributeOptions")
	private Attribute attribute;

	//bi-directional many-to-one association to AttributeOptionsVersion
	@OneToMany(mappedBy="attributeOption")
	@JsonManagedReference("attributeOption-attributeOptionsVersions")
	private List<AttributeOptionsVersion> attributeOptionsVersions;

	public AttributeOption() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAttributeValue() {
		return this.attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public Attribute getAttribute() {
		return this.attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public List<AttributeOptionsVersion> getAttributeOptionsVersions() {
		return this.attributeOptionsVersions;
	}

	public void setAttributeOptionsVersions(List<AttributeOptionsVersion> attributeOptionsVersions) {
		this.attributeOptionsVersions = attributeOptionsVersions;
	}

	public AttributeOptionsVersion addAttributeOptionsVersion(AttributeOptionsVersion attributeOptionsVersion) {
		getAttributeOptionsVersions().add(attributeOptionsVersion);
		attributeOptionsVersion.setAttributeOption(this);

		return attributeOptionsVersion;
	}

	public AttributeOptionsVersion removeAttributeOptionsVersion(AttributeOptionsVersion attributeOptionsVersion) {
		getAttributeOptionsVersions().remove(attributeOptionsVersion);
		attributeOptionsVersion.setAttributeOption(null);

		return attributeOptionsVersion;
	}

}