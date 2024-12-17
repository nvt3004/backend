package com.responsedto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistProductRes {
    private Integer idProduct;
    private String productName;
    private String image;
    private Integer like;
}
