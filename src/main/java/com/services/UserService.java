package com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.User;
import com.repositories.UserJPA;

@Service
public class UserService {
	@Autowired
	UserJPA userJPA;

	public User getUserByUsername(String username) {
		return userJPA.getUserByUsername(username);
	}

	public User createUser(User user) {		
		return userJPA.save(user);
	}
}
