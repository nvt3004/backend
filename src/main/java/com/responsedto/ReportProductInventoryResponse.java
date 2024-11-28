package com.responsedto;

public class ReportProductInventoryResponse {
    private String versionName;
    private Integer quantity;
    
    

    public ReportProductInventoryResponse() {
	}

	public ReportProductInventoryResponse(String versionName, Integer quantity) {
        this.versionName = versionName;
        this.quantity = quantity;
    }

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
    
    
}
