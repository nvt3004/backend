package com.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.entities.Feedback;
import com.entities.Image;
import com.entities.OrderDetail;
import com.entities.Product;
import com.entities.User;
import com.errors.ResponseAPI;
import com.repositories.FeedbackJPA;
import com.repositories.ImageJPA;
import com.repositories.OrderDetailJPA;
import com.responsedto.FeedbackResponseDTO;
import com.responsedto.PageCustom;
import com.responsedto.UserResponseDTO;
import com.services.AuthService;
import com.services.FeedbackService;
import com.services.JWTService;
import com.services.ProductService;
import com.services.ProductVersionService;
import com.services.UserService;
import com.utils.UploadService;

@RestController
public class FeedbackController {

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
	ImageJPA imageJPA;

	@Autowired
	FeedbackJPA feedbackJPA;
	
	@Autowired
	OrderDetailJPA orderDetailJPA;

	@PostMapping("api/user/feedback/add")
	public ResponseEntity<ResponseAPI<FeedbackResponseDTO>> addFeedback(
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestParam("comment") String comment,
			@RequestParam("idOrderDetail") Integer idOrderDetail,
			@RequestPart("photos") Optional<List<MultipartFile>> photos,
			@RequestParam("productId") Optional<Integer> productId, Optional<Integer> rating) {
		ResponseAPI<FeedbackResponseDTO> response = new ResponseAPI<>();
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

		if (comment == null || comment.isBlank() || comment.isEmpty()) {
			response.setCode(422);
			response.setMessage("Invalid format comment");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		Product product = productService.getProductById(productId.get());

		if (product == null) {
			response.setCode(404);
			response.setMessage("Product not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (photos.isPresent() && photos.get().size() > 0 && uploadService.isEmptyFile(photos.get())) {
			response.setCode(400);
			response.setMessage("The uploaded file is not a valid image or is empty");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (rating.isPresent() && (rating.get() < 0 || rating.get() > 5)) {
			response.setCode(422);
			response.setMessage("The rating must be a number between 0 and 5");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		int countProductPurchased = feedbackService.getCountProductPurchased(user.getUserId(), product.getProductId());

		if (countProductPurchased <= 0) {
			response.setCode(401);
			response.setMessage("Cannot comment because have never purchased the product");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		int countFeedbackOfUserByProduct = feedbackService.getCountFeedbackOfUserByProduct(user.getUserId(),
				product.getProductId());

		if (countFeedbackOfUserByProduct >= countProductPurchased) {
			response.setCode(401);
			response.setMessage(
					"You have already submitted feedback for this product and cannot submit again until a new purchase is made");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
		
		if(idOrderDetail == null) {
			response.setCode(999);
			response.setMessage(
					"Id order detail can't is  null");

			return ResponseEntity.status(999).body(response);
		}
		
		OrderDetail detail = orderDetailJPA.findById(idOrderDetail).orElse(null);
		
		if(detail == null) {
			response.setCode(404);
			response.setMessage(
					"Id order detail not found");

			return ResponseEntity.status(404).body(response);
		}

		Feedback feedback = new Feedback();
		
		feedback.setUser(user);
		feedback.setProduct(product);
		feedback.setRating(rating.orElse(0));
		feedback.setComment(comment);
		feedback.setFeedbackDate(new Date());
		feedback.setOrderDetail(detail);

		Feedback feedbackSaved = feedbackService.createFeedback(feedback);
		List<Image> images = saveImageFeedback(feedbackSaved, photos.orElse(new ArrayList<MultipartFile>()));

		FeedbackResponseDTO feedbackResponse = new FeedbackResponseDTO();
		UserResponseDTO userResponse = new UserResponseDTO(user.getUserId(), user.getFullName(),
				uploadService.getUrlImage(user.getImage()));

		feedbackResponse.setFeedbackId(feedback.getFeedbackId());
		feedbackResponse.setComment(feedback.getComment());
		feedbackResponse.setFeedbackDate(feedback.getFeedbackDate());
		if (images.size() > 0) {
			feedbackResponse
					.setImages(images.stream().map(img -> uploadService.getUrlImage(img.getImageUrl())).toList());
		}

		feedbackResponse.setProductId(feedback.getProduct().getProductId());
		feedbackResponse.setProductName(feedback.getProduct().getProductName());
		feedbackResponse.setRating(feedback.getRating());
		feedbackResponse.setUser(userResponse);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(feedbackResponse);

		return ResponseEntity.ok(response);
	}

	@GetMapping("api/user/feedback")
	public ResponseEntity<ResponseAPI<PageCustom<FeedbackResponseDTO>>> getFeedback(
			@RequestParam(name="page" , defaultValue = "1") Optional<Integer> pageNumber,
			@RequestParam("idProduct") Optional<Integer> idProduct) {
		ResponseAPI<PageCustom<FeedbackResponseDTO>> response = new ResponseAPI<>();

		if (!idProduct.isPresent()) {
			response.setCode(400);
			response.setMessage("Invalid product id");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (productService.getProductById(idProduct.get()) == null) {
			response.setCode(404);
			response.setMessage("Product not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		PageCustom<FeedbackResponseDTO> feedbackPage = feedbackService.findFeedbackByCondition(null, null,
				idProduct.get(), pageNumber.orElse(-1), 10);

		response.setData(feedbackPage);
		response.setCode(200);
		response.setMessage("Success");

		return ResponseEntity.ok(response);
	}

	@GetMapping("api/staff/feedback/dashboard")
	@PreAuthorize("hasPermission(#userId, 'Delete Feedback')")
	public ResponseEntity<ResponseAPI<PageCustom<FeedbackResponseDTO>>> getFeedbackDashboard(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam("page") Optional<Integer> pageNumber, @RequestParam("startDate") Optional<Date> startDate,
			@RequestParam("endDate") Optional<Date> endDate, @RequestParam("idProduct") Optional<Integer> idProduct) {
		ResponseAPI<PageCustom<FeedbackResponseDTO>> response = new ResponseAPI<>();
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

		// Bắt admin mới được vô đây

		PageCustom<FeedbackResponseDTO> feedbackPage = feedbackService.findFeedbackByCondition(startDate.orElse(null),
				endDate.orElse(null), idProduct.orElse(-1), pageNumber.orElse(-1), 10);

		response.setData(feedbackPage);
		response.setCode(200);
		response.setMessage("Success");

		return ResponseEntity.ok(response);
	}

	private List<Image> saveImageFeedback(Feedback feedback, List<MultipartFile> files) {
		List<Image> images = new ArrayList<>();

		if (!files.isEmpty() && files.size() > 0 && !uploadService.isEmptyFile(files)) {
			for (MultipartFile file : files) {
				Image image = new Image();
				String imageName = uploadService.save(file, "images");

				image.setFeedback(feedback);
				image.setImageUrl(imageName);

				images.add(imageJPA.save(image));
			}

			return images;
		}

		return new ArrayList<Image>();
	}
}
