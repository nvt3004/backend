package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.entities.PaymentMethod;

public interface PaymentMethodJPA extends JpaRepository<PaymentMethod, Integer> {

}
