package com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.responsedto.PriceSale;
import com.responsedto.ProductDTO;
import com.responsedto.WishlistResponse;
import com.services.AuthService;
import com.services.JWTService;
import com.services.ProductClientService;
import com.services.ProductService;
import com.services.UserService;
import com.services.WishlistService;
import com.utils.GetURLImg;
import com.utils.UploadService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
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

	@Autowired
	ProductClientService inforService;

	@Autowired
	UploadService uploadService;

	@GetMapping("api/user/wishlist/getproductwish")
	public ResponseEntity<ResponseAPI<List<ProductDTO>>> getProductWish(HttpServletRequest request,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ResponseAPI<List<ProductDTO>> response = new ResponseAPI<>();
		try {
			if (authHeader.isEmpty()) {
				response.setCode(400); // 400 Bad Request
				response.setMessage("Authorization header is missing");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			String token = authService.readTokenFromHeader(authHeader);
			if (token == null || token.isEmpty()) {
				response.setCode(401); // 401 Unauthorized
				response.setMessage("Token is missing or empty");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			if (jwtService.isTokenExpired(token)) {
				response.setCode(401); // 401 Unauthorized
				response.setMessage("Token expired");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			String username = jwtService.extractUsername(token);
			if (username == null || username.isEmpty()) {
				response.setCode(400); // 400 Bad Request
				response.setMessage("Username extraction from token failed");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			User user = userService.getUserByUsername(username);
			if (user == null) {
				response.setCode(404); // 404 Not Found
				response.setMessage("User not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
			if (user.getStatus() == 0) {
				response.setCode(403);
				response.setMessage("Account locked");

				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
			}
			List<ProductDTO> items = inforService.getProductWish(user);
			if (items == null || items.isEmpty()) {
				response.setCode(204); // 204 No Content
				response.setMessage("No products found");
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
			}

			for (ProductDTO productDTO : items) {
				String img = productDTO.getImgName();
				if (img != null && !img.isEmpty()) {
					productDTO.setImgName(uploadService.getUrlImage(img));
				}
				PriceSale priceSale = inforService.getSale(Integer.valueOf(productDTO.getId()));
				productDTO.setQuantity(priceSale.getQuantity());
				productDTO.setDiscountPercent(priceSale.getDiscountPercent());
				productDTO.setMinPriceSale(priceSale.getMinPriceSale());
				productDTO.setMaxPriceSale(priceSale.getMaxPriceSale());
			}
			response.setCode(200); // 200 OK
			response.setMessage("Success");
			response.setData(items);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			response.setCode(500); // 500 Internal Server Error
			response.setMessage("An error occurred while fetching products: " + e.getMessage());
			response.setData(null);
			e.printStackTrace(); // Log ngoại lệ chi tiết
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

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

		if (wishlistService.isFavorited(user, wishlistModel.getProductId())) {
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

		// Là id product
		Product product = productService.getProductById(wishlistId);
		boolean check = false;
		int idRemove = 0;

		if (product == null) {
			response.setCode(404);
			response.setMessage("Product not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!product.getWishlists().isEmpty() && product.getWishlists() != null) {
			for (Wishlist wl : product.getWishlists()) {
				if (wl.getUser().getUserId() == user.getUserId()) {
					check = true;
					idRemove = wl.getId();
					break;
				}
			}
		}

		if (!check) {
			response.setCode(404);
			response.setMessage("Wishtlist not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		boolean isDeleteSuccess = wishlistService.removeWishlist(idRemove);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(isDeleteSuccess);

		return ResponseEntity.ok(response);
	}
}
