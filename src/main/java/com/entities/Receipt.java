package com.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
@Table(name = "receipts")
public class Receipt implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Column(name="supplier_name")
	private String supplierName;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "receipt_id")
	private int receiptId;

	@Column(name = "receipt_date", nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime receiptDate;

	@ManyToOne
	@JoinColumn(name = "supplier_id")
	@JsonBackReference("supplier-receipt")
	private Supplier supplier;
	
	@ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-receipt")
    private User user;

	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
	@JsonManagedReference("receipt-receipt_detail")
	private List<ReceiptDetail> receiptDetails;

	
	public Receipt() {
	}
	

}
