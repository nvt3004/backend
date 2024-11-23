package com.controllers;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Feedback;
import com.entities.Product;
import com.entities.Reply;
import com.entities.User;
import com.errors.ResponseAPI;
import com.models.ReplyDTO;
import com.repositories.FeedbackJPA;
import com.responsedto.ReplyResponseDTO;
import com.responsedto.UserResponseDTO;
import com.services.AuthService;
import com.services.FeedbackService;
import com.services.JWTService;
import com.services.ProductService;
import com.services.ProductVersionService;
import com.services.ReplyService;
import com.services.UserService;
import com.utils.UploadService;

@RestController
@RequestMapping("/api/admin/reply")
public class ReplyController {
	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;

	@Autowired
	ProductService productService;

	@Autowired
	UserService userService;

	@Autowired
	ProductVersionService versionService;

	@Autowired
	UploadService uploadService;

	@Autowired
	FeedbackService feedbackService;

	@Autowired
	FeedbackJPA feedbackJPA;

	@Autowired
	ReplyService replyService;

	@PostMapping("/add")
	@PreAuthorize("hasPermission(#userId, 'Add Reply')")
	public ResponseEntity<ResponseAPI<ReplyResponseDTO>> addReply(
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestBody ReplyDTO reply) {
		ResponseAPI<ReplyResponseDTO> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		Feedback feedback = feedbackJPA.findById(reply.getFeedbackId()).orElse(null);

		if (feedback == null) {
			response.setCode(404);
			response.setMessage("Feedback not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		if(replyService.isReplied(feedback)) {
			response.setCode(409);
			response.setMessage("This feedback has already been replied to.");

			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}

		Product product = productService.getProductById(feedback.getProduct().getProductId());

		if (product == null) {
			response.setCode(404);
			response.setMessage("Cannot reply to feedback for non-existent products");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (reply.getContent() == null || reply.getContent().isEmpty() || reply.getContent().isBlank()) {
			response.setCode(422);
			response.setMessage("Invalid format content");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}
		

		Reply replyEntity = createReplyEntity(reply, user);
		Reply replySaved = replyService.createReply(replyEntity);

		UserResponseDTO userResponse = createUserResponse(user);
		ReplyResponseDTO replyResponseDTO = createReplyResponse(replySaved, userResponse);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(replyResponseDTO);

		return ResponseEntity.ok(response);
	}

	private UserResponseDTO createUserResponse(User user) {
		UserResponseDTO userResponse = new UserResponseDTO();

		userResponse.setFullName(user.getFullName());
		userResponse.setId(user.getUserId());
		userResponse.setImage(uploadService.getUrlImage(user.getImage()));

		return userResponse;
	}

	private ReplyResponseDTO createReplyResponse(Reply reply, UserResponseDTO userResponse) {
		ReplyResponseDTO replyResponseDTO = new ReplyResponseDTO();
		replyResponseDTO.setReplyId(reply.getId());
		replyResponseDTO.setContent(reply.getContent());
		replyResponseDTO.setReplyDate(reply.getReplyDate());
		replyResponseDTO.setUserReply(userResponse);

		return replyResponseDTO;
	}

	private Reply createReplyEntity(ReplyDTO reply, User user) {
		Reply replyEntity = new Reply();
		Feedback feedbackEntity = new Feedback();
		feedbackEntity.setFeedbackId(reply.getFeedbackId());

		replyEntity.setUser(user);
		replyEntity.setFeedback(feedbackEntity);
		replyEntity.setContent(reply.getContent());
		replyEntity.setReplyDate(new Date());

		return replyEntity;
	}
}
