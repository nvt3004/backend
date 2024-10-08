package com.models;

import java.util.List;

import lombok.Data;

@Data
public class AttributeDTO {
	private List<ColorDTO> colors;
	private List<SizeDTO> sizes;

	public AttributeDTO(List<ColorDTO> colors, List<SizeDTO> sizes) {
		this.colors = colors;
		this.sizes = sizes;
	}
}
