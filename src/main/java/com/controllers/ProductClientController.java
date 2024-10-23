package com.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algolia.search.models.indexing.Query;
import com.entities.AttributeOption;
import com.entities.Category;
import com.entities.User;
import com.entities.Wishlist;
import com.errors.ResponseAPI;
import com.responsedto.FilterAttribute;
import com.responsedto.ProductDTO;
import com.services.AlgoliaProductService;
import com.services.AuthService;
import com.services.JWTService;
import com.services.ProductClientService;
import com.services.UserService;
import com.utils.GetURLImg;
import com.utils.RemoveDiacritics;
import com.utils.UploadService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/product")
public class ProductClientController {
	@Autowired
	AuthService authService;
	
	@Autowired
	JWTService jwtService;

	@Autowired
	UserService userService;
	@Autowired
	ProductClientService inforService;
	
	@Autowired
	UploadService uploadService;

	private final AlgoliaProductService algoliaProductService;

	@Autowired
	public ProductClientController(AlgoliaProductService algoliaProductService) {
		this.algoliaProductService = algoliaProductService;
	}

	@GetMapping("/getTopProducts")
	public ResponseEntity<ResponseAPI<List<ProductDTO>>> getTopProducts(HttpServletRequest request,@RequestHeader("Authorization") Optional<String> authHeader) {

		ResponseAPI<List<ProductDTO>> response = new ResponseAPI<>();
		try {
			String token = authService.readTokenFromHeader(authHeader);
			String username = null;

			User user = null;
			if (!(token == null || token.isEmpty())) {
				if (jwtService.isTokenExpired(token)) {
					response.setCode(401);
					response.setMessage("Token expired");
				} else {
					username = jwtService.extractUsername(token);
					user = userService.getUserByUsername(username);
				}
			}
			
			// Lấy danh sách sản phẩm từ service
			List<ProductDTO> items = algoliaProductService.getTopProducts();
			List<Wishlist> wishlist = (user != null) ? user.getWishlists() : null;
			for (ProductDTO productDTO : items) {
				
				if (wishlist != null) {
					for (Wishlist wishlistItem : wishlist) {
						if (String.valueOf(wishlistItem.getProduct().getProductId())
								.equalsIgnoreCase(productDTO.getId())) {
							productDTO.setLike(true);
			 				break;
						}
					}
				}
				String img = productDTO.getImgName();
				if (img != null && !img.isEmpty()) {
					productDTO.setImgName(uploadService.getUrlImage(img));
				}
			}

			if (items.isEmpty()) {
				response.setCode(204); // 204 No Content
				response.setMessage("No products found");
			} else {
				response.setCode(200); // 200 OK
				response.setMessage("Success");
				response.setData(items);
			}
		} catch (Exception e) {
			// Xử lý ngoại lệ
			response.setCode(500); // 500 Internal Server Error
			response.setMessage("An error occurred while fetching products: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/filtered")
	public ResponseEntity<ResponseAPI<Page<ProductDTO>>> getFilteredProducts(HttpServletRequest request,
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam(required = false) String categoryName, @RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice, @RequestParam(required = false) String color,
			@RequestParam(required = false) String size, @RequestParam(defaultValue = "ASC") String sortPrice,
			@RequestParam(defaultValue = "0") int page, // Số trang, mặc định là 0 (trang đầu tiên)
			@RequestParam(defaultValue = "5") int pageSize // Kích thước trang, mặc định là 10 sản phẩm mỗi trang
	) {
		ResponseAPI<Page<ProductDTO>> response = new ResponseAPI<>();
		try {
			String token = authService.readTokenFromHeader(authHeader);
			String username = null;

			User user = null;
			if (!(token == null || token.isEmpty())) {
				if (jwtService.isTokenExpired(token)) {
					response.setCode(401);
					response.setMessage("Token expired");
				} else {
					username = jwtService.extractUsername(token);
					user = userService.getUserByUsername(username);
				}
			}
			List<Wishlist> wishlist = (user != null) ? user.getWishlists() : null;

			Page<ProductDTO> pagedItems = inforService.getFilteredProducts(user, categoryName, minPrice, maxPrice,
					color, size, sortPrice, page, pageSize);
			// Cập nhật URL hình ảnh cho từng sản phẩm
			for (ProductDTO productDTO : pagedItems.getContent()) {
				if (wishlist != null) {
					for (Wishlist wishlistItem : wishlist) {
						if (String.valueOf(wishlistItem.getProduct().getProductId())
								.equalsIgnoreCase(productDTO.getId())) {
							productDTO.setLike(true);
			 				break;
						}
					}
				}
				String img = productDTO.getImgName();
				if (img != null && !img.isEmpty()) {
					productDTO.setImgName(uploadService.getUrlImage(img));
				}
			}

			// Kiểm tra nếu không có sản phẩm nào
			if (pagedItems.isEmpty()) {
				response.setCode(204); // 204 No Content
				response.setMessage("No products found");
			} else {
				response.setCode(200); // 200 OK
				response.setMessage("Success");
				response.setData(pagedItems);
			}
		} catch (Exception e) {
			// Xử lý ngoại lệ
			response.setCode(500); // 500 Internal Server Error
			response.setMessage("An error occurred while fetching products: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/FilterAttribute")
	public ResponseEntity<ResponseAPI<FilterAttribute>> getFilterAttribute() {
		ResponseAPI<FilterAttribute> response = new ResponseAPI<>();
		try {
			// Lấy danh sách thuộc tính
			List<AttributeOption> colors = inforService.getListColor();
			List<AttributeOption> sizes = inforService.getListSize();
			List<Category> categories = inforService.getListCategory();

			FilterAttribute filterAttributes = new FilterAttribute();
			filterAttributes.setCategory(categories);
			filterAttributes.setColor(colors);
			filterAttributes.setSize(sizes);

			// Kiểm tra nếu tất cả danh sách đều rỗng
			if (colors.isEmpty() && sizes.isEmpty() && categories.isEmpty()) {
				response.setCode(204); // 204 No Content
				response.setMessage("No attribute found");
			} else {
				response.setCode(200); // 200 OK
				response.setMessage("Success");
				response.setData(filterAttributes);
			}
		} catch (Exception e) {
			// Xử lý ngoại lệ
			response.setCode(500); // 500 Internal Server Error
			response.setMessage("An error occurred while fetching attributes: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/search")
	public ResponseEntity<ResponseAPI<List<ProductDTO>>> searchProducts(
			@RequestHeader("Authorization") Optional<String> authHeader, HttpServletRequest request,
			@RequestParam(required = false) String query, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int pageSize) {
		ResponseAPI<List<ProductDTO>> response = new ResponseAPI<>();
		try {
			String token = authService.readTokenFromHeader(authHeader);
			String username = null;

			User user = null;
			if (!(token == null || token.isEmpty())) {
				if (jwtService.isTokenExpired(token)) {
					response.setCode(401);
					response.setMessage("Token expired");
				} else {
					username = jwtService.extractUsername(token);
					user = userService.getUserByUsername(username);
				}
			}

			RemoveDiacritics diacritics = new RemoveDiacritics();
			Query algoliaQuery = new Query(query).setPage(page) // Thiết lập trang hiện tại
					.setHitsPerPage(pageSize); // Thiết lập số sản phẩm mỗi trang

			// Tìm kiếm sản phẩm từ Algolia với phân trang
			List<ProductDTO> products = algoliaProductService.searchProducts(algoliaQuery);

			for (ProductDTO productDTO : products) {
				String img = productDTO.getImgName();
				if (img != null && !img.isEmpty()) {
					productDTO.setImgName(uploadService.getUrlImage(img));
				}
			}

			if (products.isEmpty()) {
				response.setCode(204);
				response.setMessage("No products found for the search query: " + query);
			} else {
				response.setCode(200);
				response.setMessage("Success");
				response.setData(products);
			}
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("An error occurred during product search: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok(response);
	}

}