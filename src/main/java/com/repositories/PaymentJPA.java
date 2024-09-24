package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entities.Payment;

public interface PaymentJPA extends JpaRepository<Payment, Integer> {

}
