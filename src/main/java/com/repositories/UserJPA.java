package com.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.User;

public interface UserJPA extends JpaRepository<User, Integer> {

	@Query("SELECT o FROM User o WHERE o.username =:username")
	public User getUserByUsername(@Param("username") String username);

	Optional<User> findByEmail(String email);
	
	Optional<User> findByPhone(String phone);

	Optional<User> findByPhoneAndProvider(String phone, String provider);

	Optional<User> findByUsername(String username);

	Optional<User> findByEmailAndProvider(String email, String provider);

	Optional<User> findByUsernameAndProvider(String username, String provider);

	@Query("SELECT o FROM User o " + "WHERE o.username LIKE:keyword " + "OR o.fullName LIKE:keyword ")
	Page<User> getAllUserByKeyword(@Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT o FROM User o " + "WHERE " + "o.status =:status")
	Page<User> getAllUserByKeywordAndStatus(@Param("status") byte status, Pageable pageable);
}
