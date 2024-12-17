package com.models;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ReceiptCreateDTO {
    @NotNull(message = "Supplier ID is required.")
    @Min(value = 1, message = "Supplier ID must be greater than or equal to 1.")
    private Integer supplierId;

    @NotNull(message = "Description is required.")
    @Size(min = 5, max = 255, message = "Description must be between 5 and 255 characters.")
    private String description;

    @NotEmpty(message = "Product versions are required.")
    @Valid
    private List<ProductVersionDTO> productVersions;

    @Data
    public static class ProductVersionDTO {
        @NotNull(message = "Product version ID is required.")
        @Min(value = 1, message = "Product version ID must be greater than or equal to 1.")
        private Integer productVersionId;

        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be greater than 0.")
        private Integer quantity;

        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0.")
        private BigDecimal price;
    }
}

