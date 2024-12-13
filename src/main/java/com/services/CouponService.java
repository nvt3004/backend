package com.services;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.entities.Coupon;
import com.errors.FieldErrorDTO;
import com.errors.InvalidException;
import com.models.CouponCreateDTO;
import com.models.CouponDTO;
import com.repositories.CouponJPA;
import com.repositories.OrderJPA;
import com.repositories.UserCouponJPA;
import com.utils.RandomStringUtils;

@Service
public class CouponService {

	@Autowired
	private CouponJPA couponJpa;

	@Autowired
	private OrderJPA orderJpa;

	@Autowired
	private UserCouponJPA userCouponJpa;

	public boolean isCouponCodeExists(String couponCode) {
		return couponJpa.existsByCouponCode(couponCode);
	}
	public List<FieldErrorDTO> validateCoupon(CouponCreateDTO couponCreateDTO, BindingResult errors) {
	    List<FieldErrorDTO> fieldErrors = new ArrayList<>();

	    if (errors.hasErrors()) {
	        for (ObjectError error : errors.getAllErrors()) {
	            String field = ((FieldError) error).getField();
	            String errorMessage = error.getDefaultMessage();
	            fieldErrors.add(new FieldErrorDTO(field, errorMessage));
	        }
	    }

	    if (couponCreateDTO.getStartDate() == null) {
	        fieldErrors.add(new FieldErrorDTO("startDate", "Ngày bắt đầu không được để trống."));
	    }
	    if (couponCreateDTO.getEndDate() == null) {
	        fieldErrors.add(new FieldErrorDTO("endDate", "Ngày kết thúc không được để trống."));
	    }

	    if (couponCreateDTO.getStartDate() != null && couponCreateDTO.getEndDate() != null) {
	        if (!couponCreateDTO.isDatesValid()) {
	            fieldErrors.add(new FieldErrorDTO(
	                "startDate", 
	                "Ngày bắt đầu phải trước ngày kết thúc và không vượt quá 3 tháng."
	            ));
	        }
	    }

	    if (!couponCreateDTO.isDiscountValid()) {
	        fieldErrors.add(new FieldErrorDTO(
	            "discount", 
	            "Chỉ được áp dụng một loại giảm giá: phần trăm hoặc số tiền."
	        ));
	    }

	    if (!couponCreateDTO.isDisPercentValid() && !couponCreateDTO.isDisPriceValid()) {
	        fieldErrors.add(new FieldErrorDTO(
	            "discount", 
	            "Phải cung cấp ít nhất một loại giảm giá: phần trăm hoặc số tiền."
	        ));
	    }

	    return fieldErrors;
	}
	
	public Coupon saveCoupon(CouponCreateDTO couponCreateDTO) {
		Coupon coupon = new Coupon();
		String couponCode;

		do {
			couponCode = RandomStringUtils.randomAlphanumeric(10);
		} while (couponJpa.existsByCouponCode(couponCode));
		coupon.setCouponCode(couponCode);

		coupon.setDisPercent(couponCreateDTO.getDisPercent());
		coupon.setDisPrice(couponCreateDTO.getDisPrice());
		coupon.setDescription(couponCreateDTO.getDescription());

		LocalDateTime startDate = couponCreateDTO.getStartDate().plusHours(7);
		LocalDateTime endDate = couponCreateDTO.getEndDate().plusHours(7);

		coupon.setStartDate(startDate);
		coupon.setEndDate(endDate);

		coupon.setQuantity(couponCreateDTO.getQuantity());
		coupon.setStatus(true);

		return couponJpa.save(coupon);
	}

	public Coupon updateCoupon(Integer id, CouponCreateDTO couponCreateDTO) throws InvalidException {

		Coupon existingCoupon = couponJpa.findById(id)
				.orElseThrow(() -> new InvalidException("Không tìm thấy mã giảm giá với ID " + id));

		boolean isCouponApplied = orderJpa.existsByCouponId(id);
		boolean isCouponUsedByUser = userCouponJpa.existsByCouponId(id);

		if (isCouponApplied || isCouponUsedByUser) {
			throw new InvalidException(
					"Không thể cập nhật mã giảm giá này vì đã được người dùng lấy về hoặc áp dụng vào đơn hàng.");
		}

		existingCoupon.setDisPercent(couponCreateDTO.getDisPercent());
		existingCoupon.setDisPrice(couponCreateDTO.getDisPrice());
		existingCoupon.setDescription(couponCreateDTO.getDescription());
		existingCoupon.setStartDate(couponCreateDTO.getStartDate());
		existingCoupon.setEndDate(couponCreateDTO.getEndDate());

		if (!isCouponApplied) {
			existingCoupon.setQuantity(couponCreateDTO.getQuantity());
		}

		return couponJpa.save(existingCoupon);
	}

	public void deleteCoupon(Integer id) {
		if (!couponJpa.existsById(id)) {
			throw new InvalidException("Không tìm thấy mã giảm giá với ID " + id);
		}

		if (userCouponJpa.existsByCouponId(id)) {
			throw new InvalidException("Không thể xóa mã giảm giá này vì đã được người dùng sử dụng.");
		}

		if (orderJpa.existsByCouponId(id)) {
			throw new InvalidException("Không thể xóa mã giảm giá này vì đã được liên kết với một đơn hàng.");
		}

		couponJpa.deleteById(id);
	}

	public Page<CouponDTO> getCoupons(LocalDateTime startDate, LocalDateTime endDate, String discountType,
			Pageable pageable) throws ParseException {

		Page<Coupon> couponPage = couponJpa.findActiveCoupons(startDate, endDate, discountType, pageable);

		List<CouponDTO> couponDTOs = new ArrayList<>();
		for (Coupon coupon : couponPage.getContent()) {
			CouponDTO dto = new CouponDTO();
			dto.setId(coupon.getCouponId());
			dto.setCode(coupon.getCouponCode());
			dto.setDescription(coupon.getDescription());
			dto.setStartDate(coupon.getStartDate());
			dto.setEndDate(coupon.getEndDate());
			dto.setDisPercent(coupon.getDisPercent());
			dto.setDisPrice(coupon.getDisPrice());
			dto.setQuantity(coupon.getQuantity());
			couponDTOs.add(dto);
		}

		return new PageImpl<>(couponDTOs, pageable, couponPage.getTotalElements());
	}

	public Coupon getCouponByCode(String code) {
		if (code == null) {
			return null;
		}

		return couponJpa.getCouponByCode(code);
	}

	public List<CouponDTO> getCouponsHome(Integer userId) {

		List<Coupon> couponPage = couponJpa.getCouponHomeByUser(userId, LocalDateTime.now());

		List<CouponDTO> couponDTOs = new ArrayList<>();
		for (Coupon coupon : couponPage) {
			CouponDTO dto = new CouponDTO();
			dto.setId(coupon.getCouponId());
			dto.setCode(coupon.getCouponCode());
			dto.setDescription(coupon.getDescription());

			dto.setStartDate(coupon.getStartDate().minusHours(7));
			dto.setEndDate(coupon.getEndDate().minusHours(7));

			dto.setDisPercent(coupon.getDisPercent());
			dto.setDisPrice(coupon.getDisPrice());
			dto.setQuantity(coupon.getQuantity());
			couponDTOs.add(dto);
		}

		return couponDTOs;
	}

}
