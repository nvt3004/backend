package com.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class UploadServiceCloud {

    private final Cloudinary cloudinary;

    public UploadServiceCloud() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "your-cloud-name",
                "api_key", "your-api-key",
                "api_secret", "your-api-secret"
        ));
    }

    public String save(MultipartFile file, String folder) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", folder));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to Cloudinary", e);
        }
    }
}
