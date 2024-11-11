package com.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 255, message = "Address cannot be longer than 255 characters")
    private String address;

    @NotBlank(message = "Contact name cannot be blank")
    @Size(max = 100, message = "Contact name cannot be longer than 100 characters")
    private String contactName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Active status cannot be null")
    private Boolean isActive;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(max = 20, message = "Phone number cannot be longer than 20 characters")
    private String phone;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(max = 100, message = "Supplier name cannot be longer than 100 characters")
    private String supplierName;
}
