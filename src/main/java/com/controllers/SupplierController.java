package com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Supplier;
import com.errors.ApiResponse;
import com.errors.FieldErrorDTO;
import com.models.SupplierModel;
import com.services.SupplierService;
import com.utils.ValidationUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

	@Autowired
	private SupplierService supplierService;

	@GetMapping
	public ResponseEntity<ApiResponse<Page<Supplier>>> getAllSuppliers(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		if (page < 0) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid page number. It must be greater than or equal to 0.", null));
		}

		if (size < 1) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid size. It must be greater than or equal to 1.", null));
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<Supplier> supplierPage = supplierService.getAllSuppliers(pageable);

		ApiResponse<Page<Supplier>> response = new ApiResponse<>(200, "Suppliers retrieved successfully", supplierPage);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/supplier-detail")
	public ResponseEntity<ApiResponse<Supplier>> getSupplierById(@RequestParam Integer id) {
		Optional<Supplier> optionalSupplier = supplierService.getSupplierById(id);

		if (optionalSupplier.isPresent()) {
			Supplier supplier = optionalSupplier.get();
			ApiResponse<Supplier> response = new ApiResponse<>(200, "Supplier found successfully", supplier);
			return ResponseEntity.ok(response);
		} else {
			ApiResponse<Supplier> response = new ApiResponse<>(404, "Supplier not found with id " + id, null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	@PostMapping
	public ResponseEntity<ApiResponse<?>> createSupplier(@Valid @RequestBody SupplierModel supplierDetails,
			BindingResult errors) {

		ApiResponse<?> errorResponse = new ApiResponse<>();
		List<FieldErrorDTO> validationErrors = ValidationUtil.validateErrors(errors);
		if (!validationErrors.isEmpty()) {
			errorResponse = new ApiResponse<>(400, "Validation failed.", validationErrors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
		Supplier createdSupplier = supplierService.createSupplier(supplierDetails);

		errorResponse = new ApiResponse<>(201, "Supplier created successfully", createdSupplier);

		return ResponseEntity.status(HttpStatus.CREATED).body(errorResponse);
	}

	@PutMapping
	public ResponseEntity<ApiResponse<?>> updateSupplier(@RequestParam Integer id, @Valid
			@RequestBody SupplierModel supplierDetails, BindingResult errors) {

		ApiResponse<?> errorResponse = new ApiResponse<>();
		List<FieldErrorDTO> validationErrors = ValidationUtil.validateErrors(errors);
		if (!validationErrors.isEmpty()) {
			errorResponse = new ApiResponse<>(400, "Validation failed.", validationErrors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
		Supplier updatedSupplier = supplierService.updateSupplier(id, supplierDetails);

		errorResponse = new ApiResponse<>(200, "Supplier updated successfully", updatedSupplier);

		return ResponseEntity.ok(errorResponse);
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> deleteSupplier(@RequestParam Integer id) {
		supplierService.deleteSupplier(id);

		ApiResponse<Void> response = new ApiResponse<>(200, "Supplier deleted successfully", null);

		return ResponseEntity.ok(response);
	}

}
