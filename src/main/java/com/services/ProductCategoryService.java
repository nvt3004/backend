package com.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Product;
import com.entities.ProductCategory;
import com.repositories.ProductCategoryJPA;

@Service
public class ProductCategoryService {
	
	@Autowired
	ProductCategoryJPA productCategoryJPA;
	
}
