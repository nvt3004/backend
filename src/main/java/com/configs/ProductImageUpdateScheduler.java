package com.configs;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.entities.Product;
import com.entities.ProductVector;
import com.entities.ProductVersion;
import com.repositories.ProductJPA;
import com.repositories.ProductVectorJPA;
import com.services.ConvertImageToVectorService;
@Component
public class ProductImageUpdateScheduler {
	private static final Logger logger = Logger.getLogger(ProductImageUpdateScheduler.class.getName());

	@Autowired
	ProductJPA productJPA;

	@Autowired
	ProductVectorJPA productVectorJPA;

	@Autowired
	ConvertImageToVectorService imgConvert;

	//@Scheduled(cron = "*/10 * * * * ?") // 10s
	@Scheduled(cron = "0 55 23 * * ?") // Giờ - Phút - Giây, mỗi ngày vào lúc
	// 23:55
	public void updateProductImage() {
		try {
			productVectorJPA.deleteAll();
			
			List<Product> products = productJPA.findAll();

			if (products == null || products.isEmpty()) {
				logger.info("Không có sản phẩm nào ở đây");
			} else {
				for (Product product : products) {
					if (!product.isStatus()) {
						continue; // Nếu sản phẩm không kích hoạt thì bỏ qua
					}

					logger.info("Đang xử lý sản phẩm ID: " + product.getProductId());
					for (ProductVersion productVer : product.getProductVersions()) {
						if (productVer.getImage() != null) {
							// Tạo ProductVector cho mỗi sản phẩm
							ProductVector productVector = new ProductVector();
							productVector.setProductId(product.getProductId());

							// Tạo URL ảnh và gọi phương thức imgConvert.getImageVector với URL
							String imageUrl = "http://localhost:8080/images/" + productVer.getImage().getImageUrl();
							String vector = imgConvert.getImageVector(imageUrl); // Gọi phương thức với URL ảnh
							productVector.setImageVector(vector); // Set chuỗi vector vào productVector

							// Lưu thông tin vào database
							productVectorJPA.save(productVector);
						}
					}
				}
				logger.info("Phân tích vector cho tất cả các sản phẩm hoàn tất.");
			}
		} catch (Exception e) {
			logger.severe("Lỗi khi phân tích vector: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
