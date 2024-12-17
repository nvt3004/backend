package com.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

    @NotBlank(message = "Địa chỉ không được để trống.")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự.")
    private String address;

    @NotBlank(message = "Tên người liên hệ không được để trống.")
    @Size(max = 100, message = "Tên người liên hệ không được vượt quá 100 ký tự.")
    private String contactName;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email phải đúng định dạng.")
    private String email;

    private Boolean isActive = true;

    @NotBlank(message = "Số điện thoại không được để trống.")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự.")
    private String phone;

    @NotBlank(message = "Tên nhà cung cấp không được để trống.")
    @Size(max = 100, message = "Tên nhà cung cấp không được vượt quá 100 ký tự.")
    private String supplierName;

    private int id;
}

