package com.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

/**
 * The persistent class for the product_categories database table.
 *
 */
@Entity
@Table(name="sales")
public class Sale implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name = "sale_name")
    private String saleName;

    @Column(name = "dis_percent")
    private BigDecimal disPercent;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "status")
    private Boolean status;

    @OneToMany(mappedBy = "sale")
    private List<VersionSale> versionSales;

    public Sale() {
    }

    public Sale(int id, List<VersionSale> versionSales, Boolean status, LocalDateTime endDate, LocalDateTime startDate, BigDecimal disPercent, String saleName) {
        this.id = id;
        this.versionSales = versionSales;
        this.status = status;
        this.endDate = endDate;
        this.startDate = startDate;
        this.disPercent = disPercent;
        this.saleName = saleName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSaleName() {
        return saleName;
    }

    public void setSaleName(String saleName) {
        this.saleName = saleName;
    }

    public BigDecimal getDisPercent() {
        return disPercent;
    }

    public void setDisPercent(BigDecimal disPercent) {
        this.disPercent = disPercent;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public List<VersionSale> getVersionSales() {
        return versionSales;
    }

    public void setVersionSales(List<VersionSale> versionSales) {
        this.versionSales = versionSales;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}