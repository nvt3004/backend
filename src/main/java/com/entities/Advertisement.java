package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * The persistent class for the advertisements database table.
 * 
 */
@Entity
@Table(name = "advertisements")
@NamedQuery(name = "Advertisement.findAll", query = "SELECT a FROM Advertisement a")
public class Advertisement implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "adv_id")
	private int advId;

	@Lob
	@Column(name = "adv_description")
	private String advDescription;

	@Column(name = "adv_name")
	private String advName;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate;

	// bi-directional many-to-one association to Image
	@OneToMany(mappedBy = "advertisement")
	@JsonManagedReference("advertisement-images")
	private List<Image> images;

	public Advertisement() {
	}

	public int getAdvId() {
		return this.advId;
	}

	public void setAdvId(int advId) {
		this.advId = advId;
	}

	public String getAdvDescription() {
		return this.advDescription;
	}

	public void setAdvDescription(String advDescription) {
		this.advDescription = advDescription;
	}

	public String getAdvName() {
		return this.advName;
	}

	public void setAdvName(String advName) {
		this.advName = advName;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public List<Image> getImages() {
		return this.images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public Image addImage(Image image) {
		getImages().add(image);
		image.setAdvertisement(this);

		return image;
	}

	public Image removeImage(Image image) {
		getImages().remove(image);
		image.setAdvertisement(null);

		return image;
	}

}