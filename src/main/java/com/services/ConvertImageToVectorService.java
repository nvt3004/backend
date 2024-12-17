package com.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.stereotype.Service;

import com.utils.JsonUtils;


@Service
public class ConvertImageToVectorService {

	private static final String FLASK_API_URL = "http://127.0.0.1:5000/extract-vector"; // URL Flask API
	private static final String FLASK_SIMILARITY_API_URL = "http://127.0.0.1:5000/calculate-similarity"; // URL Flask
																											// API cho
																											// tính toán
																											// tương tự

	// Phương thức nhận URL ảnh và chuyển ảnh thành vector
	public String getImageVector(String imageUrl) throws Exception {
		// Tạo HttpClient để gửi yêu cầu HTTP
		HttpClient client = HttpClient.newHttpClient();

		// Tạo HTTP GET request để tải ảnh từ URL
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(imageUrl)).build();

		try {
			// Gửi yêu cầu và nhận phản hồi
			HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

			if (response.statusCode() == 200) {
				// Đọc ảnh từ response
				InputStream imageStream = response.body();

				// Gửi ảnh tới Flask API để phân tích và nhận chuỗi vector
				String vector = sendImageToFlask(imageStream);
				System.out.println("vector"+vector);
				return vector;
			} else {
				throw new IOException("Failed to retrieve image, status code: " + response.statusCode());
			}
		} catch (InterruptedException | IOException e) {
			throw new IOException("Error while sending request to retrieve image: " + e.getMessage(), e);
		}
	}

	// Phương thức gửi ảnh tới Flask API và nhận vector
	public String sendImageToFlask(InputStream imageStream) throws Exception {
		try {
			// Tạo yêu cầu HTTP POST để gửi ảnh tới Flask API
			HttpClient client = HttpClient.newHttpClient();

			// Chuyển đổi InputStream thành Base64 String để gửi
			String encodedImage = encodeImageToBase64(imageStream);

			// Tạo body chứa chuỗi ảnh Base64
			String jsonRequestBody = "{\"image\": \"" + encodedImage + "\"}";

			// Tạo yêu cầu POST
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(FLASK_API_URL))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody)).build();

			// Gửi yêu cầu và nhận phản hồi
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			// Kiểm tra phản hồi từ Flask API
			if (response.statusCode() == 200) {
				// Trả về chuỗi vector từ Flask API
				return JsonUtils.extractVector(response.body());
			} else {
				throw new IOException("Failed to get response from Flask API, status code: " + response.statusCode());
			}

		} catch (InterruptedException | IOException e) {
			throw new IOException("Error while sending image to Flask API", e);
		}
	}

	// Phương thức chuyển đổi ảnh InputStream thành Base64 String
	private String encodeImageToBase64(InputStream imageStream) throws IOException {
		// Đọc InputStream vào byte array
		byte[] bytes = imageStream.readAllBytes();

		// Chuyển đổi byte array thành Base64 string
		return Base64.getEncoder().encodeToString(bytes);
	}

	public String calculateSimilarity(String inputVector, List<String> vectorList, String distanceMetric)
			throws Exception {
		try {
			// Tạo JSON body request
			JSONObject requestBody = new JSONObject();
			requestBody.put("input_vector", inputVector);
			requestBody.put("vector_list", new JSONArray(vectorList)); // Chuyển List<String> thành JSONArray
			requestBody.put("distance_metric", distanceMetric); // Mặc định: cosine

			// Tạo HttpClient
			HttpClient client = HttpClient.newHttpClient();

			// Tạo HTTP POST request
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(FLASK_SIMILARITY_API_URL))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(requestBody.toString())).build();

			// Gửi yêu cầu và nhận phản hồi
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			// Kiểm tra trạng thái phản hồi
			if (response.statusCode() == 200) {
				// Trả về kết quả JSON từ Flask API
				return response.body();
			} else {
				throw new IOException("Failed to get response from Flask API, status code: " + response.statusCode());
			}
		} catch (InterruptedException | IOException e) {
			throw new IOException("Error while calling Flask API for similarity calculation", e);
		}
	}
}
