package com.responsedto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCartResponse {
	private int cartId;
	private int userId;
	private CartDetailResponse cartItem;
}
