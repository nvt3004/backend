package com.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
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
	private final SearchIndex<ProductDTO> productIndex; // Không cần tạo các index cho minPrice hay maxPrice

	@Autowired
	public AlgoliaProductService(IdApikeyAIgolia idApikeyAIgolia) {
		// Khởi tạo SearchClient với SearchConfig
		this.searchClient = DefaultSearchClient.create(idApikeyAIgolia.getApplicationId(),
				idApikeyAIgolia.getAdminApiKey());

		// Khởi tạo index "products"
		this.productIndex = searchClient.initIndex("products", ProductDTO.class);
	}

	public List<ProductDTO> searchProducts(Integer categoryID, List<Integer> attributeId, BigDecimal minPrice,
			BigDecimal maxPrice, String query, String sortMaxPrice, int page, int pageSize) {

		// Default query if no search query is provided
		String searchQuery = query != null ? query : "*";
		Query algoliaQuery = new Query(searchQuery);

		List<String> filters = new ArrayList<>();

		// Add category filter if provided
		if (categoryID != null && categoryID > 0) {
			filters.add("categoryID = " + categoryID);
		}

		// Add attribute filters if any
		if (attributeId != null && !attributeId.isEmpty()) {
			for (Integer att : attributeId) {
				filters.add("attributeId = " + att);
			}
		}

		// Add price range filters
		if (minPrice != null && maxPrice != null) {
			filters.add("minPrice <= " + maxPrice);
			filters.add("maxPrice >= " + minPrice);
		} else if (minPrice != null) {
			filters.add("maxPrice >= " + minPrice);
		} else if (maxPrice != null) {
			filters.add("minPrice <= " + maxPrice);
		}

		// Set the filters if any exist
		if (!filters.isEmpty()) {
			algoliaQuery.setFilters(String.join(" AND ", filters));
		}

		List<ProductDTO> allProducts = new ArrayList<>();
		try {
			// Perform the Algolia search query
			SearchResult<ProductDTO> result = productIndex.search(algoliaQuery);
			allProducts = result.getHits();
		} catch (Exception e) {
			// Specific error message for Algolia search failure
			throw new RuntimeException("Error searching products with Algolia: " + e.getMessage(), e);
		}

		// Sort products by price if sortMaxPrice is provided
		if ("DESC".equalsIgnoreCase(sortMaxPrice)) {
			allProducts.sort((p1, p2) -> p2.getMaxPrice().compareTo(p1.getMaxPrice()));
		} else {
			allProducts.sort((p1, p2) -> p1.getMaxPrice().compareTo(p2.getMaxPrice()));
		}

		// Handle pagination (ensure valid page and pageSize)
		int totalProducts = allProducts.size();
		int startIndex = Math.max(page * pageSize, 0); // Ensure non-negative startIndex
		int endIndex = Math.min(startIndex + pageSize, totalProducts);

		// Return the paginated list
		List<ProductDTO> paginatedProducts = allProducts.subList(startIndex, endIndex);

		return paginatedProducts;
	}

	public List<ProductDTO> getTopProducts() {
		List<String> attributesToRetrieve = Arrays.asList("id", "name", "imgName", "rating", "minPrice", "maxPrice",
				"reviewCount");

		Query query = new Query().setQuery("").setAttributesToRetrieve(attributesToRetrieve).setHitsPerPage(50);

		try {

			SearchResult<ProductDTO> searchResult = productIndex.search(query);
			List<ProductDTO> products = searchResult.getHits();

			products.sort((p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));

			return products.stream().limit(12).collect(Collectors.toList());
		} catch (Exception e) {
			logger.severe("Lỗi khi lấy sản phẩm từ Algolia: " + e.getMessage());
			return List.of();
		}
	}

	// Thêm sản phẩm vào Algolia sau khi xóa tất cả các sản phẩm cũ
	public void addProduct(ProductDTO product) {
		try {

			// Thêm sản phẩm mới vào Algolia
			productIndex.saveObject(product).waitTask();
			logger.info("Thêm sản phẩm thành công: " + product);
		} catch (Exception e) {
			logger.severe("Lỗi khi thêm sản phẩm vào Algolia: " + e.getMessage());
		}
	}

	public List<ProductDTO> searchProducts(Query query) {
		try {
			SearchResult<ProductDTO> searchResult = productIndex.search(query);
			return searchResult.getHits();
		} catch (Exception e) {
			logger.severe("Lỗi khi tìm kiếm sản phẩm từ Algolia: " + e.getMessage());
			return List.of(); // Trả về danh sách rỗng nếu có lỗi
		}
	}

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