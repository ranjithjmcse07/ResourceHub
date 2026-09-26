package com.toolshare.service;

import com.toolshare.dao.UserDao;
import com.toolshare.dto.AuthRequest;
import com.toolshare.dto.AuthResponse;
import com.toolshare.dto.RegisterRequest;
import com.toolshare.exception.BadRequestException;
import com.toolshare.model.User;
import com.toolshare.security.CustomUserDetailsService;
import com.toolshare.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(UserDao userDao, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse register(RegisterRequest req) {
        if (req.getAddress() == null || req.getAddress().trim().isEmpty() || req.getAddress().trim().length() < 5) {
            throw new BadRequestException("Complete address is required for registration (minimum 5 characters). Please provide your street address, city, and area.");
        }

        validatePasswordStrength(req.getPassword());

        if (userDao.existsByUsername(req.getUsername().trim())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userDao.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email address is already registered");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setUsername(req.getUsername().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setMobile(req.getMobile().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole().toUpperCase());
        user.setCompanyName(req.getCompanyName());
        user.setAddress(req.getAddress());
        user.setActive(true);

        User savedUser = userDao.createUser(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String token = jwtService.generateToken(userDetails, savedUser.getRole(), savedUser.getId());

        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getFullName(), savedUser.getEmail(), savedUser.getRole(),
                savedUser.getMobile(), savedUser.getAddress(), savedUser.getCompanyName());
    }

    public AuthResponse login(AuthRequest req) {
        User user = userDao.findByUsernameOrEmail(req.getUsernameOrEmail().trim())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!user.isActive()) {
            throw new BadRequestException("Account has been deactivated. Please contact support.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), req.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails, user.getRole(), user.getId());

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole(),
                user.getMobile(), user.getAddress(), user.getCompanyName());
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must contain at least one uppercase letter (A-Z).");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must contain at least one lowercase letter (a-z).");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain at least one number (0-9).");
        }
        if (!password.matches(".*[@$!%*?&_#^~\\-+./=<>;:,].*")) {
            throw new BadRequestException("Password must contain at least one special character (@$!%*?&#^~-+).");
        }
    }
}
