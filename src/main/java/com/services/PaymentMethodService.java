package com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.PaymentMethod;
import com.repositories.PaymentMethodJPA;

@Service
public class PaymentMethodService {
	@Autowired
	PaymentMethodJPA paymentMethodJPA;
	
	public PaymentMethod getPaymentMethodById(int id) {
		return paymentMethodJPA.findById(id).orElse(null);
	}
}
