package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;


/**
 * The persistent class for the feedbacks database table.
 * 
 */
@Entity
@Table(name="feedbacks")
@NamedQuery(name="Feedback.findAll", query="SELECT f FROM Feedback f")
public class Feedback implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="feedback_id")
	private int feedbackId;

	@Lob
	private String comment;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="feedback_date")
	private Date feedbackDate;

	private int rating;

	//bi-directional many-to-one association to Product
	@ManyToOne
	@JoinColumn(name="product_id")
	@JsonBackReference("product-feedbacks")
	private Product product;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="user_id")
	@JsonBackReference("user-feedbacks")
	private User user;

	//bi-directional many-to-one association to Image
	@OneToMany(mappedBy="feedback")
	@JsonManagedReference("feedback-images")
	private List<Image> images;

	//bi-directional many-to-one association to Reply
	@OneToMany(mappedBy="feedback")
	@JsonManagedReference("feedback-replies")
	private List<Reply> replies;

	@ManyToOne
	@JoinColumn(name = "order_detail_id", nullable = false)
	@JsonBackReference("order-detail-feedbacks")
	private OrderDetail orderDetail;

	
	public Feedback() {
	}

	public int getFeedbackId() {
		return this.feedbackId;
	}

	public void setFeedbackId(int feedbackId) {
		this.feedbackId = feedbackId;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getFeedbackDate() {
		return this.feedbackDate;
	}

	public void setFeedbackDate(Date feedbackDate) {
		this.feedbackDate = feedbackDate;
	}

	public int getRating() {
		return this.rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public OrderDetail getOrderDetail() {
		return this.orderDetail;
	}

	public void setOrderDetail(OrderDetail orderDetail) {
		this.orderDetail = orderDetail;
	}
	
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<Image> getImages() {
		return this.images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public Image addImage(Image image) {
		getImages().add(image);
		image.setFeedback(this);

		return image;
	}

	public Image removeImage(Image image) {
		getImages().remove(image);
		image.setFeedback(null);

		return image;
	}

	public List<Reply> getReplies() {
		return this.replies;
	}

	public void setReplies(List<Reply> replies) {
		this.replies = replies;
	}

	public Reply addReply(Reply reply) {
		getReplies().add(reply);
		reply.setFeedback(this);

		return reply;
	}

	public Reply removeReply(Reply reply) {
		getReplies().remove(reply);
		reply.setFeedback(null);

		return reply;
	}

}