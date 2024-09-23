package com.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
@CrossOrigin("*")
public class CallImgController {

	@RequestMapping(value = "/getImage/{photo}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ByteArrayResource> getImage(@PathVariable String photo) {
		if (StringUtils.isEmpty(photo)) {
			return ResponseEntity.badRequest().body(new ByteArrayResource("Photo name is required".getBytes()));
		}

		Path filePath = Paths.get("static/images", photo);
		if (!Files.exists(filePath)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ByteArrayResource("Image not found".getBytes()));
		}

		try {
			String contentType = Files.probeContentType(filePath);
			if (contentType == null) {
				contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // Fallback if content type cannot be determined
			}

			byte[] imageData = Files.readAllBytes(filePath);
			ByteArrayResource resource = new ByteArrayResource(imageData);

			return ResponseEntity.ok().contentLength(imageData.length)
					.contentType(MediaType.parseMediaType(contentType)).body(resource);
		} catch (IOException e) {
			System.out.println("Error reading image file: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ByteArrayResource("Error reading image".getBytes()));
		}
	}
}
