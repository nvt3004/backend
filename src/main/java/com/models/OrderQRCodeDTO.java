package com.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class OrderQRCodeDTO {
    private Integer orderId;
    private Integer gender;
    private String address;
    private Integer couponId;
    private String disCount;
    private BigDecimal discountValue;
    private BigDecimal subTotal;
    private BigDecimal shippingFee;
    private BigDecimal finalTotal;
    private String finalTotalInWords;
    private Date deliveryDate;
    private String fullname;
    private Date orderDate;
    private String phone;
    private String statusName;
    private String paymentMethod;
    private String phoneNumber;
    private String email;
    private List<OrderDetailProductDetailsDTO> productDetails;


    public OrderQRCodeDTO(Integer orderId, Integer gender, String address, Integer couponId, String disCount, BigDecimal discountValue,
                          BigDecimal subTotal, BigDecimal shippingFee, BigDecimal finalTotal, String finalTotalInWords,
                          Date deliveryDate, String fullname, Date orderDate, String phone, String statusName,
                      String paymentMethod, String phoneNumber, String email,
                          List<OrderDetailProductDetailsDTO> productDetails) {
        this.orderId = orderId;
        this.gender = gender;
        this.address = address;
        this.couponId = couponId;
        this.disCount = disCount;
        this.discountValue = discountValue;
        this.subTotal = subTotal;
        this.shippingFee = shippingFee;
        this.finalTotal = finalTotal;
        this.finalTotalInWords = finalTotalInWords;
        this.deliveryDate = deliveryDate;
        this.fullname = fullname;
        this.orderDate = orderDate;
        this.phone = phone;
        this.statusName = statusName;
        this.paymentMethod = paymentMethod;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.productDetails = productDetails;
    }
}
