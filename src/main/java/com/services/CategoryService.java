package com.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.entities.Category;
import com.repositories.CategoryJPA;
import com.responsedto.CategoryDTO;

@Service
public class CategoryService {
	@Autowired
	CategoryJPA categoryJPA;
	
	public Category getCategoryById(int id) {
		return categoryJPA.findById(id).orElse(null);
	}
	
	public Category addCategory(String name) {
		Category cat = new Category();
		cat.setCategoryName(name);

		return categoryJPA.save(cat);
	}

	public void removeCategory(Integer id) {
		categoryJPA.deleteById(id);
	}

	public Category updateCategory(Category cat) {
		return categoryJPA.save(cat);
	}
	
	public PageImpl<CategoryDTO> getCategorysByKeyword(int page, int size, String keyword) {
		Sort sort = Sort.by(Sort.Direction.DESC, "categoryId");
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Category> categories = categoryJPA.getAllCategoryByKeyword(keyword, pageable);

		List<CategoryDTO> catResponses = categories.getContent().stream().map(item->{
			return new CategoryDTO(item.getCategoryId(), item.getCategoryName());
		}).toList();

		PageImpl<CategoryDTO> result = new PageImpl<CategoryDTO>(catResponses, pageable,
				categories.getTotalElements());

		return result;
	}
	
}
