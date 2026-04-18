package com.iuniverse.service;

import com.iuniverse.common.Role;
import com.iuniverse.common.UserStatus;
import com.iuniverse.controller.request.AddressRequest;
import com.iuniverse.controller.request.UserCreationRequest;
import com.iuniverse.exception.InvalidDataException;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Address;
import com.iuniverse.model.Student;
import com.iuniverse.model.Teacher;
import com.iuniverse.model.User;
import com.iuniverse.repository.AddressRepository;
import com.iuniverse.repository.CourseRepository;
import com.iuniverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

@Service
@Slf4j(topic = "ADMIN-SERVICE")
@RequiredArgsConstructor
public class AdminService  {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;

    @Transactional(rollbackFor = Exception.class)
    public Long save(UserCreationRequest req) {
        log.info("Admin is saving user: {}", req);

        if(userRepository.findByEmail(req.getEmail()) != null) {
            throw new InvalidDataException("Email is already exist!");
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new InvalidDataException("Username is already exist!");
        }

        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        user.setRole(req.getRole());

        // BẬT ACTIVE NGAY LẬP TỨC
        user.setStatus(UserStatus.ACTIVE);

        // Rẽ nhánh lưu Profile
        if (req.getRole() == Role.STUDENT) {
            if (StringUtils.isBlank(req.getStudentCode())) {
                throw new InvalidDataException("Student code is required for role STUDENT!");
            }
            Student studentProfile = new Student();
            studentProfile.setUser(user);
            studentProfile.setStudentCode(req.getStudentCode());
            user.setStudentProfile(studentProfile);

        } else if (req.getRole() == Role.TEACHER) {
            Teacher teacherProfile = new Teacher();
            teacherProfile.setUser(user);
            teacherProfile.setDepartment(req.getDepartment());
            user.setTeacherProfile(teacherProfile);
        }

        // Xử lý Address
        if(req.getAddress() != null) {
            Address address = new Address();
            AddressRequest addressReq = req.getAddress();
            address.setStreet(addressReq.getStreet());
            address.setCity(addressReq.getCity());
            address.setCountry(addressReq.getCountry());
            address.setBuilding(addressReq.getBuilding());
            address.setAddressType(addressReq.getAddressType());
            address.setUser(user);
            user.setAddress(address);
        }

        userRepository.save(user);
        log.info("Admin successfully created ACTIVE user: {}", user.getUsername());

        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeUserStatus(Long userId, UserStatus status) {
        log.info("Admin is changing status for user ID: {} to {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus(status);
        userRepository.save(user);

        log.info("Successfully changed status of user {} to {}", user.getUsername(), status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        log.info("Admin is deleting course with ID: {}", courseId);

        if (!courseRepository.existsById(courseId)) {
            log.warn("Course not found with ID: {}", courseId);
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }

        courseRepository.deleteById(courseId);
        log.info("Successfully deleted course ID: {} and all related data", courseId);
    }

}