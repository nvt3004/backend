package com.responsedto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeResponse {
	private int id;
	private String attributeName;
	private List<OptinonResponse> options;
}
