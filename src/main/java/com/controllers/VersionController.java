package com.controllers;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Product;
import com.entities.ProductVersion;
import com.errors.ResponseAPI;
import com.repositories.ProductVersionJPA;
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
	public ResponseEntity<ResponseAPI<Boolean>> addVersion(@RequestBody ProductVersionResponse versionModal) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);
		String versionName = versionModal.getVersionName();
		BigDecimal retailPrice = versionModal.getRetailPrice();
		BigDecimal wholesalePrice = versionModal.getWholesalePrice();

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

		if (wholesalePrice == null || wholesalePrice.compareTo(BigDecimal.ZERO) <= 0) {
			response.setCode(999);
			response.setMessage("Wholesale price invalid!");
			return ResponseEntity.status(999).body(response);
		}

		versionService.addVersion(versionModal, product);
		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");
		return ResponseEntity.ok(response);
	}

	@PutMapping("/update")
	public ResponseEntity<ResponseAPI<Boolean>> updateVersion(@RequestBody ProductVersionResponse versionModal) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);
		String versionName = versionModal.getVersionName();
		BigDecimal retailPrice = versionModal.getRetailPrice();
		BigDecimal wholesalePrice = versionModal.getWholesalePrice();

		ProductVersion version = versionJPA.findById(versionModal.getId()).orElse(null);

		if (version == null) {
			response.setCode(404);
			response.setMessage("Version not found!");

			ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!version.isStatus() || !version.getProduct().isStatus()) {
			response.setCode(404);
			response.setMessage("Version not found!");

			ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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

		if (wholesalePrice == null || wholesalePrice.compareTo(BigDecimal.ZERO) <= 0) {
			response.setCode(999);
			response.setMessage("Wholesale price invalid!");
			return ResponseEntity.status(999).body(response);
		}

		versionService.updateVersion(versionModal);
		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/remove/{id}")
	public ResponseEntity<ResponseAPI<Boolean>> deleteVersion(@PathVariable("id") int id) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);

		ProductVersion version = versionJPA.findById(id).orElse(null);

		if (version == null) {
			response.setCode(404);
			response.setMessage("Version not found!");

			ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!version.isStatus() || !version.getProduct().isStatus()) {
			response.setCode(404);
			response.setMessage("Version not found!");

			ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		version.setStatus(false);
		versionJPA.save(version);

		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");
		return ResponseEntity.ok(response);
	}

}
