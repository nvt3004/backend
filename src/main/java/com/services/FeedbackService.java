package com.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Feedback;
import com.entities.User;
import com.repositories.FeedbackJPA;
import com.responsedto.FeedbackResponseDTO;
import com.responsedto.PageCustom;
import com.responsedto.ReplyResponseDTO;
import com.responsedto.UserResponseDTO;
import com.utils.UploadService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Service
public class FeedbackService {
	@Autowired
	FeedbackJPA feedbackJPA;
	
	@Autowired
	private EntityManager entityManager;

	@Autowired
	UploadService uploadService;
	
	public Feedback createFeedback(Feedback feedback) {
		return feedbackJPA.save(feedback);
	}
	
	public int getCountProductPurchased(int userId, int productId) {
		return feedbackJPA.countPurchased(userId, productId);
	}
	
	public int getCountFeedbackOfUserByProduct(int userId, int productId) {
		return feedbackJPA.countFeedback(userId, productId);
	}
	

	public PageCustom<FeedbackResponseDTO> findFeedbackByCondition(Date startDate, Date endDate, int idProduct,
			int page, int pageSize) {

		StringBuilder jpql = new StringBuilder("SELECT o FROM Feedback o WHERE 1=1");

		if (startDate != null && endDate != null) {
			jpql.append(" AND o.feedbackDate >= :startDate AND o.feedbackDate <= :endDate");
		}

		if (idProduct != -1) {
			jpql.append(" AND o.product.productId = :idProduct");
		}

		jpql.append(" ORDER BY o.feedbackDate DESC");

		TypedQuery<Feedback> query = entityManager.createQuery(jpql.toString(), Feedback.class);

		if (startDate != null && endDate != null) {
			query.setParameter("startDate", startDate);
			query.setParameter("endDate", endDate);
		}

		if (idProduct != -1) {
			query.setParameter("idProduct", idProduct);
		}

		//&& pageSize > 0
		if (page > 0) {
			query.setFirstResult((page - 1) * pageSize);
			query.setMaxResults(pageSize);
		}else {
			query.setFirstResult(0);
			query.setMaxResults(pageSize);
		}

		List<Feedback> feedbacks = query.getResultList();

		List<FeedbackResponseDTO> feedbackResponses = feedbacks.stream().map(fb -> {

			FeedbackResponseDTO feedbackResponseDTO = new FeedbackResponseDTO();
			UserResponseDTO userResponseDTO = new UserResponseDTO();

			String avarta = uploadService.getUrlImage(fb.getUser().getImage());
			userResponseDTO.setImage(avarta);
			userResponseDTO.setFullName(fb.getUser().getFullName());
			userResponseDTO.setId(fb.getUser().getUserId());

			if (fb.getReplies() != null && !fb.getReplies().isEmpty()) {
				User user = fb.getReplies().get(0).getUser();

				ReplyResponseDTO replyResponseDTO = new ReplyResponseDTO();
				String tempImg = uploadService.getUrlImage(user.getImage());
				replyResponseDTO.setReplyId(fb.getReplies().get(0).getId());
				replyResponseDTO.setContent(fb.getReplies().get(0).getContent());
				replyResponseDTO.setReplyDate(fb.getReplies().get(0).getReplyDate());
				replyResponseDTO.setUserReply(new UserResponseDTO(user.getUserId(), user.getFullName(), tempImg));
				feedbackResponseDTO.setReply(replyResponseDTO);
			}

			feedbackResponseDTO.setFeedbackId(fb.getFeedbackId());
			feedbackResponseDTO.setComment(fb.getComment());
			feedbackResponseDTO.setProductId(fb.getProduct().getProductId());
			feedbackResponseDTO.setProductName(fb.getProduct().getProductName());
			feedbackResponseDTO.setFeedbackDate(fb.getFeedbackDate());
			feedbackResponseDTO.setRating(fb.getRating());

			if (fb.getImages() != null && fb.getImages().size() > 0) {
				feedbackResponseDTO.setImages(fb.getImages().stream().map(img -> {
					return uploadService.getUrlImage(img.getImageUrl());
				}).collect(Collectors.toList()));
			}
			feedbackResponseDTO.setUser(userResponseDTO);

			return feedbackResponseDTO;
		}).collect(Collectors.toList());

		StringBuilder countJpql = new StringBuilder("SELECT COUNT(o) FROM Feedback o WHERE 1=1");

		if (startDate != null && endDate != null) {
			countJpql.append(" AND o.feedbackDate >= :startDate AND o.feedbackDate <= :endDate");
		}

		if (idProduct != -1) {
			countJpql.append(" AND o.product.productId = :idProduct");
		}

		TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);

		if (startDate != null && endDate != null) {
			countQuery.setParameter("startDate", startDate);
			countQuery.setParameter("endDate", endDate);
		}

		if (idProduct != -1) {
			countQuery.setParameter("idProduct", idProduct);
		}

		int totalElements = Integer.parseInt(countQuery.getSingleResult()+"");
		int totalPage = (int)Math.ceil(Double.parseDouble(totalElements+"")/Double.parseDouble(pageSize+""));
		int totalElementOnPage = feedbacks.size();

		return new PageCustom<>(totalPage, totalElementOnPage, totalElements, feedbackResponses);
	}
}
