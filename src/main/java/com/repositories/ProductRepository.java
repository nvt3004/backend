package com.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.responsedto.ProductDTO;

@Repository
public class ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings("deprecation")
	public List<ProductDTO> callGetFilteredProducts(
			Integer categoryId,
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Integer color, 
            Integer size, 
            String sortPrice) {

        String sql = "{CALL GetFilteredProducts(? ,?, ?, ?, ?, ?)}";
        
        return jdbcTemplate.query(
            sql,
            new Object[] {
            	categoryId != null ? categoryId : null,
                minPrice != null ? minPrice : null,
                maxPrice != null ? maxPrice : null,
                color != null ? color : null,
                size != null ? size : null,
                sortPrice
            },
            (rs, rowNum) -> {
                ProductDTO dto = new ProductDTO();
                dto.setId(rs.getString("product_id"));
                dto.setName(rs.getString("product_name"));
                dto.setImg(rs.getString("product_img"));
                dto.setMinPrice(rs.getBigDecimal("min_price"));
                dto.setMaxPrice(rs.getBigDecimal("max_price"));
                return dto;
            }
        );
    }
}
