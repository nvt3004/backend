package com.controllers;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
			response.setMessage("Không tìm thấy sản phẩm!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (versionName.isBlank() || versionName.isEmpty() || versionName == null) {
			response.setCode(999);
			response.setMessage("Tên phiên bản không hợp lệ!");
			return ResponseEntity.status(999).body(response);
		}

		if (retailPrice == null || retailPrice.compareTo(BigDecimal.ZERO) <= 0) {
			response.setCode(999);
			response.setMessage("Giá bán lẻ không hợp lệ!");
			return ResponseEntity.status(999).body(response);
		}

		boolean isExitVersion = versionService.isExitVersionInProduct(product, versionModal.getAttributes());

		if (isExitVersion) {
			response.setCode(999);
			response.setMessage("Phiên bản với thuộc tính này đã tồn tại!");
			return ResponseEntity.status(999).body(response);
		}

		versionService.addVersion(versionModal, product);
		response.setCode(200);
		response.setData(true);
		response.setMessage("Thêm phiên bản thành công!");
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
			response.setMessage("Không tìm thấy phiên bản!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!version.isStatus() || !version.getProduct().isStatus()) {
			response.setCode(404);
			response.setMessage("Phiên bản hoặc sản phẩm không còn tồn tại!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (versionName.isBlank() || versionName.isEmpty() || versionName == null) {
			response.setCode(999);
			response.setMessage("Tên phiên bản không hợp lệ!");
			return ResponseEntity.status(999).body(response);
		}

		if (retailPrice == null || retailPrice.compareTo(BigDecimal.ZERO) <= 0) {
			response.setCode(999);
			response.setMessage("Giá bán lẻ không hợp lệ!");
			return ResponseEntity.status(999).body(response);
		}

		versionService.updateVersion(versionModal);
		response.setCode(200);
		response.setData(true);
		response.setMessage("Cập nhật phiên bản thành công!");
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
			response.setMessage("Không tìm thấy phiên bản!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!version.isStatus() || !version.getProduct().isStatus()) {
			response.setCode(404);
			response.setMessage("Phiên bản hoặc sản phẩm không còn tồn tại!");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		version.setStatus(false);
		versionJPA.save(version);

		response.setCode(200);
		response.setData(true);
		response.setMessage("Xóa phiên bản thành công!");
		return ResponseEntity.ok(response);
	}

}
