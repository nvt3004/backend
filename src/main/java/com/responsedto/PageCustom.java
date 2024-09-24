package com.responsedto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageCustom<T> {
	private int totalPage;
	private int totalElementOnPage;
	private int totalElements;
	private List<T> contents;
}
