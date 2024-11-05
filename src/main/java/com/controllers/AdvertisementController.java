package com.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adversetiment")
public class AdvertisementController {

    @PostMapping("/add")
    @PreAuthorize("hasPermission(#userId, 'Add ADV')")
    public ResponseEntity<String> addAdvertisement() {
        // Logic thêm quảng cáo
        return ResponseEntity.ok("Advertisement added successfully!");
    }

    // @PostMapping("/delete")
    // // @PreAuthorize("hasAuthority('Delete ADV')") 
    // public ResponseEntity<String> deleteAdvertisement() {
    //     // Logic thêm quảng cáo
    //     return ResponseEntity.ok("Advertisement deleted successfully!");
    // }
}
