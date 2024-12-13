package com.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entities.AttributeOption;
import com.entities.AttributeOptionsVersion;
import com.entities.Product;
import com.entities.ProductVersion;
import com.errors.ResponseAPI;
import com.repositories.ProductVersionJPA;
import com.responsedto.Attribute;
import com.responsedto.ProductVersionResponse;
import com.services.ProductService;
import com.services.VersionService;

@RestController
@RequestMapping("/api/staff/version")
public class VersionController {

	@Autowired
	ProductVersionJPA versionJPA;

	@Autowired
	ProductService productService;

	@Autowired
	VersionService versionService;

	@PostMapping("/add")
	@PreAuthorize("hasPermission(#userId, 'Add Product') or hasPermission(#userId, 'Update Product')")
	public ResponseEntity<ResponseAPI<Boolean>> addVersion(@RequestBody ProductVersionResponse versionModal) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);
		String versionName = versionModal.getVersionName();
		BigDecimal retailPrice = versionModal.getRetailPrice();
		BigDecimal importPrice = versionModal.getImportPrice();

		Product product = productService.getProductById(versionModal.getIdProduct());

		if (product == null) {
			response.setCode(404);
			response.setMessage("Product not found!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (versionName.isBlank() || versionName.isEmpty() || versionName == null) {
			response.setCode(999);
			response.setMessage("Version name invalid!");
			return ResponseEntity.status(999).body(response);
		}

		if (retailPrice == null || retailPrice.compareTo(BigDecimal.ZERO) <= 0) {
			response.setCode(999);
			response.setMessage("Retail price invalid!");
			return ResponseEntity.status(999).body(response);
		}

		if (importPrice == null || importPrice.compareTo(BigDecimal.ZERO) <= 0) {
			response.setCode(999);
			response.setMessage("ImportPrice price invalid!");
			return ResponseEntity.status(999).body(response);
		}

		boolean isExitVersion = versionService.isExitVersionInProduct(product, versionModal.getAttributes());

		if (isExitVersion) {
			response.setCode(999);
			response.setMessage("The version attribute already exists!");
			return ResponseEntity.status(999).body(response);
		}

		versionService.addVersion(versionModal, product);
		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");
		return ResponseEntity.ok(response);
	}

	@PutMapping("/update")
	@PreAuthorize("hasPermission(#userId, 'Add Product') or hasPermission(#userId, 'Update Product')")
	public ResponseEntity<ResponseAPI<Boolean>> updateVersion(@RequestBody ProductVersionResponse versionModal) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);
		String versionName = versionModal.getVersionName();
		BigDecimal retailPrice = versionModal.getRetailPrice();
		BigDecimal wholesalePrice = versionModal.getImportPrice();

		ProductVersion version = versionJPA.findById(versionModal.getId()).orElse(null);

		if (version == null) {
			response.setCode(404);
			response.setMessage("Version not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!version.isStatus() || !version.getProduct().isStatus()) {
			response.setCode(404);
			response.setMessage("Version not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (versionName.isBlank() || versionName.isEmpty() || versionName == null) {
			response.setCode(999);
			response.setMessage("Version name invalid!");
			return ResponseEntity.status(999).body(response);
		}

		if (retailPrice == null || retailPrice.compareTo(BigDecimal.ZERO) <= 0) {
			response.setCode(999);
			response.setMessage("Retail price invalid!");
			return ResponseEntity.status(999).body(response);
		}
		

		versionService.updateVersion(versionModal);
		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/remove/{id}")
	@PreAuthorize("hasPermission(#userId, 'Add Product') or hasPermission(#userId, 'Update Product')")
	public ResponseEntity<ResponseAPI<Boolean>> deleteVersion(@PathVariable("id") int id) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);

		ProductVersion version = versionJPA.findById(id).orElse(null);

		if (version == null) {
			response.setCode(404);
			response.setMessage("Version not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!version.isStatus() || !version.getProduct().isStatus()) {
			response.setCode(404);
			response.setMessage("Version not found!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		version.setStatus(false);
		versionJPA.save(version);

		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");
		return ResponseEntity.ok(response);
	}

}
