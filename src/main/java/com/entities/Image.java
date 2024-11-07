package com.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;


/**
 * The persistent class for the images database table.
 * 
 */
@Entity
@Table(name="images")
@NamedQuery(name="Image.findAll", query="SELECT i FROM Image i")
public class Image implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="image_id")
	private int imageId;

	@Column(name="image_url")
	private String imageUrl;

	//bi-directional many-to-one association to Advertisement
	@ManyToOne
	@JoinColumn(name="adv_id")
	@JsonBackReference("advertisement-images")
	private Advertisement advertisement;

	//bi-directional many-to-one association to Feedback
	@ManyToOne
	@JoinColumn(name="feedback_id")
	@JsonBackReference("feedback-images")
	private Feedback feedback;

	@OneToOne
    @JoinColumn(name = "product_version_id")
    @JsonBackReference("productVersion-image")
    private ProductVersion productVersion;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="user_id")
	@JsonBackReference("user-images")
	private User user;

	public Image() {
	}

	public int getImageId() {
		return this.imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Advertisement getAdvertisement() {
		return this.advertisement;
	}

	public void setAdvertisement(Advertisement advertisement) {
		this.advertisement = advertisement;
	}

	public Feedback getFeedback() {
		return this.feedback;
	}

	public void setFeedback(Feedback feedback) {
		this.feedback = feedback;
	}

	public ProductVersion getProductVersion() {
        return this.productVersion;
    }

    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}