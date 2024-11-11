package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entities.Address;
import com.entities.Advertisement;

public interface AdversitementJPA extends JpaRepository<Advertisement, Integer>{

}
