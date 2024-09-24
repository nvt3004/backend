package com.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.responsedto.PageCustom;
import com.responsedto.ProductHomeResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.IdClass;
import jakarta.persistence.Query;

@Repository
public class ProductCustomJPA {

	@Autowired
	private EntityManager entityManager;

	private final String SQL_GET_ALL_PRODUCT = "SELECT pd.product_id AS id, pd.product_name AS productName, pd.product_price AS price,"
			+ " pd.product_img AS image, IFNULL(discount,0) AS discount" 
			+ " FROM products pd"
			+ " LEFT JOIN product_sales sale" 
			+ " ON pd.product_id  = sale.product_id WHERE pd.status = true";

	private final String SQL_GET_ALL_PRODUCT_BY_CATEGORY = "SELECT pd.product_id AS id, pd.product_name AS productName, pd.product_price AS price,"
			+ " pd.product_img AS image, IFNULL(discount,0) AS discount" + " FROM products pd"
			+ " INNER JOIN product_categories pdcat" + " ON pd.product_id = pdcat.product_id"
			+ " INNER JOIN categories cat" + " ON cat.category_id = pdcat.category_id" + " LEFT JOIN product_sales sale"
			+ " ON pd.product_id  = sale.product_id" + " WHERE cat.category_id =:idCat AND pd.status = true";
	
	
	public PageCustom<ProductHomeResponse> getAllProducts(int page, int size) {
		Query query = entityManager.createNativeQuery(SQL_GET_ALL_PRODUCT, ProductHomeResponse.class);
		List<ProductHomeResponse> allProduct = query.getResultList();

		if (page <= 0 || size <= 0) {
			return new PageCustom<ProductHomeResponse>(0, 0, 0, new ArrayList<ProductHomeResponse>());
		}

		int totalPage = (int) Math.ceil(Double.parseDouble(allProduct.size() + "") / Double.parseDouble(size + ""));

		if (page > totalPage) {
			return new PageCustom<ProductHomeResponse>(0, 0, 0, new ArrayList<ProductHomeResponse>());
		}

		query.setFirstResult((page - 1) * size);
		query.setMaxResults(size);
		List<ProductHomeResponse> products = query.getResultList();

		return new PageCustom<ProductHomeResponse>(totalPage, products.size(), allProduct.size(), products);
	}

	public PageCustom<ProductHomeResponse> getAllProductsByCategory(int page, int size, int categoryId) {
		Query query = entityManager.createNativeQuery(SQL_GET_ALL_PRODUCT_BY_CATEGORY, ProductHomeResponse.class);
		query.setParameter("idCat", categoryId);
		List<ProductHomeResponse> allProduct = query.getResultList();

		if (page <= 0 || size <= 0) {
			return new PageCustom<ProductHomeResponse>(0, 0, 0, new ArrayList<ProductHomeResponse>());
		}

		int totalPage = (int) Math.ceil(Double.parseDouble(allProduct.size() + "") / Double.parseDouble(size + ""));

		if (page > totalPage) {
			return new PageCustom<ProductHomeResponse>(0, 0, 0, new ArrayList<ProductHomeResponse>());
		}

		query.setFirstResult((page - 1) * size);
		query.setMaxResults(size);
		List<ProductHomeResponse> products = query.getResultList();

		return new PageCustom<ProductHomeResponse>(totalPage, products.size(), allProduct.size(), products);
	}
	


}
