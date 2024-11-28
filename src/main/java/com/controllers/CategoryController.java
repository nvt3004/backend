package com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.entities.Category;
import com.entities.User;
import com.errors.ResponseAPI;
import com.repositories.CategoryJPA;
import com.responsedto.CategoryDTO;
import com.services.AuthService;
import com.services.CategoryService;
import com.services.JWTService;
import com.services.UserService;

@RestController
@RequestMapping("api/home/category")
public class CategoryController {

    @Autowired
    AuthService authService;

    @Autowired
    JWTService jwtService;

    @Autowired
    UserService userService;

    @Autowired
    CategoryService catService;

    @Autowired
    CategoryJPA catJPA;

    @GetMapping("/get-all")
    public ResponseEntity<ResponseAPI<PageImpl<CategoryDTO>>> getAllCategory(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam("keyword") Optional<String> keyword, @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {
        ResponseAPI<PageImpl<CategoryDTO>> response = new ResponseAPI<>();
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

        PageImpl<CategoryDTO> cats = catService.getCategorysByKeyword(page.orElse(-1) - 1, size.orElse(5),
                "%" + keyword.orElse("") + "%");

        response.setCode(200);
        response.setMessage("Success");
        response.setData(cats);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
	@PreAuthorize("hasPermission(#userId, 'Add Category')")
    public ResponseEntity<ResponseAPI<Boolean>> addCategory(@RequestHeader("Authorization") Optional<String> authHeader,
            @RequestBody CategoryDTO catModal) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
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

        if (catModal.getCategoryName().isEmpty() || catModal.getCategoryName().isBlank() || catModal.getCategoryName() == null) {
            response.setCode(400);
            response.setMessage("Category name invalid");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        Category cat = catJPA.getCategoryByName(catModal.getCategoryName());

        if (cat != null) {
            response.setCode(400);
            response.setMessage("Category name already exists");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        catService.addCategory(catModal.getCategoryName());
        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
	@PreAuthorize("hasPermission(#userId, 'Update Category')")
    public ResponseEntity<ResponseAPI<Boolean>> updateCategory(
            @RequestHeader("Authorization") Optional<String> authHeader, @RequestBody CategoryDTO catModal) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
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

        if (catModal.getCategoryId() == null) {
            response.setCode(400);
            response.setMessage("Category id invalid");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (catModal.getCategoryName().isEmpty() || catModal.getCategoryName().isBlank() || catModal.getCategoryName() == null) {
            response.setCode(400);
            response.setMessage("Category name invalid");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Category cat = catJPA.findById(catModal.getCategoryId()).orElse(null);

        if (cat == null) {
            response.setCode(404);
            response.setMessage("Category id not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        cat.setCategoryName(catModal.getCategoryName());
        catService.updateCategory(cat);
        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{id}")
	@PreAuthorize("hasPermission(#userId, 'Delete Category')")
    public ResponseEntity<ResponseAPI<Boolean>> updateCategory(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @PathVariable("id") Optional<Integer> id) {

        ResponseAPI<Boolean> response = new ResponseAPI<>();
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

        if (id.orElse(null) == null) {
            response.setCode(400);
            response.setMessage("Category id invalid");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Category cat = catJPA.findById(id.get()).orElse(null);

        if (cat == null) {
            response.setCode(404);
            response.setMessage("Category id not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        catService.removeCategory(cat.getCategoryId());
        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/get-all")
    public ResponseEntity<ResponseAPI<List<Category>>> getAllCategoryDashboard(
            @RequestHeader("Authorization") Optional<String> authHeader) {
        ResponseAPI<List<Category>> response = new ResponseAPI<>();
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

        List<Category> cats = catJPA.findAll();

        response.setCode(200);
        response.setMessage("Success");
        response.setData(cats);

        return ResponseEntity.ok(response);
    }
}
