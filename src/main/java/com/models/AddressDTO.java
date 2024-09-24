package com.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
	private Integer addressId;
	private String province;
	private String district;
	private String ward;
	private String detailAddress;
}
