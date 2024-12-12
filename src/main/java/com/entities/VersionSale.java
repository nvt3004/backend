package com.entities;


import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name="version_sales")
public class VersionSale implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @ManyToOne
    @JoinColumn(name="version_id")
    private  ProductVersion productVersion;

    public VersionSale() {
    }

    public VersionSale(int id, Sale sale, ProductVersion productVersion) {
        this.id = id;
        this.sale = sale;
        this.productVersion = productVersion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }
}
