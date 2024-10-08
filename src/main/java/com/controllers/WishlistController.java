package com.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Product;
import com.entities.User;
import com.entities.Wishlist;
import com.errors.ResponseAPI;
import com.models.WishlistModel;
import com.responsedto.WishlistResponse;
import com.services.AuthService;
import com.services.JWTService;
import com.services.ProductService;
import com.services.UserService;
import com.services.WishlistService;

@RestController
@CrossOrigin("*")
public class WishlistController {

	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;

	@Autowired
	UserService userService;

	@Autowired
	WishlistService wishlistService;

	@Autowired
	ProductService productService;
	

	@PostMapping("api/user/wishlist/add")
	public ResponseEntity<ResponseAPI<WishlistResponse>> addWishlist(
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestBody WishlistModel wishlistModel) {
		String token = authService.readTokenFromHeader(authHeader);
		ResponseAPI<WishlistResponse> response = new ResponseAPI<>();

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

		if (wishlistModel.getProductId() == null) {
			response.setCode(422);
			response.setMessage("Product id can't null");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		Product product = productService.getProductById(wishlistModel.getProductId());

		if (product == null) {
			response.setCode(404);
			response.setMessage("Product not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		if(wishlistService.isFavorited(user, wishlistModel.getProductId())) {
			response.setCode(409);
			response.setMessage("This product has been liked by user!");

			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}

		Wishlist wishlistEntity = new Wishlist();
		wishlistEntity.setUser(user);
		wishlistEntity.setProduct(product);

		Wishlist wishlistSaved = wishlistService.createWishlist(wishlistEntity);

		WishlistResponse wishlistResponse = new WishlistResponse();
		wishlistResponse.setProductId(product.getProductId());
		wishlistResponse.setProductName(product.getProductName());
		wishlistResponse.setUserId(user.getUserId());

		response.setCode(200);
		response.setMessage("Success");
		response.setData(wishlistResponse);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("api/user/wishlist/remove/{wishlistId}")
	public ResponseEntity<ResponseAPI<Boolean>> removeWishlist(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@PathVariable("wishlistId") Integer wishlistId) {
		String token = authService.readTokenFromHeader(authHeader);
		ResponseAPI<Boolean> response = new ResponseAPI<>();

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

		if (!wishlistService.validWishlist(user, wishlistId)) {
			response.setCode(404);
			response.setMessage("Wishtlist not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		boolean isDeleteSuccess = wishlistService.removeWishlist(wishlistId);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(isDeleteSuccess);

		return ResponseEntity.ok(response);
	}
}
