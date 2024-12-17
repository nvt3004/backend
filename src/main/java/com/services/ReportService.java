package com.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entities.Product;
import com.responsedto.WishlistProductRes;
import com.utils.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Order;
import com.entities.OrderDetail;
import com.repositories.OrderJPA;
import com.repositories.ProductJPA;
import com.responsedto.ReportProductInventoryResponse;

@Service
public class ReportService {
    @Autowired
    OrderJPA orderJpa;

    @Autowired
    ProductJPA productJPA;

    @Autowired
    UploadService uploadService;

    // Thống kê doanh thu theo ngày
    public BigDecimal getReportRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderJpa.getAllOrderByDate(startDate, endDate);
        BigDecimal total = BigDecimal.ZERO;

        System.out.println("Ngày băt đầu: " + startDate);
        System.out.println("Ngày ket thuc: " + endDate);

        for (Order od : orders) {
            BigDecimal totalDetail = BigDecimal.ZERO;
            BigDecimal disCount = BigDecimal.ZERO;
            System.out.println("Hoa don: " + od.getOrderId() + " - " + od.getOrderDate());
            for (OrderDetail dt : od.getOrderDetails()) {
                totalDetail = totalDetail.add(dt.getPrice().multiply(BigDecimal.valueOf(dt.getQuantity())));
            }

            if (od.getCoupon() != null) {
                if (od.getCoupon().getDisPercent() != null) {
                    disCount = totalDetail.multiply(
                            BigDecimal.ONE.subtract(od.getCoupon().getDisPercent().divide(new BigDecimal("100"))));
                    totalDetail = disCount;
                } else {
                    disCount = totalDetail.subtract(od.getCoupon().getDisPrice());
                    totalDetail = disCount;
                }
            }

            total = total.add(totalDetail);
        }

        return total;
    }

    // Thống kê sản phẩm bán chạy nhất
    public List<ReportProductInventoryResponse> getReportTopSanPhamBanChay(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderJpa.getAllOrderByDate(startDate, endDate);
        Map<String, Integer> mapProducts = new LinkedHashMap<>();
        List<ReportProductInventoryResponse> result = new ArrayList<>();
        Integer top = 5;

        for (Order od : orders) {
            for (OrderDetail dt : od.getOrderDetails()) {
                String productName = dt.getProductVersionBean().getVersionName();
                Integer quantity = mapProducts.get(productName);

                System.out.println("Vô: " + productName + " - " + dt.getQuantity());

                // Nếu quantity là null, bắt đầu từ giá trị của dt.getQuantity()
                mapProducts.put(productName, (quantity == null ? 0 : quantity) + dt.getQuantity());
            }
        }

        for (Map.Entry<String, Integer> entry : mapProducts.entrySet()) {
            result.add(new ReportProductInventoryResponse(entry.getKey(), entry.getValue()));
        }

        result.sort((a, b) -> b.getQuantity().compareTo(a.getQuantity()));
        result = result.subList(0, Math.min(top, result.size()));


        return result;
    }


    // Thống kê tổng số hóa đơn theo trạng thái
    public Integer getReportTotalOrder(LocalDateTime startDate, LocalDateTime endDate, int statusId) {
        List<Order> orders = new ArrayList<>();

        if (statusId != -1) {
            orders = orderJpa.getAllOrderByDateAndStatus(startDate, endDate, statusId);
        } else {
            orders = orderJpa.getAllOrders(startDate, endDate);
        }

        return orders.size();
    }

    // Thống kê các sản phẩm đã bán theo ngày
    public Integer getTotalProductBuy(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderJpa.getAllOrderByDate(startDate, endDate);
        Integer total = 0;

        for (Order od : orders) {
            for (OrderDetail dt : od.getOrderDetails()) {
                total += dt.getQuantity();
            }
        }

        return total;
    }

    // Thống kê lợi nhuận theo ngày
    public BigDecimal getTotalProfit(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderJpa.getAllOrderByDate(startDate, endDate);
        BigDecimal total = BigDecimal.ZERO;

        for (Order od : orders) {
            BigDecimal disCount = BigDecimal.ZERO;
            BigDecimal totalDetail = BigDecimal.ZERO;

            for (OrderDetail dt : od.getOrderDetails()) {
                BigDecimal buyPrice = dt.getPrice().multiply(BigDecimal.valueOf(dt.getQuantity()));
                BigDecimal importPrice = dt.getProductVersionBean().getImportPrice()
                        .multiply(BigDecimal.valueOf(dt.getQuantity()));
                BigDecimal profit = buyPrice.subtract(importPrice);

                total = total.add(profit);
                totalDetail = totalDetail.add(buyPrice);
            }

            if (od.getCoupon() != null) {
                if (od.getCoupon().getDisPercent() != null) {
                    disCount = totalDetail.multiply(od.getCoupon().getDisPercent().divide(new BigDecimal("100")));
                } else {
                    disCount = od.getCoupon().getDisPrice();
                }
            }

            total = total.subtract(disCount);
        }

        return total;
    }

    public List<ReportProductInventoryResponse> getTop5Products() {
        List<Object[]> results = productJPA.getTopProductsWithHighestStock();
        return results.stream()
                .map(row -> new ReportProductInventoryResponse((String) row[0], ((Number) row[1]).intValue()))
                .collect(Collectors.toList());
    }

    public List<WishlistProductRes> getTopSanPhamYeuThichNhieuNhat(){
        List<WishlistProductRes> res = new ArrayList<>();
        List<Product> products = productJPA.findAll();
        int top = 5;

        for (Product p : products) {
            WishlistProductRes wishlistProductRes = new WishlistProductRes();

            wishlistProductRes.setIdProduct(p.getProductId());
            wishlistProductRes.setProductName(p.getProductName());
            wishlistProductRes.setLike(p.getWishlists().size());
            wishlistProductRes.setImage(uploadService.getUrlImage(p.getProductImg()));

            res.add(wishlistProductRes);
        }

        res.sort((a, b) -> b.getLike().compareTo(a.getLike()));
        res = res.subList(0, Math.min(top, res.size()));

        return res;
    }

}
