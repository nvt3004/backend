package com.models;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDTO {
	
	private Integer receiptId;
    private LocalDateTime receiptDate;
	private String supplierName;
    private List<ReceiptDetailDTO> receiptDetailDTO;
    private String username;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class ReceiptDetailDTO {
    	private Integer receiptDetailId;
    	private Integer quantity;
    	private ProductVersionDTO productVersionDTO;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class ProductVersionDTO {
    	private Integer productVersionId;
    	private String versionImage;
    	private String productVersionName;
    }
}
