package com.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {
    private Integer id;
    private String code;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal disPercent;
    private BigDecimal disPrice;
    private String fullname;
    private LocalDateTime retrievalDate;
    private int quantity;

}
