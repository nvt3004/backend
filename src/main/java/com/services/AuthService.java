package com.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

	public static String readTokenFromHeader(Optional<String> auth) {
		return auth.isPresent() && auth.get().startsWith("Bearer ") ? auth.get().substring(7) : null;
	}
}
