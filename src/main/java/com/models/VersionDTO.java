package com.models;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionDTO {
	private Integer id;
	private String versionName;
	private BigDecimal retalPrice;
	private BigDecimal importPrice;
	private List<OptionDTO> attributes;
	private List<String> images;
}
