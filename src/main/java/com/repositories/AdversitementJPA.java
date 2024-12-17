package com.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.entities.Address;
import com.entities.Advertisement;

public interface AdversitementJPA extends JpaRepository<Advertisement, Integer>{
    Page<Advertisement> findByStatus(byte status, Pageable pageable);

    @Query("SELECT a FROM Advertisement a WHERE CURRENT_DATE BETWEEN a.startDate AND a.endDate")
    List<Advertisement> findAdvertisementsForToday();

    @Query("SELECT a FROM Advertisement a WHERE a.status = 1")
    List<Advertisement> findAllAdvertisementsWithStatus1();
}