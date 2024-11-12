package com.services;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.entities.ManagePermission;
import com.entities.Permission;
import com.entities.Role;
import com.entities.User;
import com.entities.UserRole;
import com.models.AuthDTO;
import com.models.UserModel;
import com.repositories.ManagePermissionsJPA;
import com.repositories.PermissionJPA;
import com.repositories.RoleJPA;
import com.repositories.UserJPA;
import com.repositories.UserRoleJPA;
import com.utils.DateTimeUtil;
import com.utils.JWTUtils;
import com.utils.UploadService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class PermissionService {
	@Autowired
	private ManagePermissionsJPA maPerJPA;

	@Autowired
	private RoleJPA roleRepo;

	@Autowired
	private UserJPA userRepo;

	@Autowired
	private UserRoleJPA userRoleRepo;

	@Autowired
	private ManagePermissionsJPA userPermissionRepo;

	@Autowired
	private JWTUtils jwtUtils;

	@Autowired
	private PermissionJPA permissionRepo;

	Date datecurrent = DateTimeUtil.getCurrentDateInVietnam();

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	UploadService uploadService;

	public List<String> getPermissionsByUserId(int userId) {
		return maPerJPA.findPermissionsByUserId(userId);
	}

	public List<Permission> getAllPermission() {
		return permissionRepo.findAll();
	}

	public AuthDTO addPermissions(AuthDTO registrationRequest, @RequestBody List<String> permissions) {
		AuthDTO resp = new AuthDTO();

		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		String phoneRegex = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
		String nameRegex = "^([A-ZÀ-Ỹ][a-zà-ỹ]*(\\s[A-ZÀ-Ỹ][a-zà-ỹ]*)*)$";

		try {
			String username = registrationRequest.getUsername();

			if (username == null || username.isEmpty()) {
				throw new IllegalArgumentException("Username cannot be empty");
			}

			if (username.matches(emailRegex)) {
				Optional<User> existingUser = userRepo.findByEmailAndProvider(username, "Guest");
				if (existingUser.isPresent()) {
					throw new IllegalArgumentException("Email already exists with provider Guest");
				}
			} else if (username.matches(phoneRegex)) {
				Optional<User> existingUser = userRepo.findByPhoneAndProvider(username, "Guest");
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

			if (registrationRequest.getFullName() == null || registrationRequest.getFullName().isEmpty()) {
				throw new IllegalArgumentException("Invalid full name is empty");
			}
			if (!registrationRequest.getFullName().matches(nameRegex)) {
				throw new IllegalArgumentException("Full name cannot contain special characters or numbers");
			}

			User ourUser = new User();
			ourUser.setEmail(username.matches(emailRegex) ? username : null);
			ourUser.setPhone(username.matches(phoneRegex) ? username : null);
			ourUser.setFullName(registrationRequest.getFullName());
			ourUser.setUsername(username);
			ourUser.setCreateDate(datecurrent);
			ourUser.setProvider("Guest");
			ourUser.setStatus((byte) 1);
			ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
			User ourUsersResult = userRepo.save(ourUser);

			List<String> roles = Collections.singletonList("Staff");
			List<UserRole> userRoles = roles.stream().map(roleName -> {
				Role role = roleRepo.findByRoleName(roleName)
						.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
				UserRole userRole = new UserRole();
				userRole.setUser(ourUsersResult);
				userRole.setRole(role);
				return userRole;
			}).collect(Collectors.toList());

			userRoleRepo.saveAll(userRoles);

			List<String> listPermission = registrationRequest.getPermissions();

			List<ManagePermission> userPermissions = listPermission.stream().map(permissionName -> {
				Permission permission = permissionRepo.findByPermissionName(permissionName)
						.orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));
				ManagePermission userPermission = new ManagePermission();
				userPermission.setUser(ourUsersResult);
				userPermission.setPermission(permission);
				return userPermission;
			}).collect(Collectors.toList());

			userPermissionRepo.saveAll(userPermissions);

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

	@Transactional
	public AuthDTO updateUser(HttpServletRequest request, Integer userId, AuthDTO authDTO) {
		AuthDTO reqRes = new AuthDTO();
		System.out.println("userid = " + userId);
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

				Optional<User> userOptional = userRepo.findById(userId);
				if (userOptional.isPresent()) {
					User existingUser = userOptional.get();

					if (authDTO.getPassword() != null && !authDTO.getPassword().isEmpty()) {
						existingUser.setPassword(passwordEncoder.encode(authDTO.getPassword()));
					}

					existingUser.setEmail(authDTO.getEmail());
					existingUser.setFullName(authDTO.getFullName());
					existingUser.setUsername(authDTO.getUsername());
					existingUser.setPhone(authDTO.getPhone());
					existingUser.setImage(authDTO.getImage());

					User savedUser = userRepo.save(existingUser);

					System.out.println("Quyền = " + existingUser.getManagePermissions());

					System.out.println("Quyền2 = " + authDTO.getPermissions());

					if (authDTO.getPermissions() != null) {

						userPermissionRepo.deleteByUser(existingUser);
						System.out.println("Deleted old permissions for user: " + existingUser.getUserId());

						for (String permissionName : authDTO.getPermissions()) {
							Optional<Permission> permissionOptional = permissionRepo
									.findByPermissionName(permissionName);
							if (permissionOptional.isPresent()) {
								Permission permission = permissionOptional.get();
								ManagePermission managePermission = new ManagePermission();
								managePermission.setUser(existingUser);
								managePermission.setPermission(permission);
								userPermissionRepo.save(managePermission);
								System.out.println("Added permission: " + permissionName + " for user: "
										+ existingUser.getUserId());
							} else {

								reqRes.setStatusCode(400);
								reqRes.setMessage("Permission not found: " + permissionName);
								return reqRes;
							}
						}
					}

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

	public User addUser(UserModel userModel) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		User userEntity = new User();
		String imageName = uploadService.save(userModel.getImage(), "images");

		userEntity.setUsername(userModel.getUsername());
		userEntity.setFullName(userModel.getFullName());
		userEntity.setPassword(passwordEncoder.encode(userModel.getPassword()));
		userEntity.setImage(imageName);
		userEntity.setBirthday(userModel.getBirthday());
		userEntity.setGender(userModel.getGender());
		userEntity.setStatus(Byte.valueOf("1"));
		if(userModel.getUsername().matches(emailRegex)) {
			userEntity.setEmail(userModel.getUsername());
		}else {
			userEntity.setPhone(userModel.getUsername());
		}
		
		User userSaved = userRepo.save(userEntity);
		
		//Set quyền là staff
		Role role = new Role();
		role.setId(3);
		UserRole userRole = new UserRole();
		userRole.setRole(role);
		userRole.setUser(userSaved);
		
		//Save quyền staff cho user
		userRoleRepo.save(userRole);

		return userRepo.save(userEntity);
	}

}
