package com.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.entities.User;
import com.repositories.UserJPA;

@Service
public class AuthDetailsService implements UserDetailsService {

    @Autowired
    private UserJPA userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByUsername(username);
        return user.orElseThrow(() -> new UsernameNotFoundException("Email not found with email: " + username));
    }

}
