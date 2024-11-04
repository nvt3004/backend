package com.configs;

import java.io.IOException;
import java.util.HashMap;
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
import com.errors.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repositories.UserJPA;
import com.services.AuthDetailsService;
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
            ApiResponse<String> res = new ApiResponse<>();
            res.setErrorCode(401);
            res.setMessage("Token khong ton tai");
            res.setData(null);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(res);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

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
        filterChain.doFilter(request, response);
    }

    private int getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOptional = userJPA.findByEmailAndProvider(email,"Guest");
        System.out.println("userid la: " + userOptional.get().getUserId());
        return userOptional.get().getUserId();
    }
}
