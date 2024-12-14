package com.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Attribute;
import com.entities.AttributeOption;
import com.entities.User;
import com.errors.ResponseAPI;
import com.models.AttributeOptionDTO;
import com.models.OptionModel;
import com.services.AttributeService;
import com.services.AuthService;
import com.services.JWTService;
import com.services.UserService;

@RestController
@RequestMapping("api/staff/attribute/option")
public class AttributeOptionController {
	@Autowired
	AttributeService attributeService;

	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;

	@Autowired
	UserService userService;

	@PostMapping("/add")
	public ResponseEntity<ResponseAPI<Boolean>> addAttribute(
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestBody AttributeOptionDTO attOptionDTO) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Định dạng mã token không hợp lệ");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		if (attOptionDTO.getAttibuteName() == null || attOptionDTO.getAttibuteName().isEmpty()
				|| attOptionDTO.getAttibuteName().isBlank()) {
			response.setCode(422);
			response.setMessage("Tên thuộc tính không được để trống");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (attOptionDTO.getOptions() == null || attOptionDTO.getOptions().isEmpty()) {
			response.setCode(999);
			response.setMessage("Giá trị thuộc tính không được để trống");

			return ResponseEntity.status(999).body(response);
		}

		for (String op : attOptionDTO.getOptions()) {
			if (op == null || op.isBlank() || op.isEmpty()) {
				response.setCode(999);
				response.setMessage("Giá trị thuộc tính không được để trống");

				return ResponseEntity.status(999).body(response);
			}
		}

		for(int i = 0; i< attOptionDTO.getOptions().size(); i++){
			String op1 = attOptionDTO.getOptions().get(i);

			for(int j = 0; j< attOptionDTO.getOptions().size(); j++){
				if(i == j) continue;

				String op2 = attOptionDTO.getOptions().get(j);

				if(op1.trim().equalsIgnoreCase(op2.trim())){
					response.setCode(999);
					response.setMessage("Giá trị thuộc tính không được trùng trùng");

					return ResponseEntity.status(999).body(response);
				}
			}
		}

		Attribute attributeEntity = new Attribute();
		attributeEntity.setAttributeName(attOptionDTO.getAttibuteName());

		Attribute attributeSaved = attributeService.saveAttribute(attributeEntity);
		attributeService.createAttriubteOption(attributeSaved, attOptionDTO.getOptions());

		response.setCode(200);
		response.setMessage("Success");
		response.setData(true);

		return ResponseEntity.ok(response);
	}
	
	
	@PostMapping("/add-option")
	public ResponseEntity<ResponseAPI<Boolean>> addOption(
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestBody OptionModel optionModel) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Định dạng mã token không hợp lệ");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
		
		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		if(optionModel.getAttributeId() == null) {
			response.setCode(999);
			response.setMessage("Thuộc tính không hợp lệ");

			return ResponseEntity.status(999).body(response);
		}
		
		Attribute attribute = attributeService.getAttributeById(optionModel.getAttributeId());
		
		if(attribute == null) {
			response.setCode(999);
			response.setMessage("Thuộc tính không hợp lệ");
			return ResponseEntity.status(999).body(response);
		}
		
		if(optionModel.getOptionName() == null || optionModel.getOptionName().isEmpty()) {
			response.setCode(999);
			response.setMessage("Giá trị thuộc tính không hợp lệ");

			return ResponseEntity.status(999).body(response);
		}

		for(AttributeOption op: attribute.getAttributeOptions()){
			for(String opvl : optionModel.getOptionName()){
				if(op.getAttributeValue().trim().equalsIgnoreCase(opvl.trim())){
					response.setMessage("Giá trị thuộc tính "+ opvl.trim() +" đã tồn tại vui lòng chọn giá trị khác");

					return ResponseEntity.status(999).body(response);
				}
			}
		}
		
		attributeService.createOption(optionModel);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(true);

		return ResponseEntity.ok(response);
	}
}
