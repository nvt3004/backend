package com.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algolia.search.models.indexing.Query;
import com.entities.AttributeOption;
import com.entities.Category;
import com.errors.ResponseAPI;
import com.responsedto.FilterAttribute;
import com.responsedto.ProductDTO;
import com.services.AlgoliaProductService;
import com.services.ProductInforService;
import com.utils.GetURLImg;
import com.utils.RemoveDiacritics;

import jakarta.servlet.http.HttpServletRequest;

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

	@GetMapping("/getTopProducts")
	public ResponseEntity<ResponseAPI<List<ProductDTO>>> getTopProducts(HttpServletRequest request) {

		ResponseAPI<List<ProductDTO>> response = new ResponseAPI<>();
		try {
			// Lấy danh sách sản phẩm từ service
			List<ProductDTO> items = algoliaProductService.getTopProducts();
			for (ProductDTO productDTO : items) {
				String img = productDTO.getImgName();
				productDTO.setImgName(GetURLImg.getURLImg(request, img));
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
				String img = productDTO.getImgName();
				productDTO.setImgName(GetURLImg.getURLImg(request, img));
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
				String img = productDTO.getImgName();
				productDTO.setImgName(GetURLImg.getURLImg(request, img));
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