package com.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.OrderStatus;
import com.models.OrderStatusDTO;
import com.repositories.OrderStatusJPA;

@Service
public class OrderStatusService {

	@Autowired
	private OrderStatusJPA orderStatusJpa;

	public List<OrderStatusDTO> getAllOrderStatusDTOs() {
	    List<OrderStatus> orderStatuses = orderStatusJpa.getStatusOrder();
	    List<OrderStatusDTO> orderStatusDTOList = new ArrayList<>();

	    for (OrderStatus orderStatus : orderStatuses) {
	       
	        if ("Temp".equalsIgnoreCase(orderStatus.getStatusName())) {
	            continue;
	        }
	        
	        OrderStatusDTO dto = new OrderStatusDTO();
	        dto.setStatusId(orderStatus.getStatusId());
	        dto.setStatusName(orderStatus.getStatusName());
	        orderStatusDTOList.add(dto);
	    }

	    return orderStatusDTOList;
	}


	public Optional<OrderStatus> getOrderStatusById(Integer statusId) {
		return orderStatusJpa.findById(statusId);
	}

	public OrderStatus findByName(String statusName) {
		return orderStatusJpa.findByStatusName(statusName)
				.orElseThrow(() -> new RuntimeException("Order status not found"));
	}
}
