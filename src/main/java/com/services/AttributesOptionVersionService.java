package com.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.AttributeOptionsVersion;
import com.repositories.AttributeOptionsVersionJPA;


@Service
public class AttributesOptionVersionService {

	@Autowired
	private AttributeOptionsVersionJPA attributeOptionsVersionJpa; 

	public Optional<AttributeOptionsVersion> findById(Integer id) {
		return attributeOptionsVersionJpa.findById(id); 
	}
	
	public AttributeOptionsVersion getById(int id) {
		return attributeOptionsVersionJpa.findById(id).orElse(null);
	}
	
    public AttributeOptionsVersion saveAttributeOptionsVersion(AttributeOptionsVersion attributeOptionsVersion) {
        return attributeOptionsVersionJpa.save(attributeOptionsVersion);
    }

}
