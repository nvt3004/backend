package com.services;

import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.models.CustomerDTO;
import com.models.UserModel;
import com.repositories.ManagePermissionsJPA;
import com.repositories.PermissionJPA;
import com.repositories.RoleJPA;
import com.repositories.UserJPA;
import com.repositories.UserRoleJPA;
import com.responsedto.PermissionDto;
import com.responsedto.PermissionResponse;
import com.responsedto.UserPermissionDto;
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

    @Autowired
    PasswordEncoder passEncoder;

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
        userEntity.setProvider("Guest");

        if (userModel.getUsername().matches(emailRegex)) {
            userEntity.setEmail(userModel.getUsername());
        } else {
            userEntity.setPhone(userModel.getUsername());
        }

        User userSaved = userRepo.save(userEntity);

        // Set quyền là staff
        Role role = new Role();
        role.setId(3);
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUser(userSaved);

        // Save quyền staff cho user
        userRoleRepo.save(userRole);

        return userRepo.save(userEntity);
    }

    public User addCustomer(CustomerDTO userModel) {
        User userEntity = new User();
        String imageName = uploadService.save(userModel.getImage(), "images");

        userEntity.setUsername(userModel.getUsername());
        userEntity.setFullName(userModel.getFullName());
        userEntity.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userEntity.setImage(imageName);
        userEntity.setBirthday(userModel.getBirthday());
        userEntity.setGender(userModel.getGender());
        userEntity.setStatus(Byte.valueOf("1"));
        userEntity.setEmail(userModel.getEmail());
        userEntity.setPhone(userModel.getPhone());

        User userSaved = userRepo.save(userEntity);

        // Set quyền là khách hàng
        Role role = new Role();
        role.setId(2);
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUser(userSaved);

        // Save quyền khách hàng cho user
        userRoleRepo.save(userRole);

        return userRepo.save(userEntity);
    }

    public User updateUser(UserModel userModel, User user) {

        user.setFullName(userModel.getFullName());
        user.setGender(userModel.getGender());
        user.setBirthday(userModel.getBirthday());

        if (userModel.getPassword() != null && userModel.getPassword().trim().length() > 0) {
            user.setPassword(passEncoder.encode(userModel.getPassword()));
        }

        if (userModel.getImage() != null && !userModel.getImage().isBlank() && !userModel.getImage().isEmpty()) {
            String image = user.getImage();

            if (image != null && !image.isBlank()) {
                uploadService.delete(image, "images");
            }
            user.setImage(uploadService.save(userModel.getImage(), "images"));
        }

        return userRepo.save(user);
    }

    public User updateCustomer(UserModel userModel, User user) {

        user.setFullName(userModel.getFullName());
        user.setGender(userModel.getGender());
        user.setPhone(user.getPhone());
        user.setEmail(userModel.getEmail());
        user.setBirthday(userModel.getBirthday());

        if (userModel.getPassword() != null && userModel.getPassword().trim().length() > 0) {
            user.setPassword(passEncoder.encode(userModel.getPassword()));
        }

        if (userModel.getImage() != null && !userModel.getImage().isBlank() && !userModel.getImage().isEmpty()) {
            String image = user.getImage();

            if (image != null && !image.isBlank()) {
                uploadService.delete(image, "images");
            }
            user.setImage(uploadService.save(userModel.getImage(), "images"));
        }

        return userRepo.save(user);
    }

    public User deleteUser(int userId) {
        User user = userRepo.findById(userId).orElse(null);

        user.setStatus(Byte.valueOf("0"));
        return userRepo.save(user);
    }

    public User deleteCustomer(int userId) {
        User user = userRepo.findById(userId).orElse(null);

        if (user.getStatus() == 1) {
            user.setStatus(Byte.valueOf("0"));
        } else {
            user.setStatus(Byte.valueOf("1"));
        }

        return userRepo.save(user);
    }

    public List<PermissionResponse> getAllPermissionByUser(Integer userId) {
        User user = userRepo.findById(userId).orElse(null);
        List<Permission> permissions = permissionRepo.findAll();
        Map<String, List<PermissionDto>> responsePermissions = new LinkedHashMap<>();
        List<Permission> permissionOfUser = maPerJPA.findPermissionsByUser(user.getUserId());

        for (Permission pms : permissions) {
            String title = extractTitleForSpecialPermissions(pms.getPermissionName());
            if (title.equalsIgnoreCase("user")) {
                continue;
            }
            responsePermissions.put(title, new ArrayList<PermissionDto>());
        }

        for (Permission pms : permissions) {
            String title = extractTitleForSpecialPermissions(pms.getPermissionName());
            String action = extractActionForSpecialPermissions(pms.getPermissionName());

            if (title.equalsIgnoreCase("user")) {
                continue;
            }

            responsePermissions.get(title).add(new PermissionDto(pms.getPermissionId(), action, false));
            responsePermissions.put(title, responsePermissions.get(title));
        }

        List<PermissionResponse> list = new ArrayList<>();

        for (Map.Entry<String, List<PermissionDto>> entry : responsePermissions.entrySet()) {
            for (PermissionDto permission : entry.getValue()) {
                if (isPermissionUserExit(permissionOfUser, permission)) {
                    permission.setUse(true);
                }
            }

            list.add(new PermissionResponse(entry.getKey(), entry.getValue()));
        }

        return list;
    }

    public boolean saveUserPermission(List<PermissionDto> pers, User user) {

        try {
            for (PermissionDto p : pers) {
                ManagePermission managePermissions = userPermissionRepo.findOneManagePermission(user.getUserId(),
                        p.getId());

                // Nếu có rồi mà bỏ quyền thì xóa
                if (managePermissions != null && !p.isUse()) {
                    userPermissionRepo.delete(managePermissions);
                } else if (managePermissions == null && p.isUse()) { // Nếu quyền chưa có thì thêm mới
                    ManagePermission mangePerEntity = new ManagePermission();
                    Permission perEntity = new Permission();
                    perEntity.setPermissionId(p.getId());

                    mangePerEntity.setUser(user);
                    mangePerEntity.setPermission(perEntity);

                    userPermissionRepo.save(mangePerEntity);
                }
                // Ngược lại quyền tồn tại thì kh làm gì hết
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPermissionExit(int idPer) {
        Permission permission = permissionRepo.findById(idPer).orElse(null);

        return permission != null;
    }

    public boolean isPermissionUserExit(List<Permission> permissionOfUser, PermissionDto perdto) {
        for (Permission pu : permissionOfUser) {
            if (pu.getPermissionId() == perdto.getId()) {
                return true;
            }
        }

        return false;
    }

    private String extractTitleForSpecialPermissions(String permissionName) {
        permissionName = permissionName.toLowerCase();

        if (permissionName.contains("user")) {
            return "User";
        } else if (permissionName.contains("product")) {
            return "Product";
        } else if (permissionName.contains("order")) {
            return "Order";
        } else if (permissionName.contains("receipt")) {
            return "Receipt";
        } else if (permissionName.contains("advertisement")) {
            return "Advertisement";
        } else if (permissionName.contains("attribute")) {
            return "Attribute";
        } else if (permissionName.contains("coupon")) {
            return "Coupon";
        } else if (permissionName.contains("feedback")) {
            return "Feedback";
        } else if (permissionName.contains("supplier")) {
            return "Supplier";
        } else if (permissionName.contains("sale")) {
            return "Sale";
        } else if (permissionName.contains("category")) {
            return "Category";
        } else if (permissionName.contains("reply")) {
            return "Reply";
        } else {
            return "None"; // Default title
        }
    }

    private String extractActionForSpecialPermissions(String permissionName) {
        permissionName = permissionName.toLowerCase();

        if (permissionName.contains("add")) {
            return "Add";
        } else if (permissionName.contains("update")) {
            return "Update";
        } else if (permissionName.contains("view")) {
            return "View";
        } else if (permissionName.contains("delete")) {
            return "Delete";
        } else {
            return "Unknown Action"; // Default action
        }
    }

}
