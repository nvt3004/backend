package com.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderByUserDTO {
    private Integer orderId;
    private Date orderDate;
    private String statusName;
    private BigDecimal totalPrice;
    private BigDecimal discountedPrice;
    private List<ProductDTO> products;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDTO {
        private Integer productId;
        private String productName;
        private Integer feedBack;
        private String imageUrl;
        private String variant; 
        private Integer quantity;
        private BigDecimal price; 
    }
}
