package com.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.entities.Cart;
import com.entities.Role;
import com.entities.User;
import com.entities.UserRole;
import com.errors.ApiResponse;
import com.models.AuthDTO;
import com.models.EmailRequestDTO;
import com.repositories.RoleJPA;
import com.repositories.UserRoleJPA;
import com.repositories.UsersJPA;
import com.utils.DateTimeUtil;
import com.utils.JWTUtils;
import com.utils.TokenBlacklist;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthManagementService {

    @Autowired
    private UsersJPA usersRepo;
    @Autowired
    private UserRoleJPA userRoleRepo;
    @Autowired
    private RoleJPA roleRepo;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MailService mailService;
    Date datecurrent = DateTimeUtil.getCurrentDateInVietnam();

    @Autowired
    private CartService cartRepo;

    public AuthDTO register(AuthDTO registrationRequest) {
        AuthDTO resp = new AuthDTO();

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        String phoneRegex = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        String nameRegex = "^([A-ZÀ-Ỹ][a-zà-ỹ]*(\\s[A-ZÀ-Ỹ][a-zà-ỹ]*)*)$";

        try {
            String username = registrationRequest.getUsername();

            if (registrationRequest.getFullName() == null || registrationRequest.getFullName().isEmpty()) {
                throw new IllegalArgumentException("Invalid full name is empty");
            }
            if (!registrationRequest.getFullName().matches(nameRegex)) {
                throw new IllegalArgumentException("Full name cannot contain special characters or numbers");
            }

            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty");
            }

            if (username.matches(emailRegex)) {
                Optional<User> existingUser = usersRepo.findByEmailAndProvider(username, "Guest");
                if (existingUser.isPresent()) {
                    throw new IllegalArgumentException("Email already exists with provider Guest");
                }
            } else if (username.matches(phoneRegex)) {
                Optional<User> existingUser = usersRepo.findByPhoneAndProvider(username, "Guest");
                if (existingUser.isPresent()) {
                    throw new IllegalArgumentException("Phone number already exists with provider Guest");
                }
            } else {
                throw new IllegalArgumentException("Invalid username format. Must be a valid email or phone number");
            }

            if (registrationRequest.getPassword() == null || registrationRequest.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Invalid password is empty");
            }
            if (!registrationRequest.getPassword().matches(passwordRegex)) {
                throw new IllegalArgumentException(
                        "Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, one number, and one special character");
            }

            User ourUser = new User();
            ourUser.setEmail(username.matches(emailRegex) ? username : null);
            ourUser.setPhone(username.matches(phoneRegex) ? username : null);
            ourUser.setFullName(registrationRequest.getFullName());
            ourUser.setUsername(username);
            ourUser.setCreateDate(datecurrent);
            ourUser.setProvider("Guest");
            ourUser.setStatus((byte) 0);
            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            String resetCode = generateRandomCode();
            ourUser.setResetCode(resetCode);
            ourUser.setResetCodeExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // Hết hạn sau 15 phút
            usersRepo.save(ourUser);
    
            // Send reset code email
            mailService.sendEmail(username, "Reset Code", "Your reset code is: " + resetCode);
            User ourUsersResult = usersRepo.save(ourUser);

            List<String> roles = Collections.singletonList("User");
            List<UserRole> userRoles = roles.stream().map(roleName -> {
                Role role = roleRepo.findByRoleName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                UserRole userRole = new UserRole();
                userRole.setUser(ourUsersResult);
                userRole.setRole(role);
                return userRole;
            }).collect(Collectors.toList());

            userRoleRepo.saveAll(userRoles);

            Cart newCart = new Cart();
            newCart.setUser(ourUsersResult);
            cartRepo.addCart(newCart);

            if (ourUsersResult.getUserId() > 0) {
                resp.setListData(ourUsersResult);

                resp.setMessage("User Saved Successfully");
                resp.setStatusCode(200);
            }

        } catch (IllegalArgumentException e) {
            resp.setStatusCode(400);
            resp.setError(e.getMessage());
        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setError("Error occurred while registering user: " + e.getMessage());
        }
        return resp;
    }

    public ResponseEntity<AuthDTO> login(AuthDTO loginRequest) {
        AuthDTO response = new AuthDTO();
        try {
            System.out.println("Login attempt for username: " + loginRequest.getUsername());

            // Kiểm tra người dùng
            Optional<User> optionalUser = usersRepo.findByUsernameAndProvider(loginRequest.getUsername(), "Guest");
            if (optionalUser.isEmpty()) {
                response.setStatusCode(401);
                response.setError("Login fail: username or password incorrect");
                System.out.println("User not found: " + loginRequest.getUsername());
                return ResponseEntity.status(200).body(response);
            }

            User user = optionalUser.get();

            // Xác thực
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                                loginRequest.getPassword()));
                System.out.println("Authentication successful for user: " + user.getEmail());
            } catch (BadCredentialsException e) {
                response.setStatusCode(401);
                response.setError("Login fail: Invalid credentials");
                System.out.println("Authentication failed: " + e.getMessage());
                return ResponseEntity.status(2000).body(response);
            }

            // Tạo JWT token
            long expirationTime = (5 * 60 * 60) * 60 * 1000; // 24 giờ (86,400,000 ms)
            String jwt = jwtUtils.generateToken(user, "login", expirationTime);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            String tokenPurpose = jwtUtils.extractPurpose(jwt);

            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRefreshToken(refreshToken);
            response.setFullName(user.getFullName());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            response.setRoles(user.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .map(Role::getRoleName)
                    .collect(Collectors.toList()));
            response.setTokenType(tokenPurpose);

            System.out.println("Login successful for user: " + user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Username: " + loginRequest.getUsername());
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
            response.setStatusCode(500);
            response.setError("An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public AuthDTO refreshToken(AuthDTO refreshTokenReqiest) {
        AuthDTO response = new AuthDTO();
        try {
            String ourEmail = jwtUtils.extractUsername(refreshTokenReqiest.getToken());
            System.out.println("Token la: " + refreshTokenReqiest.getToken());
            User users = usersRepo.findByEmail(ourEmail).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenReqiest.getToken(), users)) {
                long expirationTime = 600 * 1000;
                var jwt = jwtUtils.generateToken(users, "login", expirationTime);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenReqiest.getToken());
                response.setMessage("Successfully Refreshed Token");
            }
            response.setStatusCode(200);
            return response;

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public ApiResponse<PageImpl<AuthDTO>> getAllUsers(HttpServletRequest request, int page, int size, String keyword) {
        ApiResponse<PageImpl<AuthDTO>> response = new ApiResponse<>();
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String tokenPurpose = jwtUtils.extractClaim(token, claims -> claims.get("purpose", String.class));

            if (!"login".equals(tokenPurpose)) {
                response.setErrorCode(403);
                response.setMessage("Invalid token purpose");
                return response;
            }

            try {
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "userId"));
                Page<User> usersPage;

                if (keyword == null || keyword.trim().isEmpty()) {
                    usersPage = usersRepo.findAll(pageable);
                } else {
                    usersPage = usersRepo.findAllByKeyword(keyword, pageable);
                }

                if (!usersPage.isEmpty()) {
                    List<AuthDTO> userDTOs = usersPage.stream().map(this::convertToAuthDTO)
                            .collect(Collectors.toList());
                    PageImpl<AuthDTO> resultPage = new PageImpl<>(userDTOs, pageable, usersPage.getTotalElements());

                    response.setData(resultPage);
                    response.setErrorCode(200);
                    response.setMessage("Successful");
                } else {
                    response.setErrorCode(404);
                    response.setMessage("No users found");
                }

            } catch (Exception e) {
                response.setErrorCode(500);
                response.setMessage("Error occurred: " + e.getMessage());
            }
        } else {
            response.setErrorCode(401);
            response.setMessage("Authorization header is missing or token is invalid");
        }

        return response;
    }

    public AuthDTO convertToAuthDTO(User user) {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setFullName(user.getFullName());
        authDTO.setEmail(user.getEmail());
        authDTO.setPhone(user.getPhone());
        authDTO.setGender(user.getGender());
        authDTO.setImage(user.getImage());
        authDTO.setProvider(user.getProvider());
        authDTO.setCreatDate(user.getCreateDate());
        authDTO.setBirthDate(user.getBirthday());
        return authDTO;
    }

    public AuthDTO getUsersById(HttpServletRequest request, Integer id) {
        AuthDTO reqRes = new AuthDTO();
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                String tokenPurpose = jwtUtils.extractClaim(token, claims -> claims.get("purpose", String.class));
                if (!"login".equals(tokenPurpose)) {
                    reqRes.setStatusCode(403);
                    reqRes.setMessage("Invalid token purpose");
                    return reqRes;
                }

                User usersById = usersRepo.findById(id).orElseThrow(() -> new RuntimeException("User Not found"));
                reqRes.setListData(usersById);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Users with id '" + id + "' found successfully");
            } else {
                reqRes.setStatusCode(401);
                reqRes.setMessage("Authorization header is missing or token is invalid");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }
        return reqRes;
    }

    public AuthDTO deleteUser(HttpServletRequest request, Integer userId) {
        AuthDTO reqRes = new AuthDTO();
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                String tokenPurpose = jwtUtils.extractClaim(token, claims -> claims.get("purpose", String.class));
                if (!"login".equals(tokenPurpose)) {
                    reqRes.setStatusCode(403);
                    reqRes.setMessage("Invalid token purpose");
                    return reqRes;
                }

                Optional<User> userOptional = usersRepo.findById(userId);
                if (userOptional.isPresent()) {
                    userOptional.get().setStatus((byte) 0);
                    reqRes.setStatusCode(200);
                    reqRes.setMessage("User deleted successfully");
                } else {
                    reqRes.setStatusCode(404);
                    reqRes.setMessage("User not found for deletion");
                }
            } else {
                reqRes.setStatusCode(401);
                reqRes.setMessage("Authorization header is missing or token is invalid");

            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while deleting user: " + e.getMessage());
        }
        return reqRes;
    }

    public AuthDTO updateUser(HttpServletRequest request, Integer userId, User updatedUser) {
        AuthDTO reqRes = new AuthDTO();
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                String tokenPurpose = jwtUtils.extractClaim(token, claims -> claims.get("purpose", String.class));
                if (!"login".equals(tokenPurpose)) {
                    reqRes.setStatusCode(403);
                    reqRes.setMessage("Invalid token purpose");
                    return reqRes;
                }

                Optional<User> userOptional = usersRepo.findById(userId);
                if (userOptional.isPresent()) {
                    User existingUser = userOptional.get();

                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                    }

                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setFullName(updatedUser.getFullName());
                    existingUser.setPhone(updatedUser.getPhone());
                    existingUser.setImage(updatedUser.getImage());
                    existingUser.setGender(updatedUser.getGender());
                    existingUser.setBirthday(updatedUser.getBirthday());
                    existingUser.setStatus((byte) 1);

                    User savedUser = usersRepo.save(existingUser);
                    reqRes.setListData(savedUser);
                    reqRes.setStatusCode(200);
                    reqRes.setMessage("User updated successfully");
                } else {
                    reqRes.setStatusCode(404);
                    reqRes.setMessage("User not found for update");
                }
            } else {
                reqRes.setStatusCode(401);
                reqRes.setMessage("Authorization header is missing or token is invalid");

            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while updating user: " + e.getMessage());
        }
        return reqRes;
    }

    public AuthDTO loginSocial(User newUser) {
        AuthDTO reqRes = new AuthDTO();
        try {
            Optional<User> optionalUser = usersRepo.findByUsername(newUser.getUsername());

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if (newUser.getProvider().equalsIgnoreCase("facebook")
                        || newUser.getProvider().equalsIgnoreCase("google")) {
                    reqRes.setStatusCode(200);
                    reqRes.setMessage("Login " + newUser.getProvider() + " successfully");

                    // Tạo JWT token
                    long expirationTime = 30 * 60 * 1000;
                    String jwt = jwtUtils.generateToken(user, "login", expirationTime);
                    String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
                    String tokenPurpose = jwtUtils.extractPurpose(jwt);

                    reqRes.setToken(jwt);
                    reqRes.setRefreshToken(refreshToken);
                    reqRes.setFullName(user.getFullName());
                    reqRes.setEmail(user.getEmail());
                    reqRes.setPhone(user.getPhone());
                    reqRes.setUsername(user.getUsername());
                    reqRes.setRoles(user.getUserRoles().stream()
                            .map(UserRole::getRole)
                            .map(Role::getRoleName)
                            .collect(Collectors.toList()));
                    reqRes.setTokenType(tokenPurpose);

                    return reqRes;
                }
            }

            // Nếu người dùng chưa tồn tại, tạo mới
            newUser.setStatus((byte) 1);
            newUser.setCreateDate(datecurrent);
            User savedUser = usersRepo.save(newUser);

            // Gán vai trò mặc định cho người dùng mới
            List<String> roles = Collections.singletonList("User");
            List<UserRole> userRoles = roles.stream().map(roleName -> {
                Role role = roleRepo.findByRoleName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                UserRole userRole = new UserRole();
                userRole.setUser(savedUser);
                userRole.setRole(role);
                return userRole;
            }).collect(Collectors.toList());

            userRoleRepo.saveAll(userRoles);

            // Tạo JWT token cho người dùng mới
            long expirationTime = 30 * 60 * 1000;
            String jwt = jwtUtils.generateToken(savedUser, "login", expirationTime);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), savedUser);
            String tokenPurpose = jwtUtils.extractPurpose(jwt);

            reqRes.setListData(savedUser);
            reqRes.setStatusCode(201);
            reqRes.setMessage("User created successfully");
            reqRes.setToken(jwt);
            reqRes.setRefreshToken(refreshToken);
            reqRes.setFullName(savedUser.getFullName());
            reqRes.setEmail(savedUser.getEmail());
            reqRes.setPhone(savedUser.getPhone());
            reqRes.setRoles(roles);
            reqRes.setTokenType(tokenPurpose);

        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while creating user: " + e.getMessage());
        }
        return reqRes;
    }

    public AuthDTO getMyInfo(String username) {
        AuthDTO reqRes = new AuthDTO();
        try {
            Optional<User> userOptional = usersRepo.findByUsername(username);
            if (userOptional.isPresent()) {
                System.out.println(userOptional.get().getEmail());
            } else {
                System.out.println("Không tìm thấy email: " + username);
            }
            if (userOptional.isPresent()) {
                reqRes.setListData(userOptional.get());
                System.out.println("So dien thoai" + userOptional.get().getPhone());
                System.out.println("Ngay sinh" + userOptional.get().getBirthday());
                reqRes.setStatusCode(200);
                reqRes.setMessage("successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("Fail");

            }

        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while getting user info: " + e.getMessage());
        }
        return reqRes;

    }

    public ResponseEntity<ApiResponse<User>> sendResetPasswordEmail(EmailRequestDTO emailRequest) {
        ApiResponse<User> response = new ApiResponse<>();
        String email = emailRequest.getTo();
        Optional<User> userOptional = usersRepo.findByEmailAndProvider(email, "Guest");

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            long expirationTime = 2 * 60 * 1000;
            String jwt = jwtUtils.generateToken(user, "reset-password", expirationTime);

            usersRepo.save(user);

            String resetLink = "http://localhost:3000/auth/reset-password?token=" + jwt;

            mailService.sendEmail(email, "Reset Password", "Click the link to reset your password: " + resetLink);

            response.setErrorCode(200);
            response.setMessage("Email đã được gửi thành công");
            response.setData(user);
            return ResponseEntity.ok(response);
        } else {
            response.setErrorCode(404);
            response.setMessage("Email không tồn tại");
            response.setData(null); // Hoặc có thể bỏ qua nếu không cần
            return ResponseEntity.status(404).body(response);
        }
    }

    public ResponseEntity<ApiResponse<User>> resetPassword(Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

        ApiResponse<User> response = new ApiResponse<>();

        if (token == null) {
            response.setErrorCode(400);
            response.setMessage("Token is required");
            return ResponseEntity.badRequest().body(response);
        }

        String username = jwtUtils.extractUsername(token);
        if (username == null || !jwtUtils.isTokenValid(token,
                new org.springframework.security.core.userdetails.User(username, "", new ArrayList<>()))) {
            response.setErrorCode(400);
            response.setMessage("Invalid or expired token");
            return ResponseEntity.badRequest().body(response);
        }

        String tokenPurpose = jwtUtils.extractClaim(token, claims -> claims.get("purpose", String.class));
        if (!"reset-password".equals(tokenPurpose)) {
            response.setErrorCode(403);
            response.setMessage("Invalid token purpose for password reset");
            return ResponseEntity.status(403).body(response);
        }

        Optional<User> userOptional = usersRepo.findByEmailAndProvider(username, "Guest");
        if (userOptional.isEmpty()) {
            response.setErrorCode(404);
            response.setMessage("User not found");
            return ResponseEntity.status(404).body(response);
        }

        if (passwordEncoder.matches(newPassword, userOptional.get().getPassword())) {
            response.setErrorCode(400);
            response.setMessage("New password cannot be the same as the old password");
            return ResponseEntity.badRequest().body(response);
        }

        if (newPassword == null || newPassword.isEmpty()) {
            response.setErrorCode(400);
            response.setMessage("New password is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (!newPassword.matches(passwordRegex)) {
            response.setErrorCode(400);
            response.setMessage(
                    "Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, one number, and one special character");
            return ResponseEntity.badRequest().body(response);
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepo.save(user);

        TokenBlacklist.blacklistToken(token);

        response.setErrorCode(200);
        response.setMessage("Password reset successfully");
        response.setData(user);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<AuthDTO> logout(@RequestHeader("Authorization") String token) {
        AuthDTO response = new AuthDTO();
        try {

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            TokenBlacklist.blacklistToken(token);

            response.setStatusCode(200);
            response.setMessage("Đăng xuất thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Có lỗi xảy ra trong quá trình đăng xuất: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to generate a 6-digit random code
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // generates a random 6-digit number
        return String.valueOf(code);
    }



}
