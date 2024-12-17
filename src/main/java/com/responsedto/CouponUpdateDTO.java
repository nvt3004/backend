package com.responsedto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CouponUpdateDTO {
	@DecimalMin(value = "5.0", message = "Phần trăm giảm giá phải ít nhất là 5%.")
	@DecimalMax(value = "50.0", message = "Phần trăm giảm giá không được vượt quá 50%.")
	private BigDecimal disPercent;

	@DecimalMin(value = "5000.0", message = "Giá trị giảm giá phải ít nhất là 5000.")
	@DecimalMax(value = "100000.0", message = "Giá trị giảm giá không được vượt quá 100,000.")
	private BigDecimal disPrice;

	@Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự.")
	private String description;

	@FutureOrPresent(message = "Ngày bắt đầu phải là hôm nay hoặc trong tương lai.")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startDate;

	@Future(message = "Ngày kết thúc phải là trong tương lai.")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endDate;

	@NotNull(message = "Số lượng không được để trống.")
	@Min(value = 1, message = "Số lượng phải ít nhất là 1.")
	private Integer quantity;

	public boolean isDiscountValid() {
		if (disPercent != null && disPrice != null) {
			return false;
		}
		return true;
	}

	public boolean isDisPercentValid() {
		return disPercent != null && !disPercent.toString().trim().isEmpty();
	}

	public boolean isDisPriceValid() {
		return disPrice != null && !disPrice.toString().trim().isEmpty();
	}

	private static final long MAX_DURATION_DAYS = 90;

	public boolean isDatesValid() {
		return startDate != null && endDate != null && startDate.isBefore(endDate)
				&& ChronoUnit.DAYS.between(startDate, endDate) <= MAX_DURATION_DAYS;
	}
	
}
