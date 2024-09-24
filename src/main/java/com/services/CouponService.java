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

		if (!couponCreateDTO.isDatesValid()) {
			fieldErrors.add(new FieldErrorDTO("startDate", "Start date must be before the end date."));
		}
		if (!couponCreateDTO.isDiscountValid()) {
			fieldErrors
					.add(new FieldErrorDTO("discount", "Only one discount type (percentage or price) can be applied."));
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
		coupon.setStartDate(couponCreateDTO.getStartDate());
		coupon.setEndDate(couponCreateDTO.getEndDate());
		coupon.setQuantity(couponCreateDTO.getQuantity());

		return couponJpa.save(coupon);
	}

	public Coupon updateCoupon(Integer id, CouponCreateDTO couponCreateDTO) throws InvalidException {

		Coupon existingCoupon = couponJpa.findById(id)
				.orElseThrow(() -> new InvalidException("Coupon with ID " + id + " not found"));

		boolean isCouponApplied = orderJpa.existsByCouponId(id);

	    if (isCouponApplied) {
	        if (!(existingCoupon.getQuantity() == (couponCreateDTO.getQuantity()))) {
	            throw new InvalidException("Cannot update quantity because the coupon has already been applied to an order.");
	        }
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
			throw new InvalidException("Coupon with ID " + id + " does not exist.");
		}

		if (userCouponJpa.existsByCouponId(id)) {
			throw new InvalidException("This coupon cannot be deleted because it has been used by a user.");
		}

		if (orderJpa.existsByCouponId(id)) {
			throw new InvalidException("This coupon cannot be deleted because it is associated with an order.");
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
}
