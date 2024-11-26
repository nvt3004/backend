package com.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.errors.ResponseAPI;
import com.responsedto.ProductDTO;
import com.services.AlgoliaProductService;
import com.services.ProductClientService;

import java.util.List;

@RestController
@RequestMapping("api/push/product")
public class ProductPushController {

	@Autowired
	private AlgoliaProductService algoliaProductService;

	@Autowired
	private ProductClientService inforService;

	@PostMapping
	public ResponseEntity<ResponseAPI<?>> pushProduct() {
		ResponseAPI<Object> response = new ResponseAPI<>();
		try {

		List<ProductDTO> products = inforService.getALLProduct(null);

			if (products.isEmpty()) {
				response.setCode(204);
				response.setMessage("No products found");
				response.setData(null);
			} else {
				algoliaProductService.clearAllProductsAsync().join();
				for (ProductDTO product : products) {
					algoliaProductService.addProductToAlgoliaAsync(product);
				}
				response.setCode(200);
				response.setMessage("Success");
				response.setData(null);
			}
		} catch (Exception e) {

			response.setCode(500);
			response.setMessage("An error occurred while fetching products: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}

		return ResponseEntity.ok(response);
	}
}
