package com.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Receipt;

public interface ReceiptJPA extends JpaRepository<Receipt, Integer> {
	@Query("SELECT r FROM Receipt r ORDER BY r.receiptDate DESC")
	Page<Receipt> findAll(Pageable pageable);
	
	@Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Receipt r WHERE r.supplier.supplierId = :supplierId")
	boolean existsBySupplierSupplierId(@Param("supplierId") Integer supplierId);
}
