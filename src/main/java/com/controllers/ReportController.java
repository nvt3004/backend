package com.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.responsedto.WishlistProductRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.entities.User;
import com.errors.ResponseAPI;
import com.responsedto.ReportProductInventoryResponse;
import com.services.AuthService;
import com.services.JWTService;
import com.services.ProductService;
import com.services.ReportService;
import com.services.UserService;

@RestController
@RequestMapping("/api/admin/report")
public class ReportController {
    @Autowired
    AuthService authService;

    @Autowired
    JWTService jwtService;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    ReportService reportService;

    @GetMapping("/revenue")
    public ResponseEntity<ResponseAPI<BigDecimal>> getReportRevenue(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr
    ) {
        ResponseAPI<BigDecimal> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime startDate = LocalDate.parse(startDateStr, formatter).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(endDateStr, formatter).atTime(23, 59, 59);

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

        BigDecimal revenue = reportService.getReportRevenue(startDate, endDate);

        response.setCode(200);
        response.setData(revenue);
        response.setMessage("Success");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-order")
    public ResponseEntity<ResponseAPI<Integer>> getReportTotalOrder(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            @RequestParam("statusId") Integer statusId
    ) {
        ResponseAPI<Integer> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime startDate = LocalDate.parse(startDateStr, formatter).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(endDateStr, formatter).atTime(23, 59, 59);

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

        Integer total = reportService.getReportTotalOrder(startDate, endDate, statusId);

        response.setCode(200);
        response.setData(total);
        response.setMessage("Success");

        return ResponseEntity.ok(response);
    }

    //Top 5 sản phẩm bán chạy
    @GetMapping("/product-betsaler")
    public ResponseEntity<ResponseAPI<List<ReportProductInventoryResponse>>> getReportProductBestSaler(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr
    ) {
        ResponseAPI<List<ReportProductInventoryResponse>> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime startDate = LocalDate.parse(startDateStr, formatter).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(endDateStr, formatter).atTime(23, 59, 59);

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

        List<ReportProductInventoryResponse> products = reportService.getReportTopSanPhamBanChay(startDate,endDate);

        response.setCode(200);
        response.setData(products);
        response.setMessage("Success");

        return ResponseEntity.ok(response);
    }

    //Top 5 sản phẩm yêu thích nhất
    @GetMapping("/product-favorite")
    public ResponseEntity<ResponseAPI<List<WishlistProductRes>>> getReportProductFavorite(
            @RequestHeader("Authorization") Optional<String> authHeader
    ) {
        ResponseAPI<List<WishlistProductRes>> response = new ResponseAPI<>();
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

        List<WishlistProductRes> result = reportService.getTopSanPhamYeuThichNhieuNhat();

        response.setCode(200);
        response.setData(result);
        response.setMessage("Success");

        return ResponseEntity.ok(response);
    }



    @GetMapping("/total-product-buy")
    public ResponseEntity<ResponseAPI<Integer>> getReportTotalProductBuy(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr
    ) {
        ResponseAPI<Integer> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime startDate = LocalDate.parse(startDateStr, formatter).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(endDateStr, formatter).atTime(23, 59, 59);

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

        Integer totalProduct = reportService.getTotalProductBuy(startDate, endDate);

        response.setCode(200);
        response.setData(totalProduct);
        response.setMessage("Success");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profit")
    public ResponseEntity<ResponseAPI<BigDecimal>> getReportProfit(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr
    ) {
        ResponseAPI<BigDecimal> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime startDate = LocalDate.parse(startDateStr, formatter).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(endDateStr, formatter).atTime(23, 59, 59);

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

        BigDecimal profit = reportService.getTotalProfit(startDate, endDate);

        response.setCode(200);
        response.setData(profit);
        response.setMessage("Success");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-inventory")
    public ResponseEntity<ResponseAPI<List<ReportProductInventoryResponse>>> getReportProductStock(
            @RequestHeader("Authorization") Optional<String> authHeader
    ) {
        ResponseAPI<List<ReportProductInventoryResponse>> response = new ResponseAPI<>();
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

        List<ReportProductInventoryResponse> productStocks = reportService.getTop5Products();

        response.setCode(200);
        response.setData(productStocks);
        response.setMessage("Success");

        return ResponseEntity.ok(response);
    }

}
