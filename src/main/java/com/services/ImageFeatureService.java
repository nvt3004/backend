package com.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.HashMap;

@Service
public class ImageFeatureService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String PYTHON_API_URL = "http://localhost:5000/api/load_images";

    public String loadImages(List<String> imagePaths) {
        HashMap<String, Object> request = new HashMap<>();
        request.put("image_paths", imagePaths);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<HashMap<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                PYTHON_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }
}
