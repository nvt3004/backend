package com.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "receipt_details")
public class ReceiptDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_detail_id")
    private int receiptDetailId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "product_version_id")
    @JsonBackReference("product_version-receipt_detail")
    private ProductVersion productVersion;

    // bi-directional many-to-one association to Receipt
    @ManyToOne
    @JoinColumn(name = "receipt_id")
    @JsonBackReference("receipt-receipt_detail")
    @JsonIgnore
    private Receipt receipt;


    public ReceiptDetail() {
    }

}
