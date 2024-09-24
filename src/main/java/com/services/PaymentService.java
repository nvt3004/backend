package com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Payment;
import com.repositories.PaymentJPA;

@Service
public class PaymentService {
	@Autowired
	PaymentJPA paymentJPA;
	
	public Payment createPayment(Payment payment) {
		return paymentJPA.save(payment);
	}
	
	public boolean deletePayment(Payment payment) {
		try {
			paymentJPA.delete(payment);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}	
