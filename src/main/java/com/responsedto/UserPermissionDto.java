package com.responsedto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDto {
	private int userId;
	private Map<String, List<PermissionDto>> pers;
}
