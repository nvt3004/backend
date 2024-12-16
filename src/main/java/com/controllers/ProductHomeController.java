package com.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Product;
import com.entities.ProductVersion;
import com.errors.ResponseAPI;
import com.repositories.ProductVersionJPA;
import com.responsedto.PageCustom;
import com.responsedto.ProductDetailResponse;
import com.responsedto.ProductHomeResponse;
import com.responsedto.Version;
import com.services.AuthService;
import com.services.JWTService;
import com.services.ProductService;
import com.utils.GetURLImg;
import com.utils.UploadService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/home")
public class ProductHomeController {

	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;

	@Autowired
	ProductService productService;
	
	@Autowired
	UploadService uploadService;
	
	@Autowired
	ProductVersionJPA vsJPA;

	@GetMapping("/products")
	public ResponseEntity<ResponseAPI<PageCustom<ProductHomeResponse>>> getAllProduct(
			@RequestParam("idCategory") Optional<Integer> idCategory, @RequestParam("page") Optional<Integer> page) {
		ResponseAPI<PageCustom<ProductHomeResponse>> response = new ResponseAPI<>();
		PageCustom<ProductHomeResponse> products = null;
		int size = 10;

		if (!page.isPresent()) {
			products = productService.getProducts(1, size);
		} else if (idCategory.isPresent()) {
			products = productService.getProducts(page.get(), size, idCategory.get());
		} else {
			products = productService.getProducts(page.get(), size);
		}

		response.setCode(200);
		response.setMessage("success");
		response.setData(products);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<ResponseAPI<ProductDetailResponse>> getProductDetail(HttpServletRequest request,
			@PathVariable("productId") int productId) {
		ResponseAPI<ProductDetailResponse> response = new ResponseAPI<>();
		Product product = productService.getProductById(productId);

		if (product == null) {
			response.setCode(404);
			response.setMessage("Product not found!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		
		ProductDetailResponse productDetail = productService.getProductDetail(productId);
		for (Version v : productDetail.getVersions()) {
			if (v.getImage() != null) {
				v.setImage(uploadService.getUrlImage(v.getImage()));
			}
		}
		response.setCode(200);
		response.setMessage("Success");
		response.setData(productDetail);

		return ResponseEntity.ok(response);
	}
}
