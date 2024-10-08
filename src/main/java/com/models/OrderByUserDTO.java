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
    private int orderId;
    private Date orderDate;
    private String statusName;
    private BigDecimal totalPrice; // total price of the order (before discount)
    private BigDecimal discountedPrice; // total price after applying discounts
    private List<ProductDTO> products; // list of products in the order

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDTO {
        private String productName;
        private String imageUrl;
        private String variant; // combination of color and size
        private Integer quantity;
        private BigDecimal price; // price of individual product
    }
}
