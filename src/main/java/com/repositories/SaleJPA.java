package com.repositories;


import com.entities.Product;
import com.entities.Sale;
import com.entities.VersionSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleJPA extends JpaRepository<Sale, Integer> {

    //Trạng thái tất cả
    @Query("SELECT o FROM Sale o WHERE o.saleName LIKE:keyword AND o.startDate>=:startDate AND o.endDate<=:endDate")
    public Page<Sale> getAllSales(String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    //Trạng thai chưa bắt đầu
    @Query("SELECT o FROM Sale o WHERE o.saleName LIKE:keyword AND o.startDate>:now AND o.startDate>=:startDate AND o.endDate<=:endDate")
    public Page<Sale> getAllSalesNotStarted(String keyword, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime now, Pageable pageable);

    //Trạng thai đang diễn ra
    @Query("SELECT o FROM Sale o WHERE o.saleName LIKE:keyword AND o.startDate<=:now AND o.endDate >:now AND o.startDate>=:startDate AND o.endDate<=:endDate AND o.status=:status")
    public Page<Sale> getAllSalesInProgess(String keyword, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime now,boolean status, Pageable pageable);

    //Trạng thai đang diễn ra
    @Query("SELECT o FROM Sale o WHERE  o.startDate<=:now AND o.endDate >=:now AND o.status=:status")
    public List<Sale> getAllSalesInProgess( LocalDateTime now,boolean status);

    //Trạng thai đang khóa
    @Query("SELECT o FROM Sale o WHERE o.saleName LIKE:keyword AND o.startDate<=:now AND o.endDate >:now AND o.startDate>=:startDate AND o.endDate<=:endDate AND o.status=:status")
    public Page<Sale> getAllSalesInBlock(String keyword, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime now,boolean status, Pageable pageable);

    //Trạng thai đã kết thúc
    @Query("SELECT o FROM Sale o WHERE o.saleName LIKE:keyword AND o.endDate<:now AND o.startDate>=:startDate AND o.endDate<=:endDate")
    public Page<Sale> getAllSalesIsOver(String keyword, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime now, Pageable pageable);

    @Query("SELECT o FROM Sale o WHERE o.startDate<=:dateNow AND o.endDate>:dateNow AND o.id=:id")
    public Sale getSaleById(Integer id, LocalDateTime dateNow);

    @Query("SELECT s FROM VersionSale s WHERE s.sale.id =:id")
    List<VersionSale> findByIdWithVersionSales(@Param("id") int id);
}
