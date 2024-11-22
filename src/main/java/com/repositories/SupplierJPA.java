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

	@Query("""
		    SELECT s
		    FROM Supplier s
		    WHERE
		      (:keyword IS NULL OR :keyword = '' OR CAST(s.supplierId AS string) LIKE CONCAT('%', :keyword, '%') OR
		        s.supplierName LIKE CONCAT('%', :keyword, '%') OR
		        s.contactName LIKE CONCAT('%', :keyword, '%') OR
		        s.email LIKE CONCAT('%', :keyword, '%') OR
		        s.phone LIKE CONCAT('%', :keyword, '%') OR
		        s.address LIKE CONCAT('%', :keyword, '%')
		      )
		      AND (:status IS NULL OR s.status = :status)
		    ORDER BY s.supplierId DESC
		""")
		Page<Supplier> findSuppliersByCriteria(@Param("keyword") String keyword, @Param("status") Boolean status,
		        Pageable pageable);

	@Query("""
		    SELECT new com.models.GetAllSupplierDTO(
		        s.supplierId, 
		        s.supplierName
		    ) 
		    FROM Supplier s 
		    WHERE s.status = true 
		    ORDER BY s.supplierId DESC
		""")
		List<GetAllSupplierDTO> findAllActiveSupplierNamesAndIds();

}
