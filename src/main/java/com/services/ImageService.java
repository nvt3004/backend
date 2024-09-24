package com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Image;
import com.repositories.ImageJPA;

@Service
public class ImageService {

	@Autowired
	ImageJPA imageJPA;

	public Image createImage(Image image) {
		return imageJPA.save(image);
	}

	public Image getImageByUser(int userId) {
		return imageJPA.getImageByUser(userId);
	}
}
