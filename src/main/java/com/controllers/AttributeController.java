package com.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Attribute;
import com.entities.User;
import com.errors.ResponseAPI;
import com.responsedto.AttributeResponse;
import com.responsedto.OptinonResponse;
import com.services.AttributeService;
import com.services.AuthService;
import com.services.JWTService;
import com.services.UserService;

@RestController
@RequestMapping("api/staff/attribute")
public class AttributeController {
	@Autowired
	AttributeService attributeService;
	
	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;


	@Autowired
	UserService userService;
	
	
	@PostMapping("/add")
	public ResponseEntity<ResponseAPI<AttributeResponse>> addAttribute(@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam("attributeName") Optional<String> attributeName) {
		ResponseAPI<AttributeResponse> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);
		String name = attributeName.orElse(null);

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
		
		if(name == null || name.isEmpty() || name.isBlank() ) {
			response.setCode(422);
			response.setMessage("Invalid format attribute name");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}
		
		Attribute attributeEntity = new Attribute();
		attributeEntity.setAttributeName(name);
		
		Attribute attributeSaved = attributeService.saveAttribute(attributeEntity);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(new AttributeResponse(attributeSaved.getId(), attributeSaved.getAttributeName(),null));
		
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/update")
	public ResponseEntity<ResponseAPI<AttributeResponse>> updateAttribute(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam("id") Optional<Integer> attributeId,
			@RequestParam("attributeName") Optional<String> attributeName) 
	{
		ResponseAPI<AttributeResponse> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);
		Integer id = attributeId.orElse(null);
		String name = attributeName.orElse(null);

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
		
		if(id==null) {
			response.setCode(422);
			response.setMessage("Invalid format attribute id");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}
		
		Attribute attribute = attributeService.getAttributeById(id);
		
		if(attribute == null) {
			response.setCode(404);
			response.setMessage("Attribute not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		if(name == null || name.isEmpty() || name.isBlank() ) {
			response.setCode(422);
			response.setMessage("Invalid format attribute name");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}
		
		attribute.setAttributeName(name);
		Attribute attributeSaved = attributeService.saveAttribute(attribute);
		List<OptinonResponse> options = attributeService.createListOptionResponse(attributeSaved.getAttributeOptions());

		response.setCode(200);
		response.setMessage("Success");
		response.setData(new AttributeResponse(attributeSaved.getId(), attributeSaved.getAttributeName(),options));
		
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/remove/{id}")
	public ResponseEntity<ResponseAPI<Boolean>> removeAttribute(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@PathVariable("id") int id) 
	{
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
		
		Attribute attribute = attributeService.getAttributeById(id);
		
		if(attribute == null) {
			response.setCode(404);
			response.setMessage("Attribute not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		boolean isExitedInProductVersion = attributeService.isExitedInProductVersion(attribute);
		if(isExitedInProductVersion) {
			response.setCode(403);
			response.setMessage("Attribute exists in product version and cannot be deleted");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
	
		attributeService.removeAttribute(attribute);
		response.setCode(200);
		response.setMessage("Success");
		response.setData(true);
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/all")
	public ResponseEntity<ResponseAPI<List<AttributeResponse>>> getAllAttribute(@RequestHeader("Authorization") Optional<String> authHeader) {
		ResponseAPI<List<AttributeResponse>> response = new ResponseAPI<>();
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
			

		
		response.setCode(200);
		response.setMessage("Success");
		response.setData(attributeService.getAllAttribute());
		
		return ResponseEntity.ok(response);
	}
}
