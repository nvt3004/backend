package com.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptInfoDTO {
	
	private Integer receiptId;
    private LocalDateTime receiptDate;
	private String supplierName;
    private String username;
    
}
