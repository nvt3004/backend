package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.ReceiptDetail;

public interface ReceiptDetailJPA extends JpaRepository<ReceiptDetail, Integer> {
	@Query("SELECT SUM(rd.quantity) FROM ReceiptDetail rd WHERE rd.productVersion.id = :productVersionId")
	Integer getTotalQuantityForProductVersion(@Param("productVersionId") Integer productVersionId);
}
