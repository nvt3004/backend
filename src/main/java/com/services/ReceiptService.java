package com.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.entities.ProductVersion;
import com.entities.Receipt;
import com.entities.ReceiptDetail;
import com.entities.Supplier;
import com.entities.User;
import com.errors.ApiResponse;
import com.errors.FieldErrorDTO;
import com.models.ReceiptCreateDTO;
import com.models.ReceiptDTO;
import com.repositories.ReceiptDetailJPA;
import com.repositories.ReceiptJPA;

@Service
public class ReceiptService {
//
//	@Autowired
//	private WarehousJPA warehousJpa;

	@Autowired
	private ReceiptJPA receiptJpa;

	@Autowired
	private ReceiptDetailJPA receiptDetailJpa;

	@Autowired
	private ProductVersionService productVersionService;

	@Autowired
	private SupplierService supplierService;

	public Optional<Receipt> getWarehousById(int id) {
		return receiptJpa.findById(id);
	}

	public Page<ReceiptDTO> getAllWarehouses(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Receipt> receiptPage = receiptJpa.findAll(pageable);

		List<ReceiptDTO> receiptDTOList = new ArrayList<>();

		for (Receipt receipt : receiptPage.getContent()) {
			ReceiptDTO receiptDTO = convertReceipt(receipt);
			receiptDTOList.add(receiptDTO);
		}

		return new PageImpl<>(receiptDTOList, pageable, receiptPage.getTotalElements());
	}

	public ReceiptDTO getWarehouseById(Integer id) {
		Receipt receipt = receiptJpa.findById(id).orElse(null);
		if (receipt == null) {
			return null;
		}
		return convertReceipt(receipt);
	}

	private ReceiptDTO convertReceipt(Receipt receipt) {
		ReceiptDTO receiptDTO = new ReceiptDTO();

		List<ReceiptDetail> receiptDetails = receipt.getReceiptDetails();
		List<ReceiptDTO.ReceiptDetailDTO> receiptDetailDTOs = new ArrayList<>();

		for (ReceiptDetail detail : receiptDetails) {
			ReceiptDTO.ReceiptDetailDTO dto = receiptDTO.new ReceiptDetailDTO();
			dto.setReceiptDetailId(detail.getReceiptDetailId());
			dto.setQuantity(detail.getQuantity());

			ReceiptDTO.ProductVersionDTO productDTO = receiptDTO.new ProductVersionDTO();
			ProductVersion productVersion = detail.getProductVersion();
			if (productVersion != null) {
				productDTO.setProductVersionId(productVersion.getId());
				productDTO.setProductVersionName(productVersion.getVersionName());
			}
			dto.setProductVersionDTO(productDTO);
			receiptDetailDTOs.add(dto);
		}

		receiptDTO.setReceiptId(receipt.getReceiptId());
		receiptDTO.setReceiptDate(receipt.getReceiptDate());
		receiptDTO.setReceiptDetailDTO(receiptDetailDTOs);
		receiptDTO.setSupplierName(receipt.getSupplier().getSupplierName());
		receiptDTO.setUsername(receipt.getUser().getUsername());

		return receiptDTO;
	}

	public List<FieldErrorDTO> validateWarehouse(ReceiptCreateDTO receiptCreateDTO, BindingResult errors) {
		List<FieldErrorDTO> fieldErrors = new ArrayList<>();

		if (errors.hasErrors()) {
			for (ObjectError error : errors.getAllErrors()) {
				String field = ((FieldError) error).getField();
				String errorMessage = error.getDefaultMessage();
				fieldErrors.add(new FieldErrorDTO(field, errorMessage));
			}
		}

		return fieldErrors;
	}

	public ApiResponse<?> createReceipt(ReceiptCreateDTO dto, User user) {
		ApiResponse<?> errorResponse = new ApiResponse<>();
		Integer supplierId = dto.getSupplierId();
		Optional<Supplier> supp = supplierService.getSupplierById(supplierId);
		if (!supp.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Supplier does not exist.");
			return errorResponse;
		}

		Supplier supplier = supp.get();

		Receipt receipt = new Receipt();
		receipt.setSupplier(supplier);
		receipt.setReceiptDate(LocalDateTime.now());
		receipt.setUser(user);
		receipt.setSupplierName(supplier.getSupplierName());
		receiptJpa.save(receipt);

		for (ReceiptCreateDTO.ProductVersionDTO pvDto : dto.getProductVersions()) {
			Integer productVersionId = pvDto.getProductVersionId();
			Integer quantity = pvDto.getQuantity();

			ProductVersion prodVer = productVersionService.getProductVersionByID(productVersionId);
			if (prodVer == null) {
				errorResponse.setErrorCode(400);
				errorResponse.setMessage("ProductVersion with ID " + productVersionId + " does not exist.");
				return errorResponse;
			}

			// Update product inventory
			int inventoryProductVersion = prodVer.getQuantity();
			prodVer.setQuantity(inventoryProductVersion + quantity);
			productVersionService.updateProdVerSion(prodVer);

			// Save receipt details
			ReceiptDetail receiptDetail = new ReceiptDetail();
			receiptDetail.setReceipt(receipt);
			receiptDetail.setProductVersion(prodVer);
			receiptDetail.setQuantity(quantity);
			receiptDetailJpa.save(receiptDetail);
		}

		ApiResponse<?> response = new ApiResponse<>(200, "Receipt created successfully", null);
		return response;
	}

}
