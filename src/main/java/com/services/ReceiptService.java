package com.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.entities.Image;
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
import com.responsedto.ReceiptDetailResponse;
import com.responsedto.ReceiptResponse;
import com.utils.UploadService;

@Service
public class ReceiptService {

	@Autowired
	private ReceiptJPA receiptJpa;

	@Autowired
	private ReceiptDetailJPA receiptDetailJpa;

	@Autowired
	private ProductVersionService productVersionService;

	@Autowired
	private SupplierService supplierService;

	@Autowired
	private UploadService uploadService;

	public Optional<Receipt> getWarehousById(int id) {
		return receiptJpa.findById(id);
	}

//	public Page<ReceiptInfoDTO> getAllWarehouses(int page, int size, String keyword) {
//		
//		Pageable pageable = PageRequest.of(page, size);
//		Page<Receipt> receiptPage = receiptJpa.findByKeyword(keyword, pageable);
//
//		List<ReceiptInfoDTO> receiptDTOList = new ArrayList<>();
//
//		for (Receipt receipt : receiptPage.getContent()) {
//			ReceiptInfoDTO receiptDTO = convertReceipt(receipt);
//			receiptDTOList.add(receiptDTO);
//		}
//
//		return new PageImpl<>(receiptDTOList, pageable, receiptPage.getTotalElements());
//	}

//	private ReceiptInfoDTO convertReceipt(Receipt receipt) {
//		ReceiptInfoDTO receiptInfoDTO = new ReceiptInfoDTO();
//		receiptInfoDTO.setReceiptId(receipt.getReceiptId());
//		receiptInfoDTO.setReceiptDate(receipt.getReceiptDate());
//		receiptInfoDTO.setSupplierName(receipt.getSupplier().getSupplierName());
//		receiptInfoDTO.setUsername(receipt.getUser().getUsername());
//		return receiptInfoDTO;
//	}

	public ReceiptDTO getWarehouseById(Integer id) {
		Receipt receipt = receiptJpa.findById(id).orElse(null);
		if (receipt == null) {
			return null;
		}
		return convertReceiptById(receipt);
	}

	private ReceiptDTO convertReceiptById(Receipt receipt) {
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
				Image images = productVersion.getImage();
				String imageUrl = null;
				if (images != null) {
					imageUrl = images.getImageUrl();
				}
				productDTO.setProductVersionId(productVersion.getId());
				productDTO.setVersionImage(uploadService.getUrlImage(imageUrl));
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
			BigDecimal price = pvDto.getPrice();
			ProductVersion prodVer = productVersionService.getProductVersionByID(productVersionId);
			if (prodVer == null) {
				errorResponse.setErrorCode(400);
				errorResponse.setMessage("ProductVersion with ID " + productVersionId + " does not exist.");
				return errorResponse;
			}

			int inventoryProductVersion = prodVer.getQuantity();
			prodVer.setQuantity(inventoryProductVersion + quantity);
			productVersionService.updateProdVerSion(prodVer);

			ReceiptDetail receiptDetail = new ReceiptDetail();
			receiptDetail.setReceipt(receipt);
			receiptDetail.setProductVersion(prodVer);
			receiptDetail.setQuantity(quantity);
			receiptDetail.setPrice(price);
			receiptDetailJpa.save(receiptDetail);
		}

		ApiResponse<?> response = new ApiResponse<>(200, "Receipt created successfully", null);
		return response;
	}

	public Page<ReceiptResponse> getAllWarehousesStf(int page, int size, String keyword) {
		Sort sort = Sort.by(Sort.Direction.DESC, "receiptId");
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Receipt> receiptPage = receiptJpa.findByKeyword(keyword, pageable);

		List<ReceiptResponse> receiptDTOList = new ArrayList<>();

		for (Receipt receipt : receiptPage.getContent()) {
			System.out.println(receipt.getReceiptId() + " ReceiptIdEEE");
			ReceiptResponse receiptDTO = convertReceiptStf(receipt);
			receiptDTOList.add(receiptDTO);
		}

		return new PageImpl<>(receiptDTOList, pageable, receiptPage.getTotalElements());
	}

	private ReceiptResponse convertReceiptStf(Receipt receipt) {
		ReceiptResponse receiptInfoDTO = new ReceiptResponse();

		receiptInfoDTO.setReceiptId(receipt.getReceiptId());
		receiptInfoDTO.setReceiptDate(receipt.getReceiptDate());
		receiptInfoDTO.setSupplierName(receipt.getSupplier().getSupplierName());
		receiptInfoDTO.setSupplierEmail(receipt.getSupplier().getEmail());
		receiptInfoDTO.setSupplierPhone(receipt.getSupplier().getPhone());
		receiptInfoDTO.setSupplierAddress(receipt.getSupplier().getAddress());
		receiptInfoDTO.setUsername(receipt.getUser().getUsername());
		receiptInfoDTO.setFullname(receipt.getUser().getFullName());

		List<ReceiptDetailResponse> details = new ArrayList<>();

		BigDecimal totalPrice = BigDecimal.ZERO;
		int totalQuantity = 0;

		for (ReceiptDetail dt : receipt.getReceiptDetails()) {
			ReceiptDetailResponse detail = new ReceiptDetailResponse();
			BigDecimal total = dt.getPrice().multiply(BigDecimal.valueOf(dt.getQuantity()));

			if (dt.getProductVersion().getImage() != null) {
				detail.setImage(uploadService.getUrlImage(dt.getProductVersion().getImage().getImageUrl()));
			}

			detail.setImportPrice(dt.getPrice());
			detail.setName(dt.getProductVersion().getVersionName());
			detail.setQuantity(dt.getQuantity());
			detail.setTotal(total);

			details.add(detail);

			if (dt.getProductVersion().getImage() != null) {
				detail.setImage(uploadService.getUrlImage(dt.getProductVersion().getImage().getImageUrl()));
			}
			detail.setImportPrice(dt.getPrice());
			detail.setName(dt.getProductVersion().getVersionName());
			detail.setQuantity(dt.getQuantity());
			detail.setTotal(total);

			details.add(detail);

			totalQuantity += dt.getQuantity();
			totalPrice = totalPrice.add(total);
		}

		receiptInfoDTO.setTotalQuantity(totalQuantity);
		receiptInfoDTO.setTotalPrice(totalPrice);
		receiptInfoDTO.setDetais(details);

		return receiptInfoDTO;
	}

}
