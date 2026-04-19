package com.iuniverse.service.impl;

import com.iuniverse.common.Gender;
import com.iuniverse.common.Role;
import com.iuniverse.common.UserStatus;
import com.iuniverse.controller.request.*;
import com.iuniverse.controller.response.UserPageResponse;
import com.iuniverse.controller.response.UserResponse;
import com.iuniverse.exception.InvalidDataException;
import com.iuniverse.exception.ResourceNotFoundException;
import com.iuniverse.model.Address;
import com.iuniverse.model.Student;
import com.iuniverse.model.Teacher;
import com.iuniverse.model.User;
import com.iuniverse.repository.AddressRepository;
import com.iuniverse.repository.UserRepository;
import com.iuniverse.service.EmailService;
import com.iuniverse.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserPageResponse findAll(String keyword, String sortBy, int page, int size) {
        //Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.isNotBlank(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); //column name : asc/desc
            Matcher matcher = pattern.matcher(sortBy);

            if (matcher.find()) {
                String columnName = matcher.group(1);

                if(matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        //Hanlde case: FE wanna start with page = 1
        int pageNo = 0;
        if (page > 0) {
            pageNo = page - 1;
        }

        //Paging
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        Page<User> entityPage;

        if(StringUtils.isNotBlank(keyword)) {
            keyword = keyword.trim();
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = userRepository.searchByKeyword(keyword, pageable);
        } else {
            entityPage = userRepository.findAll(pageable);
        }

        return getUserPageResponse(page, size, entityPage);
    }



    @Override
    public UserResponse findById(Long id) {
        log.info("Finding user with ID: {}", id);

        User user = getUserEntity(id);

        return UserResponse.builder()
                .id(id)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .username(user.getUsername())
                .build();
    }

    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("user not found with username: " + username);
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    //dùng cho reset-token
//    @Override
//    public User getByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }

    @Override
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public void registerStudent(StudentRegisterRequest req) {
        log.info("Processing student registration for email: {}", req.getEmail());

        if (userRepository.findByEmail(req.getEmail()) != null) {
            throw new InvalidDataException("Email already exists!");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new InvalidDataException("Username already exists!");
        }

        // 2. Tạo User mặc định
        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(Gender.valueOf(req.getGender()));
        user.setBirthday(req.getBirthday());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        // ÉP CỨNG ROLE LÀ STUDENT
        user.setRole(Role.STUDENT);
        user.setStatus(UserStatus.NONE);

        // 3. Tạo Profile Sinh viên
        if (req.getStudentCode() == null || req.getStudentCode().trim().isEmpty()) {
            throw new InvalidDataException("Student code cannot be empty!");
        }
        Student studentProfile = new Student();
        studentProfile.setUser(user);
        studentProfile.setStudentCode(req.getStudentCode());
        user.setStudentProfile(studentProfile);

        // 4. Sinh OTP 5 phút
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiryTime(new Date(System.currentTimeMillis() + (5 * 60 * 1000)));

        userRepository.save(user);

        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
            log.info("Sent OTP to email: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending email OTP", e);
        }
    }


    //validate otp
    @Transactional(rollbackFor = Exception.class)
    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found!");
        }

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidDataException("Account is already activated!");
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new InvalidDataException("Invalid OTP code!");
        }

        if (user.getOtpExpiryTime().before(new Date())) {
            throw new InvalidDataException("OTP has expired!");
        }

        // Kích hoạt tài khoản và xóa OTP đi cho an toàn
        user.setStatus(UserStatus.ACTIVE);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);

        userRepository.save(user);
        log.info("Account activated successfully for email: {}", email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateRequest req, String currentUsername) {
        log.info("User '{}' is requesting to update profile ID: {}", currentUsername, req.getId());

        // 1. Tìm User đang đăng nhập dựa vào token
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("Current user not found!");
        }

        // 2. BẢO MẬT: Kiểm tra xem ID muốn sửa có phải là ID của chính họ không
        if (!currentUser.getId().equals(req.getId())) {
            log.warn("IDOR attempt! User {} tried to update profile of user ID {}", currentUsername, req.getId());
            throw new AccessDeniedException("Access denied! You can only update your own profile.");
        }

        // 3. Tiến hành update như bình thường (Dùng luôn currentUser cho tối ưu)
        currentUser.setFirstName(req.getFirstName());
        currentUser.setLastName(req.getLastName());
        currentUser.setGender(req.getGender());
        currentUser.setBirthday(req.getBirthday());
        currentUser.setEmail(req.getEmail());
        currentUser.setPhoneNumber(req.getPhoneNumber());
        // Lưu ý: Không cho phép đổi Username ở đây để tránh lỗi hệ thống

        userRepository.save(currentUser);
        log.info("User updated successfully: {}", currentUser.getUsername());

        // 4. Xử lý Address
        if (req.getAddress() != null) {
            Address address = addressRepository.findByUserIdAndAddressType(currentUser.getId(), req.getAddress().getAddressType());
            if (address != null) {
                log.info("Updating address for user: {}", currentUser.getId());
                address.setStreet(req.getAddress().getStreet());
                address.setCity(req.getAddress().getCity());
                address.setCountry(req.getAddress().getCountry());
                address.setBuilding(req.getAddress().getBuilding());
                addressRepository.save(address);
                log.info("Address updated successfully");
            }
        }
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = getUserEntity(id);
        user.setStatus(UserStatus.INACTIVE);

        userRepository.save(user);
        log.info("Deleted user: {}", user);
    }

    @Override
    public Long saveUser(User user) {
        return userRepository.save(user).getId();
    }


    private User getUserEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Convert UserEntity to UserResponse
     * @param page
     * @param size
     * @param userEntities
     * @return
     */
    private static UserPageResponse getUserPageResponse(int page, int size, Page<User> userEntities) {

        List<UserResponse> userResponseList = userEntities.stream().map(user -> {

            AddressRequest addressDto = null;
            if (user.getAddress() != null) {
                addressDto = new AddressRequest();
                addressDto.setStreet(user.getAddress().getStreet());
                addressDto.setCity(user.getAddress().getCity());
                addressDto.setCountry(user.getAddress().getCountry());
                addressDto.setBuilding(user.getAddress().getBuilding());
                addressDto.setAddressType(user.getAddress().getAddressType());
            }

            return UserResponse.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .gender(user.getGender())
                    .birthday(user.getBirthday())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .status(user.getStatus() != null ? user.getStatus().name() : null)
                    .address(addressDto)
                    .build();
        }).toList();

        UserPageResponse response = new UserPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(userEntities.getTotalElements());
        response.setTotalPages(userEntities.getTotalPages());
        response.setUsers(userResponseList);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resendOtp(String email) {
        log.info("Resend OTP for email: {}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found!");
        }

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidDataException("Account is already activated. Please login!");
        }

        String newOtp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(newOtp);
        user.setOtpExpiryTime(new Date(System.currentTimeMillis() + (5 * 60 * 1000)));

        userRepository.save(user);
        log.info("Created new OTP for user: {}", user.getEmail());

        try {
            emailService.sendOtpEmail(user.getEmail(), newOtp);
            log.info("Resend OTP Email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend OTP email", e);
        }
    }
}
