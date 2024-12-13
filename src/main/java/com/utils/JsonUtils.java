package com.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

public class JsonUtils {

	// Phương thức để trích xuất chuỗi vector từ JSON
	public static String extractVector(String jsonResponse) throws Exception {
		// Khởi tạo ObjectMapper để parse JSON
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);

		// Kiểm tra và trích xuất giá trị từ khóa "vector"
		if (rootNode.has("vector") && rootNode.get("vector").isArray()) {
			JsonNode vectorNode = rootNode.get("vector");
			StringBuilder vectorString = new StringBuilder();

			// Duyệt qua các phần tử trong mảng vector
			for (int i = 0; i < vectorNode.size(); i++) {
				// Chuyển đổi số từ E-notation sang chuỗi thập phân
				BigDecimal number = new BigDecimal(vectorNode.get(i).asText());
				vectorString.append(number.toPlainString()); // Sử dụng toPlainString để loại bỏ E-notation
				if (i < vectorNode.size() - 1) {
					vectorString.append(", "); // Thêm dấu phẩy ngăn cách
				}
			}
			return vectorString.toString();
		} else {
			throw new IllegalArgumentException("Invalid JSON: 'vector' key not found or is not an array.");
		}
	}
}
