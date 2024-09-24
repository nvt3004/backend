package com.models;

import java.util.List;

import lombok.Data;

@Data
public class OrderDetailCreateDTO {

	private List<Integer> productVersionId;
	private List<Integer> quantity;

}
