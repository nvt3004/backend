package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.Image;

public interface ImageJPA extends JpaRepository<Image, Integer> {
	@Query("SELECT o FROM Image o WHERE o.user.userId=:userId")
	public Image getImageByUser(@Param("userId") int userId);

	Image findByImageUrl(String imageUrl);
}
