package com.controllers;

import com.entities.Product;
import com.entities.ProductVersion;
import com.entities.Sale;
import com.entities.User;
import com.errors.ResponseAPI;
import com.models.SaleDTO;
import com.models.VersionSaleDTO;
import com.repositories.ProductVersionJPA;
import com.repositories.SaleJPA;
import com.responsedto.SaleResponse;
import com.responsedto.Version;
import com.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/sale")
public class SaleController {
    @Autowired
    private SaleService saleService;

    @Autowired
    AuthService authService;

    @Autowired
    JWTService jwtService;

    @Autowired
    UserService userService;

    @Autowired
    ProductVersionJPA productVersionJPA;

    @Autowired
    SaleJPA saleJPA;

    @GetMapping("/all")
    @PreAuthorize("hasPermission(#userId, 'View Sale')")
    public ResponseEntity<ResponseAPI<Page<SaleResponse>>> getSales
            (
                    @RequestHeader("Authorization") Optional<String> authHeader,
                    @RequestParam(name = "page", defaultValue = "1") int page,
                    @RequestParam(name = "size", defaultValue = "8") int size,
                    @RequestParam(name = "keyword", defaultValue = "") String keyword,
                    @RequestParam(name = "startDate", defaultValue = "") String startDateStr,
                    @RequestParam(name = "endDate", defaultValue = "") String endDateStr,
                    @RequestParam(name = "status", defaultValue = "-1") int status
            ) {
        ResponseAPI<Page<SaleResponse>> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime startDate = LocalDate.parse(startDateStr, formatter).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(endDateStr, formatter).atTime(23, 59, 59);
        keyword = "%" + keyword + "%";

        Page<SaleResponse> sales = saleService.getAllSales(page - 1, size, keyword, startDate, endDate, status);

        response.setCode(200);
        response.setMessage("Success");
        response.setData(sales);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    @PreAuthorize("hasPermission(#userId, 'Add Sale')")
    public ResponseEntity<ResponseAPI<Boolean>> addSale
            (
                    @RequestHeader("Authorization") Optional<String> authHeader,
                    @RequestBody SaleDTO saleDTO
            ) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (saleDTO.getSaleName() == null || saleDTO.getSaleName().isEmpty() || saleDTO.getSaleName().isBlank()) {
            response.setCode(999);
            response.setMessage("Tên chương trình giảm giá không được để trống");

            return ResponseEntity.status(999).body(response);
        }

        if (saleDTO.getPercent().compareTo(BigDecimal.ZERO) < 0) {
            response.setCode(999);
            response.setMessage("Phần trăm giảm giá phải lớn hơn 0");

            return ResponseEntity.status(999).body(response);
        }

        if (!saleDTO.getStartDate().isAfter(LocalDateTime.now())) {
            response.setCode(999);
            response.setMessage("Thời gian bắt đầu phải từ thời gian hiện tại trở đi");

            return ResponseEntity.status(999).body(response);
        }

        if (!saleDTO.getEndDate().isAfter(saleDTO.getStartDate())) {
            response.setCode(999);
            response.setMessage("Thời gian kết thúc phải lớn hơn thời gian bắt đầu");

            return ResponseEntity.status(999).body(response);
        }

        if (saleDTO.getVersionIds() == null) {
            response.setCode(999);
            response.setMessage("Danh sách sản phẩm không được để trống");

            return ResponseEntity.status(999).body(response);
        }

        if (saleDTO.getVersionIds().isEmpty()) {
            response.setCode(999);
            response.setMessage("Danh sách sản phẩm không được để trống");

            return ResponseEntity.status(999).body(response);
        }

        for (Integer versionId : saleDTO.getVersionIds()) {
            ProductVersion pdVs = productVersionJPA.findById(versionId).orElse(null);
            if (pdVs != null && (pdVs.isStatus() && pdVs.getProduct().isStatus())) {
                continue;
            }

            response.setCode(999);
            response.setMessage("Sản phẩm không tồn tại");

            return ResponseEntity.status(999).body(response);
        }

        Set<Integer> set = new HashSet<>(saleDTO.getVersionIds());

        if (set.size() < saleDTO.getVersionIds().size()) {
            response.setCode(999);
            response.setMessage("Sản phẩm không được trùng");

            return ResponseEntity.status(999).body(response);
        }

        if(saleService.duplicateVersionSaleStartedAdd(saleDTO)){
            response.setCode(999);
            response.setMessage("Sản phẩm đã áp dụng trong một chương trình giảm giá khác trong cùng khoảng thời gian");

            return ResponseEntity.status(999).body(response);
        }

        saleService.addSale(saleDTO);

        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/update")
    @PreAuthorize("hasPermission(#userId, 'Update Sale')")
    public ResponseEntity<ResponseAPI<Boolean>> updateSale
            (
                    @RequestHeader("Authorization") Optional<String> authHeader,
                    @RequestBody SaleDTO saleDTO
            ) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (saleDTO.getId() == null) {
            response.setCode(999);
            response.setMessage("Chương trình giảm giá không tồn tại");

            return ResponseEntity.status(999).body(response);
        }

        if (!saleJPA.findById(saleDTO.getId()).isPresent()) {
            response.setCode(999);
            response.setMessage("Chương trình giảm giá không tồn tại");

            return ResponseEntity.status(999).body(response);
        }

        if (saleService.getSaleById(saleDTO.getId()) != null) {
            response.setCode(999);
            response.setMessage("Chỉ có thể cập nhật thông tin khi chương trình giảm giá chưa bắt đầu");

            return ResponseEntity.status(999).body(response);
        }

        if (saleDTO.getSaleName() == null || saleDTO.getSaleName().isEmpty() || saleDTO.getSaleName().isBlank()) {
            response.setCode(999);
            response.setMessage("Tên chương trình giảm giá không được để trống");

            return ResponseEntity.status(999).body(response);
        }

        if (saleDTO.getPercent().compareTo(BigDecimal.ZERO) < 0) {
            response.setCode(999);
            response.setMessage("Phần trăm giảm giá phải lớn hơn 0");

            return ResponseEntity.status(999).body(response);
        }


        if (saleDTO.getVersionSaleDTOS() == null) {
            response.setCode(999);
            response.setMessage("Danh sách sản phẩm không được để trống");

            return ResponseEntity.status(999).body(response);
        }

        if (saleDTO.getVersionSaleDTOS().isEmpty()) {
            response.setCode(999);
            response.setMessage("Danh sách sản phẩm không được để trống");

            return ResponseEntity.status(999).body(response);
        }

        for (VersionSaleDTO versionId : saleDTO.getVersionSaleDTOS()) {
            ProductVersion pdVs = productVersionJPA.findById(versionId.getIdVersion()).orElse(null);

            if (pdVs != null && (pdVs.isStatus() && pdVs.getProduct().isStatus())) {
                continue;
            }

            response.setCode(999);
            response.setMessage("Sản phẩm không tồn tại");

            return ResponseEntity.status(999).body(response);
        }

        Set<Integer> set = new HashSet<>(saleDTO.getVersionSaleDTOS().stream().map(i -> i.getIdVersion()).toList());

        if (set.size() < saleDTO.getVersionSaleDTOS().size()) {
            response.setCode(999);
            response.setMessage("Sản phẩm không được trùng");

            return ResponseEntity.status(999).body(response);
        }

        if(saleService.duplicateVersionSaleStartedUpdate(saleDTO)){
            response.setCode(999);
            response.setMessage("Sản phẩm đã áp dụng trong một chương trình giảm giá khác trong cùng khoảng thời gian");

            return ResponseEntity.status(999).body(response);
        }

        saleService.updateSale(saleDTO);

        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-status")
    @PreAuthorize("hasPermission(#userId, 'Update Sale')")
    public ResponseEntity<ResponseAPI<Boolean>> updateStatusSale
            (
                    @RequestHeader("Authorization") Optional<String> authHeader,
                    @RequestBody SaleDTO saleDTO
            ) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (saleDTO.getId() == null) {
            response.setCode(999);
            response.setMessage("Id chương trình giảm giá không hợp lệ");

            return ResponseEntity.status(999).body(response);
        }

        if (!saleJPA.findById(saleDTO.getId()).isPresent()) {
            response.setCode(999);
            response.setMessage("Chương trình giảm giá không tồn tại");

            return ResponseEntity.status(999).body(response);
        }

        Sale sale = saleService.getSaleById(saleDTO.getId());

        if (sale == null) {
            response.setCode(999);
            response.setMessage("Chỉ được đổi trạng thái khi chương trình giảm giá đang diễn ra");

            return ResponseEntity.status(999).body(response);
        }

        sale.setStatus(saleDTO.isStatus());
        saleService.updateStatusSale(sale);
        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasPermission(#userId, 'Delete Sale')")
    public ResponseEntity<ResponseAPI<Boolean>> deleteSale
            (
                    @RequestHeader("Authorization") Optional<String> authHeader,
                    @PathVariable("id") Integer id
            ) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (id == null) {
            response.setCode(999);
            response.setMessage("Id chương trình giảm giá không hợp lệ");

            return ResponseEntity.status(999).body(response);
        }

        if (!saleJPA.findById(id).isPresent()) {
            response.setCode(999);
            response.setMessage("Chương trình giảm giá không tồn tại");

            return ResponseEntity.status(999).body(response);
        }

        if (saleService.getSaleById(id) != null) {
            response.setCode(999);
            response.setMessage("Không thể xóa chương trình giảm giá đang diễn ra");

            return ResponseEntity.status(999).body(response);
        }


        Sale sale = saleJPA.findById(id).orElse(null);

        saleService.deleteSale(sale);
        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }
}
