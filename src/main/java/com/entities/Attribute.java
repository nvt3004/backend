package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;


/**
 * The persistent class for the attributes database table.
 * 
 */
@Entity
@Table(name="attributes")
@NamedQuery(name="Attribute.findAll", query="SELECT a FROM Attribute a")
public class Attribute implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name="attribute_name")
	private String attributeName;

	//bi-directional many-to-one association to AttributeOption
	@OneToMany(mappedBy="attribute")
	@JsonManagedReference("attribute-attributeOptions")
	private List<AttributeOption> attributeOptions;

	public Attribute() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public List<AttributeOption> getAttributeOptions() {
		return this.attributeOptions;
	}

	public void setAttributeOptions(List<AttributeOption> attributeOptions) {
		this.attributeOptions = attributeOptions;
	}

	public AttributeOption addAttributeOption(AttributeOption attributeOption) {
		getAttributeOptions().add(attributeOption);
		attributeOption.setAttribute(this);

		return attributeOption;
	}

	public AttributeOption removeAttributeOption(AttributeOption attributeOption) {
		getAttributeOptions().remove(attributeOption);
		attributeOption.setAttribute(null);

		return attributeOption;
	}

}