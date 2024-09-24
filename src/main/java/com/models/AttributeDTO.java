package com.models;

import java.util.List;

import lombok.Data;

@Data
public class AttributeDTO {
	private List<ColorDTO> colors;
	private List<SizeDTO> sizes;
	private ColorDTO colorDTO;
	private SizeDTO sizeDTO;

	private String color;
	private String size;
	private Integer colorId; 
	private Integer sizeId; 

	public AttributeDTO(List<ColorDTO> colors, List<SizeDTO> sizes) {
		this.colors = colors;
		this.sizes = sizes;
	}
	public AttributeDTO(ColorDTO colorDTO,SizeDTO sizeDTO ) {
		this.colorDTO = colorDTO;
		this.sizeDTO = sizeDTO;
	}
	public AttributeDTO(Integer colorId, String color, Integer sizeId, String size) {
		this.colorId = colorId;
		this.color = color;
		this.sizeId = sizeId;
		this.size = size;
	}
}
