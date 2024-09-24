package com.configs;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.responsedto.ProductDTO;
import com.services.AlgoliaProductService;
import com.services.ProductInforService;

@Component
public class ProductUpdateScheduler {

	private static final Logger logger = Logger.getLogger(ProductUpdateScheduler.class.getName());

	@Autowired
	private AlgoliaProductService algoliaProductService;

	@Autowired
	private ProductInforService inforService;

	//@Scheduled(cron = "*/10 * * * * ?") // 10s
	// Lên lịch thực hiện mỗi ngày vào lúc 23:55
	@Scheduled(cron = "0 55 23 * * ?") // Giờ - Phút - Giây, mỗi ngày vào lúc
	// 23:55
	public void updateProductsToAlgolia() {
		try {
			// Lấy danh sách tất cả các sản phẩm từ dịch vụ
			List<ProductDTO> products = inforService.getALLInforProduct();
			// Thêm hoặc cập nhật tất cả sản phẩm vào Algolia
			for (ProductDTO product : products) {
				algoliaProductService.addProduct(product);
			}
			logger.info("Cập nhật sản phẩm lên Algolia thành công.");
		} catch (Exception e) {
			logger.severe("Lỗi khi cập nhật sản phẩm lên Algolia: " + e.getMessage());
		}
	}
}
