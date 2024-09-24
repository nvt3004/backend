package com.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Attribute;
import com.entities.AttributeOption;
import com.entities.AttributeOptionsVersion;
import com.repositories.AttributeJPA;
import com.repositories.AttributeOptionJPA;
import com.responsedto.AttributeProductResponse;
import com.responsedto.AttributeResponse;
import com.responsedto.OptinonResponse;

import lombok.val;

@Service
public class AttributeService {
	@Autowired
	AttributeJPA attributeOptionJPA;

	@Autowired
	AttributeJPA attributeJPA;

	@Autowired
	AttributeOptionJPA optionJPA;

	public List<AttributeProductResponse> getAttributeProduct(int productId) {
		List<AttributeOption> attributes = attributeOptionJPA.getAttributeByProduct(productId);

		if (attributes.isEmpty()) {
			return new ArrayList<AttributeProductResponse>();
		}

		List<AttributeProductResponse> productResponses = new ArrayList<>();

		Map<String, List<String>> map = new LinkedHashMap<>();

		for (AttributeOption attribute : attributes) {
			map.put(attribute.getAttribute().getAttributeName(), new ArrayList<String>());
		}

		for (String s : map.keySet()) {
			AttributeProductResponse response = new AttributeProductResponse();
			List<String> values = map.get(s);

			for (AttributeOption attribute : attributes) {
				if (s.equals(attribute.getAttribute().getAttributeName())) {
					values.add(attribute.getAttributeValue());
					map.put(s, values);
				}
			}

			response.setKey(s);
			response.setValues(map.get(s));

			productResponses.add(response);
		}

		return productResponses;
	}

	public Attribute saveAttribute(Attribute attribute) {
		return attributeJPA.save(attribute);
	}

	public boolean removeAttribute(Attribute attribute) {
		try {
			List<AttributeOption> options = attribute.getAttributeOptions();

			if (options != null) {
				optionJPA.deleteAll(options);
			}

			attributeJPA.delete(attribute);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<AttributeResponse> getAllAttribute() {
		return attributeJPA.findAll().stream().map(atb -> {
			List<OptinonResponse> options = createListOptionResponse(atb.getAttributeOptions());

			return new AttributeResponse(atb.getId(), atb.getAttributeName(), options);
		}).toList();
	}

	public List<OptinonResponse> createListOptionResponse(List<AttributeOption> options) {
		return options.stream().map(op -> {
			return new OptinonResponse(op.getId(), op.getAttributeValue());
		}).toList();
	}

	public boolean isExitedInProductVersion(Attribute attribute) {
		List<AttributeOption> options = attribute.getAttributeOptions();

		if (options == null) {
			return false;
		}

		for (AttributeOption option : attribute.getAttributeOptions()) {
			List<AttributeOptionsVersion> optionVersions = option.getAttributeOptionsVersions();

			if (!optionVersions.isEmpty()) {
				return true;
			}
		}

		return false;
	}

	public Attribute getAttributeById(int id) {
		return attributeJPA.findById(id).orElse(null);
	}
}
