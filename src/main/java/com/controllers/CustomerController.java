package com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.User;
import com.errors.ResponseAPI;
import com.models.CustomerDTO;
import com.models.UserModel;
import com.repositories.UserJPA;
import com.responsedto.PermissionDto;
import com.responsedto.PermissionResponse;
import com.responsedto.UserPermissionDto;
import com.responsedto.UserResponse;
import com.services.AuthService;
import com.services.JWTService;
import com.services.MailService;
import com.services.PermissionService;
import com.services.UserService;

@RestController
@RequestMapping("/api/admin/customer")
public class CustomerController {

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

	@Autowired
	MailService mailService;

	// Lấy danh sách user
	@GetMapping("/all")
	public ResponseEntity<ResponseAPI<PageImpl<UserResponse>>> getAllUser(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam(value = "keyword", defaultValue = "") String keywword,
			@RequestParam(value = "status", defaultValue = "1") byte status,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {
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

		PageImpl<UserResponse> data = userService.getUserByKeyword(page - 1, size, keywword, status, 2);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(data);

		return ResponseEntity.ok(response);
	}

//	@PostMapping("/add")
//	public ResponseEntity<ResponseAPI<Boolean>> addUser(@RequestBody CustomerDTO userModel) {
//		ResponseAPI<Boolean> response = new ResponseAPI<>();
//		response.setData(false);
//
//		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
//		String phoneRegex = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
//		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
//
//		String username = userModel.getUsername();
//
//		if (username == null || username.isEmpty()) {
//			response.setCode(999);
//			response.setMessage("Username cannot be empty");
//
//			return ResponseEntity.status(999).body(response);
//		}
//
//		if (username.matches(emailRegex)) {
//			Optional<User> existingUser = userRepo.findByEmailAndProvider(username, "Guest");
//			if (existingUser.isPresent()) {
//				response.setCode(999);
//				response.setMessage("Email already exists with provider Guest");
//
//				return ResponseEntity.status(999).body(response);
//			}
//
//		}
//
//		if (username.matches(phoneRegex)) {
//			Optional<User> existingUser = userRepo.findByPhoneAndProvider(username, "Guest");
//			if (existingUser.isPresent()) {
//				response.setCode(999);
//				response.setMessage("Phone number already exists with provider Guest");
//
//				return ResponseEntity.status(999).body(response);
//			}
//		}
//
//		if (userModel.getPassword() == null || userModel.getPassword().isEmpty()) {
//			response.setCode(999);
//			response.setMessage("Invalid password is empty");
//
//			return ResponseEntity.status(999).body(response);
//		}
//
//		if (!userModel.getPassword().matches(passwordRegex)) {
//			response.setCode(999);
//			response.setMessage(
//					"Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, one number, and one special character");
//
//			return ResponseEntity.status(999).body(response);
//		}
//
//		if (userModel.getFullName() == null || userModel.getFullName().isEmpty()) {
//
//			response.setCode(999);
//			response.setMessage("Invalid full name is empty");
//
//			return ResponseEntity.status(999).body(response);
//		}
//
//		Optional<User> existingUser = userRepo.findByUsername(username);
//		if (existingUser.isPresent()) {
//			response.setCode(999);
//			response.setMessage("Username already exists");
//
//			return ResponseEntity.status(999).body(response);
//		}
//
//		permissionService.addCustomer(userModel);
//		response.setCode(200);
//		response.setMessage("Success");
//		response.setData(true);
//		return ResponseEntity.ok(response);
//	}
//
//	@PutMapping("/update")
//	public ResponseEntity<ResponseAPI<Boolean>> updateUser(@RequestHeader("Authorization") Optional<String> authHeader,
//			@RequestBody UserModel userModel) {
//		ResponseAPI<Boolean> response = new ResponseAPI<>();
//		response.setData(false);
//
//		String token = authService.readTokenFromHeader(authHeader);
//		String username = jwtService.extractUsername(token);
//		User userLogin = userService.getUserByUsername(username);
//
//		if (userLogin == null) {
//			response.setCode(404);
//			response.setMessage("Account not found");
//
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//		}
//
//		if (userLogin.getStatus() == 0) {
//			response.setCode(403);
//			response.setMessage("Account locked");
//
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//		}
//
//		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
//		User user = userRepo.findById(userModel.getId()).orElse(null);
//
//		if (user == null) {
//			response.setCode(404);
//			response.setMessage("Update failed because user does not exist!");
//
//			return ResponseEntity.status(404).body(response);
//		}
//
//		// Nếu cập nhật admin thì kh cho
//		if (user.getUserRoles().get(0).getRole().getId() == 1) {
//			response.setCode(404);
//			response.setMessage("Update failed because user does not exist!");
//
//			return ResponseEntity.status(404).body(response);
//		}
//
//		if (user.getStatus() == 0) {
//			response.setCode(404);
//			response.setMessage("Update failed because user does not exist!");
//
//			return ResponseEntity.status(404).body(response);
//		}
//
//		if (userModel.getFullName() == null || userModel.getFullName().isEmpty()) {
//
//			response.setCode(999);
//			response.setMessage("Invalid full name is empty");
//
//			return ResponseEntity.status(999).body(response);
//		}
//
//		if (userModel.getPassword() != null && !userModel.getPassword().isBlank()
//				&& !userModel.getPassword().matches(passwordRegex)) {
//			response.setCode(999);
//			response.setMessage(
//					"Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, one number, and one special character");
//
//			return ResponseEntity.status(999).body(response);
//		}
//		
//		if(userModel.getEmail() != null && !userModel.getEmail().isBlank() && !userModel.getEmail().isEmpty()) {
//			User exitEmail = userRepo.findByEmailAndProviderUpdate(username, passwordRegex, 0);
//		}
//
//		permissionService.updateCustomer(userModel, user);
//		response.setCode(200);
//		response.setMessage("Success");
//		response.setData(true);
//		return ResponseEntity.ok(response);
//	}

	@GetMapping("/delete")
	public ResponseEntity<ResponseAPI<Boolean>> deleteUser(@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam(value = "reason", defaultValue = "") String reason,
			@RequestParam(value = "id", defaultValue = "0") Integer userId) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);
		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);

		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(403).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(403).body(response);
		}

		User exitUser = userRepo.findById(userId).orElse(null);

		if (exitUser == null) {
			response.setCode(999);
			response.setMessage("Thất bại bởi vì tài khoản không tồn tại!");

			return ResponseEntity.status(999).body(response);
		}

		if (user.getUserId() == exitUser.getUserId()) {
			response.setCode(999);
			response.setMessage("Bạn không thể khóa tài khoản chính bạn!");

			return ResponseEntity.status(999).body(response);
		}

		// Nếu cập nhật admin thì kh cho
		if (exitUser.getUserRoles().get(0).getRole().getId() == 1) {
			response.setCode(404);
			response.setMessage("Thất bại bởi vì tài khoản không tồn tại!");

			return ResponseEntity.status(404).body(response);
		}

		if (reason.trim().length() == 0 && exitUser.getStatus() == 1) {
			response.setCode(999);
			response.setMessage("Không được để trống lý do!");

			return ResponseEntity.status(999).body(response);
		}

		// Nếu trước đó bị khóa thì mở khóa và ngược lại
		String email = exitUser.getEmail() != null ? exitUser.getEmail() : "minhty295@gmail.com";
		if (exitUser.getStatus() == 1) {
			mailService.sendEmail(email, "Step To Future Shop Thông Báo",
					"Tài khoản của bạn bị khóa vì lý do: "
							+ reason);
		}else{
			mailService.sendEmail(email, "Step To Future Shop Thông Báo",
					"Tài khoản của bạn đã được mở khóa, bây giờ bạn có thể đăng nhập vào hệ thống!");
		}

		permissionService.deleteCustomer(exitUser.getUserId());
		response.setCode(200);
		response.setMessage("Success");
		return ResponseEntity.ok(response);
	}

}
