package com.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Address;
import com.entities.User;
import com.errors.ResponseAPI;
import com.models.AddressDTO;
import com.repositories.AddressJPA;
import com.repositories.UserJPA;
import com.services.AddressService;
import com.services.AuthService;
import com.services.JWTService;
import com.services.UserService;

@RestController
@RequestMapping("api/user/address")
public class AddressesController {

	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;

	@Autowired
	UserService userService;

	@Autowired
	AddressService addressService;

	@Autowired
	AddressJPA addressJPA;

	@Autowired
	UserJPA userJPA;

	@GetMapping("/get-all")
	public ResponseEntity<ResponseAPI<List<AddressDTO>>> addAddress(
			@RequestHeader("Authorization") Optional<String> authHeader) {
		ResponseAPI<List<AddressDTO>> response = new ResponseAPI<>();
		response.setData(null);
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		List<AddressDTO> addresses = addressService.getAllAddressByUser(user.getUserId());

		response.setCode(200);
		response.setData(addresses);
		response.setMessage("Success");

		return ResponseEntity.ok(response);
	}

	@PostMapping("/add")
	public ResponseEntity<ResponseAPI<Boolean>> addAddress(@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestBody AddressDTO addressModel) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);
		String token = authService.readTokenFromHeader(authHeader);
		System.out.println(token);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		if (addressModel.getProvince().isBlank() || addressModel.getProvince().isEmpty()
				|| addressModel.getProvince() == null) {
			response.setCode(422);
			response.setMessage("Province invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (addressModel.getDistrict().isBlank() || addressModel.getDistrict().isEmpty()
				|| addressModel.getDistrict() == null) {

			response.setCode(422);
			response.setMessage("District invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (addressModel.getWard().isBlank() || addressModel.getWard().isEmpty() || addressModel.getWard() == null) {
			response.setCode(422);
			response.setMessage("Ward id invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (addressModel.getDetailAddress().isBlank() || addressModel.getDetailAddress().isEmpty()
				|| addressModel.getDetailAddress() == null) {

			response.setCode(422);
			response.setMessage("Detail address invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		addressService.createAddress(addressModel, user);

		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");

		return ResponseEntity.ok(response);
	}

	@PutMapping("/update")
	public ResponseEntity<ResponseAPI<Boolean>> updateAddress(
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestBody AddressDTO addressModel) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(false);
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		if (addressModel.getAddressId() == null) {
			response.setCode(400);
			response.setMessage("Address id not null");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (addressModel.getProvince().isBlank() || addressModel.getProvince().isEmpty()
				|| addressModel.getProvince() == null) {
			response.setCode(422);
			response.setMessage("Province invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (addressModel.getDistrict().isBlank() || addressModel.getDistrict().isEmpty()
				|| addressModel.getDistrict() == null) {

			response.setCode(422);
			response.setMessage("District invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (addressModel.getWard().isBlank() || addressModel.getWard().isEmpty() || addressModel.getWard() == null) {
			response.setCode(422);
			response.setMessage("Ward id invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (addressModel.getDetailAddress().isBlank() || addressModel.getDetailAddress().isEmpty()
				|| addressModel.getDetailAddress() == null) {

			response.setCode(422);
			response.setMessage("Detail address invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (addressModel.getDetailAddress().isBlank() || addressModel.getDetailAddress().isEmpty()
				|| addressModel.getDetailAddress() == null) {

			response.setCode(422);
			response.setMessage("Detail address invalid format");
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		Address address = addressService.getArressByUser(user.getUserId(), addressModel.getAddressId());

		if (address == null) {
			response.setCode(404);
			response.setMessage("Address not found!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		addressService.updateAddress(addressModel, user);

		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/remove/{id}")
	public ResponseEntity<ResponseAPI<Boolean>> removeAddress(
			@RequestHeader("Authorization") Optional<String> authHeader, @PathVariable("id") int addressId) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setData(null);
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		Address address = addressService.getArressByUser(user.getUserId(), addressId);
		if (address == null) {
			response.setCode(404);
			response.setMessage(String.format("Address id %d not found!", addressId));
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		user.removeAddress(address);

		userJPA.save(user);

		response.setCode(200);
		response.setData(true);
		response.setMessage("Success");

		return ResponseEntity.ok(response);
	}
}
