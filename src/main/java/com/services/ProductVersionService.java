package com.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.ProductVersion;
import com.repositories.ProductVersionJPA;

@Service
public class ProductVersionService {
	@Autowired
	private ProductVersionJPA productVersionJpa;

    public Optional<ProductVersion> getProductVersionByAttributes(Integer productVersionId, Integer colorId, Integer sizeId) {
        return productVersionJpa.findByProductAttributes(productVersionId, colorId, sizeId);
    }

	public ProductVersion getProductVersionById(int id) {
		return productVersionJpa.findById(id).orElse(null);
	}

	public boolean isValidProductVersion(ProductVersion version) {
		if (version != null && version.getProduct().isStatus()) {
			return true;
		}

		return false;
	}

	public boolean isValidProductVersion(int versionId) {
		ProductVersion version = productVersionJpa.findById(versionId).get();

		if (version != null && version.getProduct().isStatus()) {
			return true;
		}

		return false;
	}

	public ProductVersion saveProductVersion(ProductVersion productVersion) {
		return productVersionJpa.save(productVersion);
	}
	
	public List<ProductVersion> getAllProductVersions() {
		return productVersionJpa.findAll();
	}

	public ProductVersion getProductVersionByID(Integer id) {
		return productVersionJpa.findById(id).orElse(null);
	}

	public Optional<ProductVersion> getWarehousById(int id) {
		return productVersionJpa.findById(id);
	}
	public ProductVersion updateProdVerSion(ProductVersion productVersion) {
		return productVersionJpa.save(productVersion);
	}
}

