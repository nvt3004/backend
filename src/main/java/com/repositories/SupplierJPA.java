package com.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.entities.Supplier;
import com.models.GetAllSupplierDTO;

@Repository
public interface SupplierJPA extends JpaRepository<Supplier, Integer> {
	Page<Supplier> findAll(Pageable pageable);
	
	@Query("SELECT new com.models.GetAllSupplierDTO(s.supplierId, s.supplierName) FROM Supplier s ORDER BY s.supplierName ASC")
	List<GetAllSupplierDTO> findAllSupplierNamesAndIds();

}
