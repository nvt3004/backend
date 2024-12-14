package com.responsedto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleResponse {
    private Integer id;
    private String saleName;
    private BigDecimal percent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Version> versions;
    private int active;
    private boolean status;
}
