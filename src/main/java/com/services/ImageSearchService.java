package com.services;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageSearchService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String PYTHON_API_URL = "http://localhost:5000/api/search_image";

    public String searchImage(MultipartFile imageFile) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Convert MultipartFile to ByteArrayResource
        Resource resource = new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        };

        HttpEntity<Resource> entity = new HttpEntity<>(resource, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                PYTHON_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }
}
