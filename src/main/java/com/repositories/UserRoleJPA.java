package com.repositories;

import com.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleJPA extends JpaRepository<UserRole, Integer> {
}
