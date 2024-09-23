package com.services;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchConfig;
import com.algolia.search.SearchIndex;
import com.algolia.search.models.indexing.Query;
import com.algolia.search.models.indexing.SearchResult;
import com.responsedto.ProductDTO;
import com.utils.IdApikeyAIgolia;

import jakarta.annotation.PreDestroy;

@Service
public class AlgoliaProductService {

    private static final Logger logger = Logger.getLogger(AlgoliaProductService.class.getName());

    private final SearchClient searchClient;
    private final SearchIndex<ProductDTO> productIndex; // Thay Object bằng Product

    @Autowired
    public AlgoliaProductService(IdApikeyAIgolia idApikeyAIgolia) {
      
        // Khởi tạo SearchClient với SearchConfig
        this.searchClient = DefaultSearchClient.create(idApikeyAIgolia.getApplicationId(), idApikeyAIgolia.getAdminApiKey());

        // Khởi tạo index "products"
        this.productIndex = searchClient.initIndex("products", ProductDTO.class); // Thay Object bằng Product
    }

    // Thêm sản phẩm vào Algolia
    public void addProduct(ProductDTO product) {
        try {
            productIndex.saveObject(product).waitTask();
            logger.info("Thêm sản phẩm thành công: " + product);
        } catch (Exception e) {
            logger.severe("Lỗi khi thêm sản phẩm vào Algolia: " + e.getMessage());
        }
    }

    // Tìm kiếm sản phẩm từ Algolia
    public List<ProductDTO> searchProducts(Query query) {
        try {
            SearchResult<ProductDTO> searchResult = productIndex.search(query);
            return searchResult.getHits();
        } catch (Exception e) {
            logger.severe("Lỗi khi tìm kiếm sản phẩm từ Algolia: " + e.getMessage());
            return List.of(); // Trả về danh sách rỗng nếu có lỗi
        }
    }

    // Đóng client khi không còn sử dụng
    @PreDestroy
    public void closeClient() {
        try {
            searchClient.close();
            logger.info("Đóng SearchClient thành công.");
        } catch (IOException e) {
            logger.severe("Lỗi khi đóng SearchClient: " + e.getMessage());
        }
    }
}