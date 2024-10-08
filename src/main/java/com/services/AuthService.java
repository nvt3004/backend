package com.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.User;
import com.errors.InvalidException;
import com.errors.UserServiceException;

import io.jsonwebtoken.JwtException;

@Service
public class AuthService {

	public static String readTokenFromHeader(Optional<String> auth) {
		return auth.isPresent() && auth.get().startsWith("Bearer ") ? auth.get().substring(7) : null;
	}

	@Autowired
	private JWTService jwtService;

	@Autowired
	private UserService userService;

	public User validateTokenAndGetUsername(String token) throws Exception {
	    try {
	        if (jwtService.isTokenExpired(token)) {
	            throw new InvalidException("Token expired");
	        }

	        String username = jwtService.extractUsername(token);
	        if (username == null || username.isEmpty()) {
	            throw new InvalidException("Username could not be extracted from token");
	        }

	        User user = userService.getUserByUsername(username);
	        if (user == null) {
	            throw new UserServiceException("User not found with username: " + username);
	        }

	        if (user.getStatus() == 0) {
	            throw new UserServiceException("Account locked - Username: " + username);
	        }

	        return user;

	    }catch (JwtException e) {
	        throw new InvalidException("Invalid token: " + e.getMessage());
	    } catch (InvalidException | UserServiceException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new Exception("An unexpected error occurred: " + e.getMessage());
	    }
	}


}
