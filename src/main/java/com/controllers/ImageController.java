package com.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.services.ImageFeatureService;
import com.services.ImageSearchService;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageFeatureService imageFeatureService;

    @Autowired
    private ImageSearchService imageSearchService;

    // API để load ảnh và lưu trữ đặc trưng
    @PostMapping("/load")
    public String loadImages(@RequestBody List<String> imagePaths) {
        return imageFeatureService.loadImages(imagePaths);
    }

    // API để tìm kiếm ảnh
    @PostMapping("/search")
    public String searchImage(@RequestParam("image") MultipartFile image) throws Exception {
        return imageSearchService.searchImage(image);
    }
}
