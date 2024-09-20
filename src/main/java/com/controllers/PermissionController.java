package com.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.User;
import com.models.AuthDTO;
import com.services.AuthManagementService;
import com.services.PermissionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/api/admin/permissions/{userId}")
    public ResponseEntity<List<String>> getPermissions(@PathVariable int userId) {
        List<String> permissions = permissionService.getPermissionsByUserId(userId);

        System.out.println("Quyền của user với ID " + userId + ": " + permissions);

        if (permissions.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(permissions);
        }
    }

    @PostMapping("/api/admin/permissions/add")
    public ResponseEntity<AuthDTO> addPermissions(@RequestBody AuthDTO registrationRequest) {
        List<String> permissions = registrationRequest.getPermissions();

        AuthDTO response = permissionService.addPermissions(registrationRequest, permissions);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/api/admin/permissions/update/{userId}")
    public ResponseEntity<AuthDTO> updateUser(
            HttpServletRequest request,
            @PathVariable Integer userId,
            @RequestBody AuthDTO authDTO) {

        AuthDTO response = permissionService.updateUser(request, userId, authDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
