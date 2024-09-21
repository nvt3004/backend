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

    public List<OrderStatus> getAllOrderStatuses() {
        return orderStatusJpa.findAll();
    }
    
    public Optional<OrderStatus> getOrderStatusById(Integer statusId){
    	return orderStatusJpa.findById(statusId);
    }
    
    public List<OrderStatusDTO> convertToDTO(List<OrderStatus> orderStatuses) {
        List<OrderStatusDTO> orderStatusDTOList = new ArrayList<>();
        
        for (OrderStatus orderStatus : orderStatuses) {
            OrderStatusDTO dto = new OrderStatusDTO();
            dto.setStatusId(orderStatus.getStatusId());
            dto.setStatusName(orderStatus.getStatusName());
            orderStatusDTOList.add(dto);
        }

        return orderStatusDTOList;
    }
}
