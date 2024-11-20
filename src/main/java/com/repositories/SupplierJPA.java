package com.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.entities.Supplier;
import com.models.GetAllSupplierDTO;

@Repository
public interface SupplierJPA extends JpaRepository<Supplier, Integer> {
	
	@Query("SELECT s FROM Supplier s WHERE s.status = :status")
	Page<Supplier> findAllByStatus(Pageable pageable,@Param("status") Boolean status);
	
	@Query("SELECT new com.models.GetAllSupplierDTO(s.supplierId, s.supplierName) FROM Supplier s WHERE s.status = true ORDER BY s.supplierName DESC")
	List<GetAllSupplierDTO> findAllActiveSupplierNamesAndIds();

}
