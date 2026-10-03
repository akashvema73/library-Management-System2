package com.library.lms.service;

import com.library.lms.dto.RegisterRequest;
import com.library.lms.entity.AccountStatus;
import com.library.lms.entity.Role;
import com.library.lms.entity.User;
import com.library.lms.exception.BadRequestException;
import com.library.lms.exception.ResourceNotFoundException;
import com.library.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUserByAdmin(RegisterRequest request, Role role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (role == Role.STUDENT && request.getEnrollmentNumber() != null
                && !request.getEnrollmentNumber().isBlank()
                && userRepository.existsByEnrollmentNumber(request.getEnrollmentNumber())) {
            throw new BadRequestException("Enrollment number is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobile(request.getMobile());
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);

        if (role == Role.STUDENT) {
            user.setEnrollmentNumber(request.getEnrollmentNumber());
            user.setDepartment(request.getDepartment());
            user.setCourse(request.getCourse());
            user.setYear(request.getYear());
            user.setSemester(request.getSemester());
        }

        return userRepository.save(user);
    }

    public List<User> getStudents() {
        return userRepository.findByRole(Role.STUDENT);
    }

    public List<User> getLibrarians() {
        return userRepository.findByRole(Role.LIBRARIAN);
    }

    public List<User> searchStudents(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getStudents();
        }
        return userRepository.searchByRoleAndKeyword(Role.STUDENT, keyword);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User updateUser(Long id, RegisterRequest request) {
        User user = getById(id);
        if (request.getName() != null) user.setName(request.getName());
        if (request.getMobile() != null) user.setMobile(request.getMobile());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getCourse() != null) user.setCourse(request.getCourse());
        if (request.getYear() != null) user.setYear(request.getYear());
        if (request.getSemester() != null) user.setSemester(request.getSemester());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userRepository.save(user);
    }

    public User updateStatus(Long id, AccountStatus status) {
        User user = getById(id);
        user.setAccountStatus(status);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getById(id);
        userRepository.delete(user);
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
}
