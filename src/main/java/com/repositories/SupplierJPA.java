package com.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entities.Supplier;

@Repository
public interface SupplierJPA extends JpaRepository<Supplier, Integer> {
	Page<Supplier> findAll(Pageable pageable);
}
