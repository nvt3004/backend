package com.configs;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.entities.User;
import com.repositories.UserJPA;
import com.services.AuthDetailsService;
// import com.services.PermissionCheckService;
import com.services.PermissionService;
import com.utils.JWTUtils;
import com.utils.TokenBlacklist;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthDetailsService ourUserDetailsService;

    @Autowired
    private UserJPA userJPA;

    @Autowired
    @Lazy
    private PermissionService permissionService;
    
    private final Map<String, String> uriToPermissionMap = new HashMap<>();

    public JWTAuthFilter() {
        uriToPermissionMap.put("/api/adversetiment/add", "Add ADV");
        // Đứa nào api gì thì thêm vào, nhiều quá tui ko nhớ. Tự thêm giúp tui nhé
        uriToPermissionMap.put("/api/other/endpoint", "Other Permission");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(7);

        if (TokenBlacklist.isTokenBlacklisted(jwtToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Token is invalid.");
            return;
        }

        userEmail = jwtUtils.extractUsername(jwtToken);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = ourUserDetailsService.loadUserByUsername(userEmail);

                if (jwtUtils.isTokenValid(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(token);
                }
            } catch (UsernameNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User not found with email: " + userEmail);
                return;
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String requestURI = request.getRequestURI();

            int userId = getUserIdFromAuthentication(authentication);

            List<String> permissions = permissionService.getPermissionsByUserId(userId);

            System.out.println("Permissions: " + permissions);

            String requiredPermission = uriToPermissionMap.get(requestURI);

            if (requiredPermission != null) {
                if (permissions.contains(requiredPermission)) {
                    System.out.println("Cho phép truy cập");
                    System.out.println("Quyền của user với ID " + userId + ": " + permissions);
                    filterChain.doFilter(request, response); 
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Access Denied: You do not have permission to access this resource.");
                    return;
                }
            } else {
                filterChain.doFilter(request, response);
                return;
            }
            
        }
        filterChain.doFilter(request, response);
    }

    private int getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userJPA.findByEmailAndProvider(email,"Guest");
        System.out.println("userid la: " + userOptional.get().getUserId());
        return userOptional.get().getUserId();
    }
}
