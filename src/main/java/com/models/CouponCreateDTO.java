package com.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CouponCreateDTO {

    @DecimalMin(value = "5.0", message = "Discount percentage must be at least 5%.")
    @DecimalMax(value = "50.0", message = "Discount percentage cannot exceed 50%.")
    private BigDecimal disPercent;

    @DecimalMin(value = "5000.0", message = "Discount price must be at least 5000.0.")
    @DecimalMax(value = "100000.0", message = "Discount price cannot exceed 100,000 units.")
    private BigDecimal disPrice;

    @Size(max = 255, message = "Description cannot exceed 255 characters.")
    private String description;

    @NotNull(message = "Start date cannot be null.")
    @FutureOrPresent(message = "Start date must be today or in the future.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @NotNull(message = "End date cannot be null.")
    @Future(message = "End date must be in the future.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    @NotNull(message = "Quantity cannot be null.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private Integer quantity;

    public boolean isDatesValid() {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }

    public boolean isDiscountValid() {
        return (disPercent == null && disPrice != null) || (disPercent != null && disPrice == null);
    }

    // Hàm kiểm tra xem disPercent có rỗng hoặc null không
    public boolean isDisPercentValid() {
        return disPercent != null && !disPercent.toString().trim().isEmpty();
    }

    // Hàm kiểm tra xem disPrice có rỗng hoặc null không
    public boolean isDisPriceValid() {
        return disPrice != null && !disPrice.toString().trim().isEmpty();
    }
}
