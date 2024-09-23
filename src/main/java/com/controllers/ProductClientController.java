package com.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.responsedto.CartItemResponse;
import com.responsedto.ProductCartResponse;
import com.responsedto.ProductDTO;
import com.services.AlgoliaProductService;
import com.services.ProductInforService;
import com.utils.GetURLImg;
import com.utils.RemoveDiacritics;

import jakarta.servlet.http.HttpServletRequest;

import com.algolia.search.models.indexing.Query;
import com.algolia.search.models.indexing.SearchResult;
import com.entities.AttributeOption;
import com.entities.Category;
import com.errors.ResponseAPI;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/product")
@CrossOrigin("*")
public class ProductClientController {

	@Autowired
	ProductInforService inforService;

	private final AlgoliaProductService algoliaProductService;

	@Autowired
	public ProductClientController(AlgoliaProductService algoliaProductService) {
		this.algoliaProductService = algoliaProductService;
	}

	@GetMapping("/filtered")
	public ResponseEntity<ResponseAPI<List<ProductDTO>>> getFilteredProducts(HttpServletRequest request,
			@RequestParam(required = false) Integer categoryId, @RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice, @RequestParam(required = false) Integer color,
			@RequestParam(required = false) Integer size, @RequestParam(defaultValue = "ASC") String sortPrice) {

		ResponseAPI<List<ProductDTO>> response = new ResponseAPI<>();
		try {
			// Lấy danh sách sản phẩm từ service
			List<ProductDTO> items = inforService.getFilteredProducts(categoryId, minPrice, maxPrice, color, size,
					sortPrice);
			for (ProductDTO productDTO : items) {
				String img = productDTO.getImg();
				productDTO.setImg(GetURLImg.getURLImg(request, img));
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

	@GetMapping("/getAllSize")
	public ResponseEntity<ResponseAPI<List<AttributeOption>>> getAllSize() {
		ResponseAPI<List<AttributeOption>> response = new ResponseAPI<>();
		try {
			// Lấy danh sách sản phẩm
			List<AttributeOption> items = inforService.getListSize();
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

	@GetMapping("/getAllColor")
	public ResponseEntity<ResponseAPI<List<AttributeOption>>> getAllColor() {
		ResponseAPI<List<AttributeOption>> response = new ResponseAPI<>();
		try {
			// Lấy danh sách sản phẩm
			List<AttributeOption> items = inforService.getListColor();
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
			response.setMessage("An error occurred while fetching colors: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/getAllCategory")
	public ResponseEntity<ResponseAPI<List<Category>>> getAllCategory() {
		ResponseAPI<List<Category>> response = new ResponseAPI<>();
		try {
			// Lấy danh sách sản phẩm
			List<Category> items = inforService.getListCategory();
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

	// Tìm kiếm sản phẩm từ Algolia
	@GetMapping("/search")
	public ResponseEntity<ResponseAPI<List<ProductDTO>>> searchProducts(HttpServletRequest request,
			@RequestParam String query) {
		ResponseAPI<List<ProductDTO>> response = new ResponseAPI<>();
		try {
			RemoveDiacritics diacritics = new RemoveDiacritics();
			Query algoliaQuery = new Query(diacritics.removeDiacritics(query));
			List<ProductDTO> products = algoliaProductService.searchProducts(algoliaQuery);
			for (ProductDTO productDTO : products) {
				String img = productDTO.getImg();
				productDTO.setImg(GetURLImg.getURLImg(request, img));
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