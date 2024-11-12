package com.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.ManagePermission;
import com.entities.Permission;
import com.entities.Role;
import com.entities.User;
import com.entities.UserRole;
import com.errors.ResponseAPI;
import com.models.AuthDTO;
import com.models.UserModel;
import com.repositories.UserJPA;
import com.responsedto.ProductResponse;
import com.responsedto.UserResponse;
import com.services.AuthService;
import com.services.JWTService;
import com.services.PermissionService;
import com.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PermissionUserController {

	@Autowired
	private PermissionService permissionService;

	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;

	@Autowired
	UserService userService;

	@Autowired
	private UserJPA userRepo;

	@GetMapping("/api/admin/userpermissions/{userId}")
	public ResponseEntity<List<String>> getPermissions(@PathVariable int userId) {
		List<String> permissions = permissionService.getPermissionsByUserId(userId);

		System.out.println("Quyền của user với ID " + userId + ": " + permissions);

		if (permissions.isEmpty()) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.ok(permissions);
		}
	}

	@GetMapping("/api/admin/user/all")
	public ResponseEntity<ResponseAPI<PageImpl<UserResponse>>> getAllUser(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam(value = "keyword", defaultValue = "") String keywword,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {
		boolean statusProduct = true;
		ResponseAPI<PageImpl<UserResponse>> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		PageImpl<UserResponse> data = userService.getUserByKeyword(page - 1, size, keywword);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(data);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/admin/userpermissions/add")
	public ResponseEntity<ResponseAPI<Boolean>> addPermissions(@RequestBody UserModel userModel) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);

		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		String phoneRegex = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
		String nameRegex = "^([A-ZÀ-Ỹ][a-zà-ỹ]*(\\s[A-ZÀ-Ỹ][a-zà-ỹ]*)*)$";

		String username = userModel.getUsername();

		if (username == null || username.isEmpty()) {
			response.setCode(999);
			response.setMessage("Username cannot be empty");

			return ResponseEntity.status(999).body(response);
		}

		if (username.matches(emailRegex)) {
			Optional<User> existingUser = userRepo.findByEmailAndProvider(username, "Guest");
			if (existingUser.isPresent()) {
				response.setCode(999);
				response.setMessage("Email already exists with provider Guest");

				return ResponseEntity.status(999).body(response);
			}

		} else if (username.matches(phoneRegex)) {
			Optional<User> existingUser = userRepo.findByPhoneAndProvider(username, "Guest");
			if (existingUser.isPresent()) {
				response.setCode(999);
				response.setMessage("Phone number already exists with provider Guest");

				return ResponseEntity.status(999).body(response);
			}
		} else {
			response.setCode(999);
			response.setMessage("Invalid username format. Must be a valid email or phone number");

			return ResponseEntity.status(999).body(response);
		}

		if (userModel.getPassword() == null || userModel.getPassword().isEmpty()) {
			response.setCode(999);
			response.setMessage("Invalid password is empty");

			return ResponseEntity.status(999).body(response);
		}
		if (!userModel.getPassword().matches(passwordRegex)) {
			response.setCode(999);
			response.setMessage(
					"Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, one number, and one special character");

			return ResponseEntity.status(999).body(response);
		}

		if (userModel.getFullName() == null || userModel.getFullName().isEmpty()) {

			response.setCode(999);
			response.setMessage("Invalid full name is empty");

			return ResponseEntity.status(999).body(response);
		}
//		if (userModel.getFullName().matches(nameRegex)) {
//			response.setCode(999);
//			response.setMessage("Full name cannot contain special characters or numbers");
//
//			return ResponseEntity.status(999).body(response);
//		}
		
		Optional<User> existingUser = userRepo.findByUsername(username);
		if (existingUser.isPresent()) {
			response.setCode(999);
			response.setMessage("Username already exists");

			return ResponseEntity.status(999).body(response);
		}


		permissionService.addUser(userModel);
		response.setCode(200);
		response.setMessage("Success");
		response.setData(true);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/api/admin/userpermissions/update/{userId}")
	public ResponseEntity<AuthDTO> updateUser(HttpServletRequest request, @PathVariable Integer userId,
			@RequestBody AuthDTO authDTO) {

		AuthDTO response = permissionService.updateUser(request, userId, authDTO);
		return ResponseEntity.status(response.getStatusCode()).body(response);
	}

}
