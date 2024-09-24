package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entities.Address;

public interface AddressJPA extends JpaRepository<Address, Integer>{

}
