package com.library.lms.service;

import com.library.lms.dto.AuthResponse;
import com.library.lms.dto.LoginRequest;
import com.library.lms.dto.RegisterRequest;
import com.library.lms.entity.AccountStatus;
import com.library.lms.entity.Role;
import com.library.lms.entity.User;
import com.library.lms.exception.BadRequestException;
import com.library.lms.repository.UserRepository;
import com.library.lms.security.CustomUserDetails;
import com.library.lms.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (request.getEnrollmentNumber() != null && !request.getEnrollmentNumber().isBlank()
                && userRepository.existsByEnrollmentNumber(request.getEnrollmentNumber())) {
            throw new BadRequestException("Enrollment number is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobile(request.getMobile());

        // Public self-registration is always as STUDENT for safety.
        user.setRole(Role.STUDENT);
        user.setEnrollmentNumber(request.getEnrollmentNumber());
        user.setDepartment(request.getDepartment());
        user.setCourse(request.getCourse());
        user.setYear(request.getYear());
        user.setSemester(request.getSemester());
        user.setAccountStatus(AccountStatus.ACTIVE);

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
