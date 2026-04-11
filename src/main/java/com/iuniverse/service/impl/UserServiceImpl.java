package com.iuniverse.service.impl;

import com.iuniverse.common.Role;
import com.iuniverse.common.UserStatus;
import com.iuniverse.controller.request.AddressRequest;
import com.iuniverse.controller.request.UserCreationRequest;
import com.iuniverse.controller.request.ResetPasswordRequest;
import com.iuniverse.controller.request.UserUpdateRequest;
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
        return null;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(UserCreationRequest req) {
        log.info("Saving user: {}", req);

        User userByEmail = userRepository.findByEmail(req.getEmail());
        if(userByEmail != null) {
            log.warn("Email is already exist: {}", req.getEmail());
            throw new InvalidDataException("Email is already exist!");
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
        user.setStatus(UserStatus.NONE);

        //generate otp within 5 mins live
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiryTime(new Date(System.currentTimeMillis() + (5 * 60 * 1000)));

        if (req.getRole() == Role.STUDENT) {
            if (StringUtils.isBlank(req.getStudentCode())) {
                throw new InvalidDataException("Student code is required for role STUDENT!");
            }
            Student studentProfile = new Student();
            studentProfile.setUser(user);
            studentProfile.setStudentCode(req.getStudentCode());
            user.setStudentProfile(studentProfile); // Gắn con vào mẹ -> Hibernate sẽ tự lưu

        } else if (req.getRole() == Role.TEACHER) {
            Teacher teacherProfile = new Teacher();
            teacherProfile.setUser(user);
            teacherProfile.setDepartment(req.getDepartment());
            user.setTeacherProfile(teacherProfile);
        }

        if(req.getAddress() != null) {
            log.info("Preparing address for user...");
            Address address = new Address();
            AddressRequest addressReq = req.getAddress();

            address.setStreet(addressReq.getStreet());
            address.setCity(addressReq.getCity());
            address.setCountry(addressReq.getCountry());
            address.setBuilding(addressReq.getBuilding());
            address.setAddressType(addressReq.getAddressType());

            // Quan trọng: Gắn User vào Address và Gắn Address vào User
            address.setUser(user);
            user.setAddress(address);
        }

        userRepository.save(user);
        log.info("User, Profile and Address saved successfully: {}", user);

        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
            log.info("OTP Email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
            // Có thể throw Exception hoặc kệ nó tùy logic của bạn
        }

        return user.getId();
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
    public void update(UserUpdateRequest req) {
        log.info("Updating user: {}", req);

        User user = getUserEntity(req.getId());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setUsername(req.getUsername());



        userRepository.save(user);
        log.info("User updated successfully: {}", user);

        Address address = addressRepository.findByUserIdAndAddressType(req.getId(), req.getAddress().getAddressType());

        if (address != null) {
            log.info("Updating address for user: {}", user.getId());
            address.setStreet(req.getAddress().getStreet());
            address.setCity(req.getAddress().getCity());
            address.setCountry(req.getAddress().getCountry());
            address.setBuilding(req.getAddress().getBuilding());
            addressRepository.save(address);
            log.info("Address updated successfully: {}", address);
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
        log.info("Đã tạo OTP mới cho user: {}", user.getEmail());

        try {
            emailService.sendOtpEmail(user.getEmail(), newOtp);
            log.info("Resend OTP Email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend OTP email", e);
        }
    }
}
