package com.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entities.Permission;

public interface PermissionJPA extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByPermissionName(String permissionName);
    

}