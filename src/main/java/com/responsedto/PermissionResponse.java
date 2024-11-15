package com.responsedto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
	private Integer userId;
	private String title;
	private List<PermissionDto> permission;
	public PermissionResponse(String title, List<PermissionDto> permission) {
		super();
		this.title = title;
		this.permission = permission;
	}
}
