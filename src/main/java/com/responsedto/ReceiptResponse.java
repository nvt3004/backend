package com.responsedto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptResponse {
	private Integer receiptId;
    private LocalDateTime receiptDate;
	private String supplierName;
	private String supplierEmail;
	private String supplierPhone;
	private String supplierAddress;
    private String username;
    private String fullname;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private List<ReceiptDetailResponse> detais;
}
