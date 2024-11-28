package com.controllers;

import com.models.EmailRequestDTO;
import com.models.OtpRequest;
import com.repositories.UsersJPA;
import com.models.AuthDTO;
import com.entities.Advertisement;
import com.entities.User;
import com.errors.ApiResponse;
import com.services.AdvertisementService;
import com.services.AuthManagementService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private AuthManagementService usersManagementService;

    @Autowired
    AdvertisementService advertisementService;

    @Autowired
    private UsersJPA usersRepo;

    @GetMapping("/api/today")
    public ResponseEntity<List<Advertisement>> getAdvertisementsForToday() {
        List<Advertisement> advertisements = advertisementService.getAdvertisementsForToday();
        return ResponseEntity.ok(advertisements);
    }

    @PostMapping("/api/register")
    public ResponseEntity<AuthDTO> regeister(@RequestBody AuthDTO reg) {
        return ResponseEntity.ok(usersManagementService.register(reg));
    }

    @PostMapping("/api/login")
    public ResponseEntity<AuthDTO> login(@RequestBody AuthDTO req) {
        return usersManagementService.login(req);
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthDTO> refreshToken(@RequestBody AuthDTO req) {
        return ResponseEntity.ok(usersManagementService.refreshToken(req));
    }

    @GetMapping("/api/staff/get-all-users")
    public ResponseEntity<ApiResponse<PageImpl<AuthDTO>>> getAllUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword) {
        ApiResponse<PageImpl<AuthDTO>> response = usersManagementService.getAllUsers(request, page, size, keyword);
        return ResponseEntity.status(response.getErrorCode())
                .body(response);
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

    // @PostMapping("/api/send-reset-code")
    // public ResponseEntity<ApiResponse<User>> sendCodeEmail(@RequestBody
    // EmailRequestDTO emailRequest) {
    // return usersManagementService.sendResetCodeEmail(emailRequest);
    // }

    @PostMapping("/api/reset-password")
    public ResponseEntity<ApiResponse<User>> resetPassword(@RequestBody Map<String, String> payload) {
        return usersManagementService.resetPassword(payload);
    }

    @PostMapping("/api/adminuser/logout")
    public ResponseEntity<AuthDTO> logout(@RequestHeader("Authorization") String token) {
        return usersManagementService.logout(token);
    }

    @PostMapping("/api/verify-otp")
    public ResponseEntity<AuthDTO> verifyOtp(@RequestBody OtpRequest otpRequest) {
        AuthDTO response = new AuthDTO();
        try {
            String username = otpRequest.getUsername();
            String otp = otpRequest.getOtp();

            // Kiểm tra username và OTP
            User user = usersRepo.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!user.getResetCode().equals(otp)) {
                throw new IllegalArgumentException("Invalid OTP");
            }

            // Kiểm tra thời gian hết hạn OTP
            if (user.getResetCodeExpiration().before(new Date())) {
                throw new IllegalArgumentException("OTP has expired");
            }

            // Cập nhật trạng thái người dùng
            user.setStatus((byte) 1); // Cập nhật status = 1
            user.setResetCode(null); // Xóa OTP sau khi xác thực thành công
            user.setResetCodeExpiration(null);

            usersRepo.save(user);

            response.setMessage("OTP verified successfully");
            response.setStatusCode(200);
            response.setListData(user);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.setStatusCode(400);
            response.setError(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setError("An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
