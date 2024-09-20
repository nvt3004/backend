package com.repositories;

import com.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleJPA extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}
