package com.models;

import lombok.Data;

@Data
public class AttributeProductVersionDTO {
	private ColorDTO color;
	private SizeDTO size;

	public AttributeProductVersionDTO(ColorDTO colorDTO, SizeDTO sizeDTO) {
		this.color = colorDTO;
		this.size = sizeDTO;
	}
}
