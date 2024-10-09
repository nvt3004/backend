package com.controllers;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.entities.Image;
import com.entities.User;
import com.errors.ResponseAPI;
import com.responsedto.ProfileResponse;
import com.services.AuthService;
import com.services.ImageService;
import com.services.JWTService;
import com.services.UserService;
import com.utils.UploadService;

@RestController
public class UserController {
	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;

	@Autowired
	UserService userService;

	@Autowired
	ImageService imageService;

	@Autowired
	UploadService uploadService;

	@PostMapping("api/user/update-profile")
	public ResponseEntity<ResponseAPI<ProfileResponse>> updateProfile(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam("fullname") Optional<String> fullname, @RequestParam("gender") Optional<Integer> gender,
			@RequestPart("photo") Optional<MultipartFile> photo, @RequestParam("birthday") Optional<String> birthday) {
		ResponseAPI<ProfileResponse> response = new ResponseAPI<>();
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

		if(fullname.isPresent()) {
			String fullnameTemp = fullname.get();
			if(fullnameTemp.trim().length()==0) {
				response.setCode(422);
				response.setMessage("Fullname can not blank");

				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
			}
			user.setFullName(fullname.get());
		}
		
		if(birthday.isPresent()) {
			String birthdayTemp = birthday.get();
			
			if(birthdayTemp.trim().length()==0) {
				response.setCode(422);
				response.setMessage("Fullname can not blank");

				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
			}
			
			try {
				Date date = new Date(birthdayTemp);
				user.setBirthday(date);
			} catch (Exception e) {
				response.setCode(422);
				response.setMessage("Incorrect date format");

				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
			}
		}
		
		if(gender.isPresent()) {
			if(gender.get()>3 || gender.get()<1) {
				response.setCode(404);
				response.setMessage("Gender id not found");

				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
			
			user.setGender(gender.get());
		}
		

		User userSaved = userService.createUser(user);
		Image image = imageService.getImageByUser(userSaved.getUserId());
		Image imageSaved = null;
		
		if (photo.isPresent() && image != null) {	
			uploadService.delete(image.getImageUrl(), "images");
			
			String uploadImage = uploadService.save(photo.get(), "images");
			image.setImageUrl(uploadImage);
			
			imageSaved=imageService.createImage(image);
		}else if(photo.isPresent() && image == null) {
			Image temp = new Image();
			String uploadImage = uploadService.save(photo.get(), "images");
			temp.setImageUrl(uploadImage);
			temp.setUser(userSaved);
			
			imageSaved=imageService.createImage(temp);
		}
		
		response.setCode(200);
		response.setMessage("Success");
		response.setData(new ProfileResponse(fullname.get(), gender.get(), imageSaved.getImageUrl(), birthday.get()));

		return ResponseEntity.ok(response);

	}
}
