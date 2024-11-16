package com.configs;

import com.errors.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.AuthDetailsService;
import com.utils.JWTUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.utils.TokenBlacklist;

import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthDetailsService ourUserDetailsService;

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

        try {
            userEmail = jwtUtils.extractUsername(jwtToken);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = ourUserDetailsService.loadUserByUsername(userEmail);

                if (jwtUtils.isTokenValid(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(token);
                }
            }
        } catch (ExpiredJwtException ex) {
            // Nếu token hết hạn, trả về mã lỗi 999
            ApiResponse<String> res = new ApiResponse<>();
            res.setErrorCode(999);
            res.setMessage("Token has expired");
            res.setData(null);
            response.setStatus(999); // Đặt mã trạng thái HTTP là 999
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(res);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
            return;
        }
        filterChain.doFilter(request, response);
    }

}
