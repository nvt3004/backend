package com.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Category;
import com.entities.User;
import com.entities.Wishlist;
import com.errors.ResponseAPI;
import com.responsedto.Att;
import com.responsedto.FilterAttribute;
import com.responsedto.ProductDTO;
import com.services.AlgoliaProductService;
import com.services.AuthService;
import com.services.JWTService;
import com.services.ProductClientService;
import com.services.UserService;
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
	public ResponseEntity<ResponseAPI<List<ProductDTO>>> getTopProducts(HttpServletRequest request,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ResponseAPI<List<ProductDTO>> response = new ResponseAPI<>();
		List<ProductDTO> items;
		List<Wishlist> wishlist = new ArrayList<>(); // Khởi tạo danh sách wishlist mặc định
		User user = null;

		try {
			// Nếu có token, kiểm tra tính hợp lệ và lấy thông tin người dùng
			if (authHeader.isPresent() && !authHeader.get().isEmpty()) {
				String token = authService.readTokenFromHeader(authHeader);

				if (token != null && !token.isEmpty() && !jwtService.isTokenExpired(token)) {
					String username = jwtService.extractUsername(token);
					user = userService.getUserByUsername(username);
					wishlist = user != null ? user.getWishlists() : new ArrayList<>();
				}
			}

			// Lấy danh sách sản phẩm từ service
			items = algoliaProductService.getTopProducts();

			// Cập nhật trạng thái "like" và URL hình ảnh cho các sản phẩm
			for (ProductDTO productDTO : items) {
				if (user != null) { // Chỉ kiểm tra wishlist nếu có user
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

			// Kiểm tra kết quả sản phẩm
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

	@GetMapping("/FilterAttribute")
	public ResponseEntity<ResponseAPI<FilterAttribute>> getFilterAttribute() {
		ResponseAPI<FilterAttribute> response = new ResponseAPI<>();
		try {
			// Lấy danh sách thuộc tính
			List<String> attName = inforService.getListAttName();
			List<Integer> attId = inforService.getListAttId();
			List<Category> categories = inforService.getListCategory();

			FilterAttribute filterAttributes = new FilterAttribute();
			Att a = new Att();
			a.setNames(attName);
			a.setIds(attId);
			filterAttributes.setCategories(categories);
			filterAttributes.setAttribute(a);

			// Kiểm tra nếu tất cả danh sách đều rỗng
			if (attName.isEmpty() && attName.isEmpty() && categories.isEmpty()) {
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
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestParam(required = false) String query,
			@RequestParam(required = false) Integer categoryID, @RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String attribute,
			@RequestParam(defaultValue = "ASC") String sortMaxPrice, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int pageSize) {

		System.out.println("Search Request Parameters:");
		System.out.println("Authorization Header: " + authHeader.orElse("None"));
		System.out.println("Query: " + query);
		System.out.println("Category ID: " + categoryID);
		System.out.println("Min Price: " + minPrice);
		System.out.println("Max Price: " + maxPrice);
		System.out.println("Attributes: " + attribute);
		System.out.println("Sort Max Price: " + sortMaxPrice);
		System.out.println("Page: " + page);
		System.out.println("Page Size: " + pageSize);

		ResponseAPI<List<ProductDTO>> response = new ResponseAPI<>();
		List<Wishlist> wishlist = new ArrayList<>();
		   List<Integer> attributeIds = new ArrayList<>();

		    // Phân tích chuỗi `attribute` thành danh sách các số nguyên
		    if (attribute != null && !attribute.isEmpty()) {
		        try {
		            attributeIds = Arrays.stream(attribute.split(","))
		                                 .map(Integer::parseInt)
		                                 .collect(Collectors.toList());
		            System.out.println("Danh sách attribute ID: " + attributeIds); 
		        } catch (NumberFormatException e) {
		            System.out.println("Lỗi: Chuỗi attribute chứa giá trị không hợp lệ.");
		            response.setCode(400);
		            response.setMessage("Invalid attribute format. Attributes should be integers separated by commas.");
		            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		        }
		    }
		try {
			User user = null;

			if (authHeader.isPresent() && !authHeader.get().isEmpty()) {
				String token = authService.readTokenFromHeader(authHeader);

				if (token != null && !token.isEmpty() && !jwtService.isTokenExpired(token)) {
					String username = jwtService.extractUsername(token);
					user = userService.getUserByUsername(username);
					if (user != null) {
						wishlist = user.getWishlists();
					}
				}
			}

			List<ProductDTO> products = algoliaProductService.searchProducts(categoryID, attributeIds, minPrice, maxPrice,
					query, sortMaxPrice, page, pageSize);

			if (products != null && !products.isEmpty()) {
				for (ProductDTO productDTO : products) {

					if (user != null) {
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
						String imageUrl = uploadService.getUrlImage(img);
						if (imageUrl != null && !imageUrl.isEmpty()) {
							productDTO.setImgName(imageUrl);
						}
					}
				}
			}

			if (products.isEmpty()) {
				response.setCode(404);
				response.setMessage("No products found");
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

		System.out.println("Search Request Parameters:");
		System.out.println("Authorization Header: " + authHeader.orElse("None"));
		System.out.println("Query: " + query);
		System.out.println("Category ID: " + categoryID);
		System.out.println("Min Price: " + minPrice);
		System.out.println("Max Price: " + maxPrice);
		System.out.println("Attributes: " + attribute);
		System.out.println("Sort Max Price: " + sortMaxPrice);
		System.out.println("Page: " + page);
		System.out.println("Page Size: " + pageSize);
		return ResponseEntity.ok(response);
	}

}
