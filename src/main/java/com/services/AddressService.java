package com.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Address;
import com.entities.User;
import com.models.AddressDTO;
import com.repositories.AddressJPA;
import com.repositories.UserJPA;


@Service
public class AddressService {
	@Autowired
	AddressJPA addressJPA;

	@Autowired
	UserJPA userJPA;

	public Address createAddress(AddressDTO addressModel, User user) {
		Address address = setPropAddress(addressModel, user);

		return addressJPA.save(address);
	}

	public Address updateAddress(AddressDTO addressModel, User user) {
		Address address = setPropAddress(addressModel, user);
		address.setAddressId(addressModel.getAddressId());

		return addressJPA.save(address);
	}
	
	public boolean removeAddress(int addressId) {
		try {
			Address address = addressJPA.findById(addressId).orElse(null);
			
			addressJPA.delete(address);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Address getArressByUser(int userId, int addressId) {
		User user = userJPA.findById(userId).orElse(null);
		
		if(user.getAddresses() == null) {
			return null;
		}
		
		if(user.getAddresses().isEmpty()) {
			return null;
		}
		
		for(Address addr : user.getAddresses()) {
			if(addr.getAddressId() == addressId) {
				return addr;
			}
		}
		
		return null;
	}

	public List<AddressDTO> getAllAddressByUser(int userId) {
		User user = userJPA.findById(userId).orElse(null);
		
		if(user.getAddresses().isEmpty()) {
			return new ArrayList<AddressDTO>();
		}

		List<AddressDTO> addressDTos = user.getAddresses()
										   .stream()
										   .map(this::setPropAddressDTO)
										   .collect(Collectors.toList());
		return addressDTos;
	}

	
	private Address setPropAddress(AddressDTO address, User user) {
		Address addressEntity = new Address();

		addressEntity.setUser(user);
		addressEntity.setProvince(address.getProvince());
		addressEntity.setDistrict(address.getDistrict());
		addressEntity.setWard(address.getWard());
		addressEntity.setDetailAddress(address.getDetailAddress());

		return addressEntity;
	}

	private AddressDTO setPropAddressDTO(Address entity) {
		AddressDTO addressDTO = new AddressDTO();

		addressDTO.setAddressId(entity.getAddressId());
		addressDTO.setProvince(entity.getProvince());
		addressDTO.setDistrict(entity.getDistrict());
		addressDTO.setWard(entity.getWard());
		addressDTO.setDetailAddress(entity.getDetailAddress());

		return addressDTO;
	}
}
