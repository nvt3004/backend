package com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Category;
import com.repositories.CategoryJPA;

@Service
public class CategoryService {
	@Autowired
	CategoryJPA categoryJPA;
	
	public Category getCategoryById(int id) {
		return categoryJPA.findById(id).orElse(null);
	}
}
