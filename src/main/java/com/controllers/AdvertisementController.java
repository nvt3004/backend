package com.controllers;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dto.advertisement.request.AdvertisementDeleteRequest;
import com.dto.response.ApiResponse;
import com.entities.Advertisement;
import com.entities.Image;
import com.errors.ResponseAPI;
import com.repositories.AdversitementJPA;
import com.repositories.ImageJPA;
import com.responsedto.AdvertisementResponse;
import com.services.AdvertisementService;
import com.utils.UploadService;

import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/staff/advertisement")
public class AdvertisementController {

    @Autowired
    AdversitementJPA adversitementJPA;

    @Autowired
    UploadService uploadService;

    @Autowired
    ImageJPA imageJPA;

    @Autowired
    AdvertisementService advertisementService;

    @PostMapping("/add")
    @PreAuthorize("hasPermission(#userId, 'Add Advertisement')")
    public ResponseEntity<ResponseAPI<Boolean>> addAdvertisement(
            @RequestParam("advName") String advName,
            @RequestParam("advDescription") String advDescription,
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate,
            @RequestParam("status") byte status,
            @RequestPart("images") List<MultipartFile> images) {

        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);

        LocalDateTime now = LocalDateTime.now();

        if (endDate.isBefore(startDate)) {
            response.setCode(445);
            response.setMessage("End date must be greater than start date.");
            return ResponseEntity.ok(response);
        }

        // Kiểm tra ngày bắt đầu của quảng cáo không được nằm trong khoảng ngày của
        // quảng cáo khác
        List<Advertisement> existingAdvertisements = adversitementJPA.findAllAdvertisementsWithStatus1();
        for (Advertisement existingAdvertisement : existingAdvertisements) {
            if ((startDate.isEqual(existingAdvertisement.getStartDate())
                    || startDate.isAfter(existingAdvertisement.getStartDate()))
                    && startDate.isBefore(existingAdvertisement.getEndDate())) {
                response.setCode(446);
                response.setMessage("Start date overlaps with another advertisement.");
                return ResponseEntity.ok(response);
            }
        }

        // Create a new Advertisement object
        Advertisement advertisement = new Advertisement();
        advertisement.setAdvDescription(advDescription);
        advertisement.setAdvName(advName);
        advertisement.setStartDate(startDate);
        advertisement.setEndDate(endDate);
        advertisement.setStatus(status);

        // Save the advertisement
        adversitementJPA.save(advertisement);

        // Save the images associated with the advertisement
        saveImageAdvertisement(advertisement, images);

