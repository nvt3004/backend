package com.responsedto;

import org.springframework.web.multipart.MultipartFile;

public class ImageRequestDTO {

	private MultipartFile image;

	// Getter và Setter
	public MultipartFile getImage() {
		return image;
	}

	public void setImage(MultipartFile image) {
		this.image = image;
	}
}
