package com.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
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

}
