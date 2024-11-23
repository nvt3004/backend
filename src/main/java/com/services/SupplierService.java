package com.services;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.entities.Supplier;
import com.errors.ApiResponse;
import com.models.GetAllSupplierDTO;
import com.models.SupplierDTO;
import com.repositories.ReceiptJPA;
import com.repositories.SupplierJPA;
import com.utils.ExcelUtil;

@Service
public class SupplierService {

	@Autowired
	private SupplierJPA supplierJpa;

	@Autowired
	private ReceiptJPA receiptJpa;

	public Page<Supplier> getAllSuppliers(String keyword, Boolean status,Pageable pageable) {
		return supplierJpa.findSuppliersByCriteria(keyword,status, pageable);
	}
	
	public List<GetAllSupplierDTO> getAllSuppliersWithoutPagination() {
	    return supplierJpa.findAllActiveSupplierNamesAndIds(); 
	}


	public Optional<Supplier> getSupplierById(int id) {
		return supplierJpa.findById(id);
	}

	public Supplier createSupplier(SupplierDTO supplierDetails) {
		Supplier supplier = new Supplier();
		supplier.setAddress(supplierDetails.getAddress());
		supplier.setContactName(supplierDetails.getContactName());
		supplier.setEmail(supplierDetails.getEmail());
		supplier.setStatus(supplierDetails.getIsActive());
		supplier.setPhone(supplierDetails.getPhone());
		supplier.setSupplierName(supplierDetails.getSupplierName());
		
		return supplierJpa.save(supplier);
	}

	public Supplier updateSupplier(int id, SupplierDTO supplierDetails) {
		Supplier supplier = supplierJpa.findById(id).orElseThrow(() -> new RuntimeException("Supplier not found"));
		supplier.setAddress(supplierDetails.getAddress());
		supplier.setContactName(supplierDetails.getContactName());
		supplier.setEmail(supplierDetails.getEmail());
		supplier.setStatus(supplierDetails.getIsActive());
		supplier.setPhone(supplierDetails.getPhone());
		supplier.setSupplierName(supplierDetails.getSupplierName());
		return supplierJpa.save(supplier);
	}

	public ApiResponse<?> deleteSupplier(Integer id) {
	    if (receiptJpa.existsBySupplierSupplierId(id)) {
	        return new ApiResponse<>(400, "Cannot delete Supplier, it is referenced in a receipt.", null);
	    }

	    Optional<Supplier> supplier = supplierJpa.findById(id);

	    if (supplier.isPresent()) {
	        Supplier existingSupplier = supplier.get();
	        existingSupplier.setStatus(false);
	        supplierJpa.save(existingSupplier);

	        return new ApiResponse<>(200, "Supplier marked as deleted successfully.", null);
	    } else {
	        return new ApiResponse<>(404, "Supplier with ID " + id + " not found.", null);
	    }
	}
	
	public ApiResponse<?> restoreSupplier(Integer id) {
	    Optional<Supplier> supplier = supplierJpa.findById(id);

	    if (supplier.isPresent()) {
	        Supplier existingSupplier = supplier.get();
	        if (existingSupplier.getStatus() == false) {
	            existingSupplier.setStatus(true);
	            supplierJpa.save(existingSupplier);
	            return new ApiResponse<>(200, "Supplier restored successfully.", null);
	        } else {
	            return new ApiResponse<>(400, "Supplier is already active.", null);
	        }
	    } else {
	        return new ApiResponse<>(404, "Supplier with ID " + id + " not found.", null);
	    }
	}
	
	public ByteArrayResource exportSuppliersToExcel(String keyword, Boolean status, Pageable pageable) {
	    try {

	        Page<Supplier> suppliersResponse = this.getAllSuppliers(keyword, status, pageable);

	        String[] headers = {"Supplier ID", "Supplier Name", "Contact Name", "Email", "Phone", "Address", "Status"};
	        Object[][] data = suppliersResponse.getContent().stream().map(supplier -> {
	            return new Object[]{
	                    supplier.getSupplierId(),
	                    supplier.getSupplierName(),
	                    supplier.getContactName(),
	                    supplier.getEmail(),
	                    supplier.getPhone(),
	                    supplier.getAddress(),
	                    supplier.getStatus() ? "Active" : "Inactive"
	            };
	        }).toArray(Object[][]::new);

	        ByteArrayOutputStream outputStream = ExcelUtil.createExcelFile("Suppliers", headers, data);

	        return new ByteArrayResource(outputStream.toByteArray());
	    } catch (Exception e) {
	        throw new RuntimeException("An error occurred while exporting suppliers to Excel: " + e.getMessage(), e);
	    }
	}



}
