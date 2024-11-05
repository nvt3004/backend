package com.controllers;

import com.models.EmailRequestDTO;
import com.models.AuthDTO;
import com.entities.User;
import com.errors.ApiResponse;
import com.services.AuthManagementService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private AuthManagementService usersManagementService;

    @PostMapping("/api/register")
    public ResponseEntity<AuthDTO> regeister(@RequestBody AuthDTO reg) {
        System.out.println("Vô resgfdgfdgdfsgdfgfdsgdffffffffffffffffffffff");
        return ResponseEntity.ok(usersManagementService.register(reg));
    }

    @PostMapping("/api/te")
    public ResponseEntity<AuthDTO> regeisterh(@RequestBody AuthDTO reg) {
        System.out.println("Vô resgfdgfdgdfsgdfgfdsgdffffffffffffffffffffff");
        return ResponseEntity.ok(usersManagementService.register(reg));
    }

    @PostMapping("/api/login")
    public ResponseEntity<AuthDTO> login(@RequestBody AuthDTO req) {
        // Trả thẳng về bên kia luôn để cho bên controller ngắn lại dễ nhìn
        return usersManagementService.login(req);
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthDTO> refreshToken(@RequestBody AuthDTO req) {
        return ResponseEntity.ok(usersManagementService.refreshToken(req));
    }

    @GetMapping("/api/admin/get-all-users")
    public ResponseEntity<AuthDTO> getAllUsers(HttpServletRequest request) {
        AuthDTO response = usersManagementService.getAllUsers(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/api/admin/get-users/{userId}")
    public ResponseEntity<AuthDTO> getUserById(@PathVariable Integer userId, HttpServletRequest request) {
        AuthDTO response = usersManagementService.getUsersById(request, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/api/adminuser/update/{userId}")
    public ResponseEntity<AuthDTO> updateUser(@PathVariable Integer userId, @RequestBody User updatedUser,
            HttpServletRequest request) {
        AuthDTO response = usersManagementService.updateUser(request, userId, updatedUser);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/api/login-social")
    public ResponseEntity<AuthDTO> createUser(@RequestBody User newUser) {
        return ResponseEntity.ok(usersManagementService.loginSocial(newUser));
    }

    @GetMapping("/api/adminuser/get-profile")
    public ResponseEntity<AuthDTO> getMyProfile(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        AuthDTO response = usersManagementService.getMyInfo(email);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/api/admin/delete/{userId}")
    public ResponseEntity<AuthDTO> deleteUser(@PathVariable Integer userId, HttpServletRequest request) {
        AuthDTO response = usersManagementService.deleteUser(request, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/api/send")
    public ResponseEntity<ApiResponse<User>> sendResetPasswordEmail(@RequestBody EmailRequestDTO emailRequest) {
        return usersManagementService.sendResetPasswordEmail(emailRequest);
    }

    @PostMapping("/api/reset-password")
    public ResponseEntity<ApiResponse<User>> resetPassword(@RequestBody Map<String, String> payload) {
        return usersManagementService.resetPassword(payload);
    }

    @PostMapping("/api/adminuser/logout")
    public ResponseEntity<AuthDTO> logout(@RequestHeader("Authorization") String token){
        return usersManagementService.logout(token);
    }
}
