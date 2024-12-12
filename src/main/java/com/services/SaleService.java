package com.services;

import com.entities.ProductVersion;
import com.entities.Sale;
import com.entities.VersionSale;
import com.models.SaleDTO;
import com.models.VersionSaleDTO;
import com.repositories.ProductVersionJPA;
import com.repositories.SaleJPA;
import com.repositories.VersionSaleJPA;
import com.responsedto.ProductResponse;
import com.responsedto.SaleResponse;
import com.responsedto.Version;
import com.utils.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {
    @Autowired
    SaleJPA saleJPA;

    @Autowired
    VersionSaleJPA versionSaleJPA;

    @Autowired
    VersionService versionService;

    @Autowired
    ProductVersionJPA productVersionJPA;

    @Autowired
    UploadService uploadService;

    public PageImpl<SaleResponse> getAllSales(int page, int size, String keyword, LocalDateTime startDate, LocalDateTime endDate, int status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Sale> sales = null;
        List<SaleResponse> saleResponses = new ArrayList<>();
        LocalDateTime dateNow = LocalDateTime.now().withSecond(0).withNano(0).plusHours(7);

        if (status == 2) { // Trang thai chua bat dau
            sales = saleJPA.getAllSalesNotStarted(keyword, startDate, endDate, dateNow, pageable);
            saleResponses = sales.stream().map(this::crateSaleResponse).toList();
        } else if (status == 1) {// Trang thai dang dien ra
            sales = saleJPA.getAllSalesInProgess(keyword, startDate, endDate, dateNow, true, pageable);
            saleResponses = sales.stream().map(this::crateSaleResponse).toList();
        } else if (status == 3) {// Trang thai dang khoa
            sales = saleJPA.getAllSalesInBlock(keyword, startDate, endDate, dateNow, false, pageable);
            saleResponses = sales.stream().map(this::crateSaleResponse).toList();
        } else if (status == 0) { // Trang thai da ket thuc
            sales = saleJPA.getAllSalesIsOver(keyword, startDate, endDate, dateNow, pageable);
            saleResponses = sales.stream().map(this::crateSaleResponse).toList();
        } else {//Lay tat ca
            sales = saleJPA.getAllSales(keyword, startDate, endDate, pageable);
            saleResponses = sales.stream().map(this::crateSaleResponse).toList();
        }

        PageImpl<SaleResponse> result = new PageImpl<SaleResponse>(saleResponses, pageable,
                sales.getTotalElements());

        return result;
    }

    public Sale getSaleById(Integer id) {
        LocalDateTime dateNow = LocalDateTime.now().withSecond(0).withNano(0).plusHours(7);

        return saleJPA.getSaleById(id, dateNow);
    }

    public void addSale(SaleDTO saleDTO) {
        saleDTO.setStatus(false);
        saleDTO.setId(-1);

        Sale saleSaved = createSale(saleDTO);
        createVersionSale(saleSaved, saleDTO.getVersionIds());
    }

    public void updateSale(SaleDTO saleDTO) {
        Sale saleSaved = createSale(saleDTO);

        List<Integer> versionAddIds = new ArrayList<>();

        for (VersionSaleDTO vsSaleDTO : saleDTO.getVersionSaleDTOS()) {
            if (vsSaleDTO.getId() == null) {
                versionAddIds.add(vsSaleDTO.getIdVersion());
            }
        }


        for (VersionSale vsSale : saleJPA.findByIdWithVersionSales(saleDTO.getId())) {
            boolean check = true;

            for (VersionSaleDTO vsSaleDTO : saleDTO.getVersionSaleDTOS()) {
                if (vsSaleDTO.getId() != null && vsSale.getId() == vsSaleDTO.getId()) {
                    check = false;
                }
            }

            if (check) {
                versionSaleJPA.delete(vsSale);
            }
        }

        createVersionSale(saleSaved, versionAddIds);
    }

    public Sale updateStatusSale(Sale sale) {
        return saleJPA.save(sale);
    }

    public boolean deleteSale(Sale sale) {
        try {
            versionSaleJPA.deleteAll(sale.getVersionSales());
            saleJPA.delete(sale);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Sale createSale(SaleDTO saleDTO) {
        Sale sale = new Sale();

        sale.setId(saleDTO.getId());
        sale.setSaleName(saleDTO.getSaleName());
        sale.setDisPercent(saleDTO.getPercent());

        // Chuyển đổi từ LocalDateTime sang múi giờ Việt Nam (UTC+7)
        LocalDateTime startDate = saleDTO.getStartDate();
        LocalDateTime endDate = saleDTO.getEndDate();

        // Tạo một ZonedDateTime từ LocalDateTime
        ZonedDateTime startDateInVietnamTime = startDate.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneOffset.ofHours(7));
        ZonedDateTime endDateInVietnamTime = endDate.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneOffset.ofHours(7));

        // Chuyển lại về LocalDateTime sau khi đổi múi giờ
        sale.setStartDate(startDateInVietnamTime.toLocalDateTime());
        sale.setEndDate(endDateInVietnamTime.toLocalDateTime());

        sale.setStatus(saleDTO.isStatus());

        return saleJPA.save(sale);
    }

    private void createVersionSale(Sale sale, List<Integer> versionIds) {
        if (versionIds.isEmpty()) {
            return;
        }

        for (Integer versionId : versionIds) {
            ProductVersion version = new ProductVersion();
            VersionSale versionSale = new VersionSale();
            versionSale.setSale(sale);

            version.setId(versionId);
            versionSale.setProductVersion(version);

            versionSaleJPA.save(versionSale);
        }
    }

    private SaleResponse crateSaleResponse(Sale sale) {
        SaleResponse saleResponse = new SaleResponse();
        List<Version> versionsResponses = getVersionResponse(sale);
        LocalDateTime dateNow = LocalDateTime.now().withSecond(0).withNano(0);

        saleResponse.setId(sale.getId());
        saleResponse.setSaleName(sale.getSaleName());
        saleResponse.setPercent(sale.getDisPercent());

        // Trừ 7 tiếng từ startDate và endDate
        LocalDateTime startDate = sale.getStartDate();
        LocalDateTime endDate = sale.getEndDate();

        // Trừ 7 giờ từ startDate và endDate
        if (startDate != null) {
            startDate = startDate.minusHours(7);
        }
        if (endDate != null) {
            endDate = endDate.minusHours(7);
        }

        saleResponse.setStartDate(startDate);
        saleResponse.setEndDate(endDate);
        saleResponse.setStatus(sale.getStatus());
        saleResponse.setVersions(versionsResponses);

        if (dateNow.isBefore(startDate)) { // Chưa bắt đầu
            saleResponse.setActive(2);
        } else if (!dateNow.isBefore(startDate) && !dateNow.isAfter(endDate)) { // Đang diễn ra
            saleResponse.setActive(sale.getStatus() ? 1 : 3); // 1: đang diễn ra, 3: đang khóa
        } else { // Đã kết thúc
            saleResponse.setActive(0);
        }

        return saleResponse;
    }

    private List<Version> getVersionResponse(Sale sale) {
        List<Version> versions = new ArrayList<>();

        for (VersionSale versionSale : sale.getVersionSales()) {
            ProductVersion versionEntity = versionSale.getProductVersion();
            Version versionResponse = new Version();

            versionResponse.setId(versionEntity.getId());
            versionResponse.setVersionName(versionEntity.getVersionName());
            versionResponse.setQuantity(versionService.getTotalStockQuantityVersion(versionEntity.getId()));
            versionResponse.setPrice(versionEntity.getRetailPrice());
            versionResponse.setActive(versionEntity.isStatus() && versionEntity.getProduct().isStatus());
            versionResponse.setImage(uploadService.getUrlImage(versionEntity.getImage().getImageUrl()));

            versions.add(versionResponse);
        }

        return versions;
    }
}
