package com.repositories;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.responsedto.SaleProductDTO;
import com.services.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.entities.Product;
import com.entities.ProductVersion;
import com.responsedto.PageCustom;
import com.responsedto.ProductHomeResponse;
import com.services.VersionService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.IdClass;
import jakarta.persistence.Query;

@Repository
public class ProductCustomJPA {

	@Autowired
	private EntityManager entityManager;
	
	@Autowired 
	private ProductJPA productJPA;
	
	@Autowired
	VersionService vsService;

	@Autowired
	SaleService saleService;

	private final String SQL_GET_ALL_PRODUCT = "SELECT pd.product_id AS id, \r\n"
			+ "       pd.product_name AS productName, \r\n"
			+ "       pd.product_img AS image, \r\n"
			+ "       IFNULL(discount, 0) AS discount , \r\n"
			+ "       MIN(vs.retail_price) AS minPrice,\r\n"
			+ "       MAX(vs.retail_price) AS maxPrice,\r\n"
			+ "		  pd.status AS active, \r\n"		
			+ "		  1 AS inStock, \r\n"	
			+ "		  pd.description AS description, \r\n"
			+ "		  0.00 AS minSale, \r\n"
			+ "		  0.00 AS maxSale \r\n"
			+ "FROM products pd \r\n"
			+ "LEFT JOIN product_sales sale ON pd.product_id = sale.product_id \r\n"
			+ "INNER JOIN product_version vs ON pd.product_id = vs.product_id\r\n"
			+ "WHERE \r\n"
			+ "	pd.status = true \r\n"
			+ "    AND \r\n"
			+ "    vs.status = true\r\n"
			+ "GROUP BY pd.product_id, \r\n"
			+ "		 pd.product_name, \r\n"
			+ "         pd.product_price, \r\n"
			+ "         pd.product_img,discount, \r\n"
			+ "         vs.product_id;";

	private final String SQL_GET_ALL_PRODUCT_BY_CATEGORY = "SELECT pd.product_id AS id, pd.product_name AS productName, pd.product_price AS price,"	
			+ " pd.product_img AS image, IFNULL(discount,0) AS discount," + " pd.status AS active,"+ " 1 AS inStock,"+ " pd.description as description,"+ "		  0.00 AS minSale, \r\n"
			+ "		  0.00 AS maxSale \r\n"+
			" FROM products pd"
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

		
		for(ProductHomeResponse res : products) {
			Product pd = productJPA.findById(Long.valueOf(res.getId()).intValue()).get();
			BigDecimal minSale = new BigDecimal(0);
			BigDecimal maxSale = new BigDecimal(0);
			boolean inFirst = true;
			int totalStockVersion = 0;
			
			for(ProductVersion vs : pd.getProductVersions()) {
				int quantity = vsService.getTotalStockQuantityVersion(vs.getId());
				SaleProductDTO sale = saleService.getVersionSaleDTO(vs.getId());

				totalStockVersion += quantity;

				if(sale != null && inFirst) {
					minSale = sale.getSale();
					maxSale = sale.getSale();
					inFirst = false;
				}

				if(sale != null && sale.getSale().compareTo(minSale)==-1) {
					minSale = sale.getSale();
				}

				if(sale != null && sale.getSale().compareTo(maxSale)==1) {
					maxSale = sale.getSale();
				}
			}

			res.setMinSale(minSale);
			res.setMaxSale(maxSale);
			res.setInStock(Long.valueOf(totalStockVersion));
		}

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
		
		for(ProductHomeResponse res : products) {
			Product pd = productJPA.findById(Long.valueOf(res.getId()).intValue()).get();
			
			int totalStockVersion = 0;
			
			for(ProductVersion vs : pd.getProductVersions()) {
				int quantity = vsService.getTotalStockQuantityVersion(vs.getId());
				totalStockVersion += quantity;
			}
			res.setInStock(Long.valueOf(totalStockVersion));
		}

		return new PageCustom<ProductHomeResponse>(totalPage, products.size(), allProduct.size(), products);
	}
	


}