        response.setCode(200);
        response.setData(true);
        response.setMessage("Advertisement added successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasPermission(#userId, 'Update Advertisement')")
    public ResponseEntity<ResponseAPI<Boolean>> updateAdvertisement(
            @PathVariable("id") Integer id,
            @RequestParam("advName") String advName,
            @RequestParam("advDescription") String advDescription,
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate,
            @RequestParam("status") int status,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "existingImages", required = false) List<String> existingImages,
            @RequestParam(value = "removedImages", required = false) List<String> removedImages) {

        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);

        // Tìm kiếm quảng cáo
        Advertisement advertisement = adversitementJPA.findById(id).orElse(null);

        if (advertisement == null) {
            response.setCode(404);
            response.setMessage("Advertisement not found");
            return ResponseEntity.status(404).body(response);
        }

        List<Advertisement> existingAdvertisements = adversitementJPA.findAllAdvertisementsWithStatus1();
        for (Advertisement existingAdvertisement : existingAdvertisements) {
            if ((startDate.isEqual(existingAdvertisement.getStartDate())
                    || startDate.isAfter(existingAdvertisement.getStartDate()))
                    && startDate.isBefore(existingAdvertisement.getEndDate())) {
                response.setCode(446);
                response.setMessage("Start date overlaps with another advertisement.");
                return ResponseEntity.ok(response);
            }
        }

        // Cập nhật thông tin quảng cáo
        advertisement.setAdvDescription(advDescription);
        advertisement.setAdvName(advName);

        advertisement.setStatus((byte) status);

        // Lưu cập nhật quảng cáo
        adversitementJPA.save(advertisement);

        // Cập nhật ảnh nếu có
        List<String> updatedImages = updateImages(advertisement, images, removedImages); // Gọi phương thức updateImages

        // Trả về danh sách ảnh đã được cập nhật
        response.setCode(200);
        response.setData(true);
        response.setMessage("Advertisement updated successfully");
        // response.setData(updatedImages); // Trả về danh sách ảnh đã được cập nhật
        return ResponseEntity.ok(response);
    }

    private void saveImageAdvertisement(Advertisement advertisement, List<MultipartFile> images) {
        for (MultipartFile image : images) {
            try {
                // Save the image file
                String fileName = uploadService.save(image, "images"); // Save method adapted to handle MultipartFile
                Image imageEntity = new Image();
                imageEntity.setImageUrl(fileName);
                imageEntity.setAdvertisement(advertisement);
                imageJPA.save(imageEntity);
            } catch (Exception e) {
                e.printStackTrace(); // Handle error appropriately
            }
        }
    }

    private List<String> updateImages(Advertisement advertisement, List<MultipartFile> images,
            List<String> imagesToDelete) {
        List<String> uploadedFileNames = new ArrayList<>();

        try {
            // Xử lý xóa ảnh
            if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
                for (String imageUrl : imagesToDelete) {
                    // Tìm ảnh cần xóa trong cơ sở dữ liệu bằng URL
                    Image imageToDelete = imageJPA.findByImageUrl(imageUrl);
                    if (imageToDelete != null) {
                        // Xóa ảnh khỏi hệ thống (ví dụ: xóa file trên server)
                        uploadService.delete(imageToDelete.getImageUrl(), "images");
                        // Xóa ảnh khỏi cơ sở dữ liệu
                        imageJPA.delete(imageToDelete);
                    }
                }
            }

            // Kiểm tra và lưu ảnh mới nếu có
            if (images != null && !images.isEmpty()) {
                for (MultipartFile imageFile : images) {
                    String fileName = uploadService.save(imageFile, "images"); // Lưu ảnh mới
                    Image newImage = new Image();
                    newImage.setImageUrl(fileName);
                    newImage.setAdvertisement(advertisement);
                    imageJPA.save(newImage); // Lưu ảnh vào DB
                    uploadedFileNames.add(fileName);
                }
            }

            // Giữ lại ảnh cũ của quảng cáo (nếu có)
            if (advertisement.getImages() != null) {
                for (Image oldImage : advertisement.getImages()) {
                    uploadedFileNames.add(oldImage.getImageUrl()); // Thêm ảnh cũ vào danh sách trả về
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to update images for advertisement", e);
        }

        return uploadedFileNames;
    }

    @GetMapping
    @PreAuthorize("hasPermission(#userId, 'View Advertisement')")
    public ResponseEntity<ApiResponse<PageImpl<com.dto.advertisement.response.AdvertisementResponse>>> getAllAdvertisement(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        PageImpl<com.dto.advertisement.response.AdvertisementResponse> response = advertisementService
                .getAllAdvertisement(page, size);
        return ResponseEntity.ok(ApiResponse.<PageImpl<com.dto.advertisement.response.AdvertisementResponse>>builder()
                .message("Lấy toàn bộ Advertisement thành công")
                .result(response)
                .build());
    }

    @GetMapping("/id")
    public ResponseEntity<ApiResponse<com.dto.advertisement.response.AdvertisementResponse>> getAdvertisementById(
            @RequestParam int id) {
        com.dto.advertisement.response.AdvertisementResponse response = advertisementService.getAdvertisementById(id);
        return ResponseEntity.ok(ApiResponse.<com.dto.advertisement.response.AdvertisementResponse>builder()
                .message("Lấy Advertisement theo id thành công")
                .result(response).build());
    }

    @DeleteMapping
    @PreAuthorize("hasPermission(#userId, 'Delete Advertisement')")
    public ResponseEntity<ApiResponse<Void>> deleteAdvertisementById(@Valid @RequestParam int id) {
        advertisementService.deleteAdvertisementById(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Xóa thành công Advertisement").build());
    }

    @DeleteMapping("/ids")
    public ResponseEntity<ApiResponse<Void>> deleteAdvertisementByList(
            @Valid @RequestBody AdvertisementDeleteRequest request) {
        advertisementService.deleteAdvertisementByList(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Xóa thành công các Advertisement").build());
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        Path imagePath = Paths.get("/static/images/" + imageName);
        Resource resource = new FileSystemResource(imagePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Hoặc có thể điều chỉnh tùy vào định dạng ảnh
                .body(resource);
    }
}