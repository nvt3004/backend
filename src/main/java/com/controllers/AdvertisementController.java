package com.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.entities.Advertisement;
import com.entities.Image;
import com.entities.ProductVersion;
import com.errors.ResponseAPI;
import com.repositories.AdversitementJPA;
import com.repositories.ImageJPA;
import com.responsedto.AdvertisementResponse;
import com.responsedto.ImageResponse;
import com.responsedto.ProductVersionResponse;
import com.utils.UploadService;

@RestController
@RequestMapping("/api/advertisement")
public class AdvertisementController {

    @Autowired
    private AdversitementJPA adversitementJPA;

    @Autowired
    UploadService uploadService;

    @Autowired
    ImageJPA imageJPA;

    @PostMapping("/add")
    public ResponseEntity<ResponseAPI<Boolean>> addAdvertisement(
            @RequestBody AdvertisementResponse advertisementModal) {
        System.out.println("Add adv");
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);

        Advertisement advertisement = new Advertisement();

        advertisement.setAdvDescription(advertisementModal.getDescription());
        advertisement.setAdvName(advertisementModal.getTitle());
        advertisement.setStartDate(advertisementModal.getStartDate());
        advertisement.setEndDate(advertisementModal.getEndDate());

        adversitementJPA.save(advertisement);

        // System.out.println("Nhan duoc anh: "+advertisementModal.getImages().get(0).getName());

        saveImageAdvertisement(advertisement, advertisementModal.getImages());

        response.setCode(200);
        response.setData(true);
        response.setMessage("Success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseAPI<Boolean>> updateAdvertisement(
            @RequestBody AdvertisementResponse advertisementModal) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);

        Advertisement advertisement = adversitementJPA.findById(advertisementModal.getId()).orElse(null);

        advertisement.setAdvDescription(advertisementModal.getDescription());
        advertisement.setAdvName(advertisementModal.getTitle());
        advertisement.setStartDate(advertisementModal.getStartDate());
        advertisement.setEndDate(advertisementModal.getEndDate());

        adversitementJPA.save(advertisement);

        String imageName = changeNewImage(advertisementModal, advertisement);
        if (imageName != null) {
            Image imageEntity = new Image();
            imageEntity.setImageUrl(imageName);
            imageEntity.setAdvertisement(advertisement);
            imageJPA.save(imageEntity);
        }

        response.setCode(200);
        response.setData(true);
        response.setMessage("Success");
        return ResponseEntity.ok(response);
    }

    private void saveImageAdvertisement(Advertisement advertisement, List<ImageResponse> images) {
        for (ImageResponse img : images) {
            System.out.println("Nhan duoc anh: "+img.getName());
            String fileName = uploadService.save(img.getName(), "images");
            Image imageEntity = new Image();
            imageEntity.setImageUrl(fileName);
            imageEntity.setAdvertisement(advertisement);
            imageJPA.save(imageEntity);
        }
    }

    private String changeNewImage(AdvertisementResponse advertisementModal, Advertisement advertisement) {

        if (advertisementModal.getImages() != null && !advertisementModal.getImages().isEmpty()) {

            if (advertisement.getImages() != null && !advertisement.getImages().isEmpty()) {
                for (Image oldImage : advertisement.getImages()) {
                    String oldImageUrl = oldImage.getImageUrl();
                    uploadService.delete(oldImageUrl, "images");
                    imageJPA.delete(oldImage);
                }
            }

            String imageName = advertisementModal.getImages().get(0).getName();
            if (imageName != null && !imageName.isBlank()) {
                String fileName = uploadService.save(imageName, "images");

                Image newImage = new Image();
                newImage.setImageUrl(fileName);
                newImage.setAdvertisement(advertisement);

                imageJPA.save(newImage);
                return fileName;
            }
        }

        return null;
    }

}
