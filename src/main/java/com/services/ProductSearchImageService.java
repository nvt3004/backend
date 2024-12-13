package com.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.ProductVector;
import com.repositories.ProductVectorJPA;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductSearchImageService {

    @Autowired
    private ConvertImageToVectorService convertImageToVectorService; // Service convert image to vector

    @Autowired
    private ProductVectorJPA productVectorJPA; // JPA repository to get vectors from DB

    // Phương thức tìm kiếm sản phẩm dựa trên hình ảnh
    public String searchProductByImage(InputStream imageStream) throws Exception {
        // 1. Lấy vector từ hình ảnh gửi đến Flask API
        String inputVector = convertImageToVectorService.sendImageToFlask(imageStream);

        // 2. Lấy danh sách các vector sản phẩm từ cơ sở dữ liệu (ProductVectorJPA)
        List<String> vectorList = getProductVectorsFromDB();

        // 3. Tính toán độ tương tự giữa vector từ ảnh và các vector trong cơ sở dữ liệu
        String result = calculateSimilarity(inputVector, vectorList, "cosine"); // Sử dụng cosine làm mặc định

        return result;
    }

    // Phương thức lấy các vector từ cơ sở dữ liệu
    private List<String> getProductVectorsFromDB() {
        List<String> vectorList = new ArrayList<>();
        
        // Lấy tất cả các vector sản phẩm từ cơ sở dữ liệu
        List<ProductVector> productVectors = productVectorJPA.findAll();

        // Chuyển đổi từng đối tượng ProductVector thành chuỗi vector
        for (ProductVector productVector : productVectors) {
            vectorList.add(productVector.getImageVector()); // Giả sử ProductVector có thuộc tính vector dạng String
        }

        return vectorList;
    }

    // Phương thức gọi Flask API để tính độ tương tự
    public String calculateSimilarity(String inputVector, List<String> vectorList, String distanceMetric) throws Exception {
        return convertImageToVectorService.calculateSimilarity(inputVector, vectorList, distanceMetric);
    }
}
