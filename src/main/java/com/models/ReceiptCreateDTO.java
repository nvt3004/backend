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

    @NotNull(message = "Mã nhà cung cấp là bắt buộc.")
    @Min(value = 1, message = "Mã nhà cung cấp phải lớn hơn hoặc bằng 1.")
    private Integer supplierId;

    @NotNull(message = "Mô tả là bắt buộc.")
    @Size(min = 5, max = 255, message = "Mô tả phải từ 5 đến 255 ký tự.")
    private String description;

    @NotEmpty(message = "Phiên bản sản phẩm là bắt buộc.")
    @Valid
    private List<ProductVersionDTO> productVersions;

    @Data
    public static class ProductVersionDTO {

        @NotNull(message = "Mã phiên bản sản phẩm là bắt buộc.")
        @Min(value = 1, message = "Mã phiên bản sản phẩm phải lớn hơn hoặc bằng 1.")
        private Integer productVersionId;

        @NotNull(message = "Số lượng là bắt buộc.")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0.")
        private Integer quantity;

        @NotNull(message = "Giá là bắt buộc.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0.")
        private BigDecimal price;
    }
}

