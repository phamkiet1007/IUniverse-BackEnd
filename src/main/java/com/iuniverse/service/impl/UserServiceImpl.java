package com.iuniverse.service.impl;

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
import com.iuniverse.model.User;
import com.iuniverse.repository.AddressRepository;
import com.iuniverse.repository.UserRepository;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

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
        user.setUserType(req.getUserType());
        user.setStatus(UserStatus.NONE);

        userRepository.save(user);
        log.info("User saved successfully: {}", user);

        if(user.getId() != null) {
            log.info("Saving address for user: {}", user.getId());
            Address address = new Address();
            AddressRequest addressReq = req.getAddress();

            address.setStreet(addressReq.getStreet());
            address.setCity(addressReq.getCity());
            address.setCountry(addressReq.getCountry());
            address.setBuilding(addressReq.getBuilding());
            address.setAddressType(addressReq.getAddressType());

            address.setUser(user);

            addressRepository.save(address);
            log.info("Address saved successfully: {}", address);
        }

        return user.getId();
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
                    .userType(user.getUserType())
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
}
