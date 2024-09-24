package com.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.repositories.UserJPA;
import com.entities.User;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/test/user")
public class UserControllerTest {

    @Autowired
    UserJPA userJPA;

    // Lấy danh sách tất cả người dùng (GET)
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userJPA.findAll();
        return ResponseEntity.ok(users);
    }

    // Lấy thông tin người dùng theo ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        Optional<User> user = userJPA.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Thêm người dùng mới (POST)
    @PostMapping("/add")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userJPA.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // Cập nhật thông tin người dùng theo ID (PUT)
    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userJPA.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setFullName(userDetails.getFullName());
            user.setEmail(userDetails.getEmail());
            // cập nhật các thuộc tính khác tương tự
            User updatedUser = userJPA.save(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Xóa người dùng theo ID (DELETE)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userJPA.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
