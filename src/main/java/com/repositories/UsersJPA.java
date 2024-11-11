package com.repositories;

import com.entities.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsersJPA extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneAndProvider(String phone, String provider);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailAndProvider(String email, String provider);

    Optional<User> findByUsernameAndProvider(String username, String provider);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);
    

}
