package com.configs;

import com.entities.User;
import com.repositories.UsersJPA;
import com.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UsersJPA usersJPA;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        int userId = getUserIdFromAuthentication(authentication);
        if (userId == -1) {
            throw new AccessDeniedException("Authentication failed");
        }

        List<String> userPermissions = permissionService.getPermissionsByUserId(userId);
        if (!userPermissions.contains(permission.toString())) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện thao tác này!");
        }

        return true;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }

    private int getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                Optional<User> optionalUser = usersJPA.findByUsername(userDetails.getUsername());
                if (optionalUser.isPresent()) {
                    return optionalUser.get().getUserId();
                } else {
                    throw new UsernameNotFoundException("User not found with username: " + userDetails.getUsername());
                }
            }
        }
        return -1;
    }
}
