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
    private Date expectedDeliveryDate;
    private String statusName;
    private Integer couponId;
	private String disCount;
	private BigDecimal discountValue;
	private BigDecimal subTotal;
	private BigDecimal shippingFee;
	private BigDecimal finalTotal;
	private String finalTotalInWords;
    private List<ProductDTO> products;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDTO {
        private Integer productId;
        private Integer orderDetailId;
        private Boolean isFeedback;
        private String productName;
        private String imageUrl;
        private String variant; 
        private Integer quantity;
        private BigDecimal price; 
    }
}
