package com.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.User;

public interface UserJPA extends JpaRepository<User, Integer>{

	@Query("SELECT o FROM User o WHERE o.username =:username")
	public User getUserByUsername(@Param("username") String username);
	
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneAndProvider(String phone, String provider);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailAndProvider(String email, String provider);

    Optional<User> findByUsernameAndProvider(String username, String provider);
}
