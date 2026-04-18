package com.iuniverse.service;

import com.iuniverse.common.UserStatus;
import com.iuniverse.controller.request.StudentRegisterRequest;
import com.iuniverse.controller.request.UserCreationRequest;
import com.iuniverse.controller.request.UserUpdateRequest;
import com.iuniverse.controller.response.UserPageResponse;
import com.iuniverse.controller.response.UserResponse;
import com.iuniverse.model.User;

public interface UserService {

    UserPageResponse findAll(String keyword, String sortBy, int page, int size);
    UserResponse findById(Long id);
    UserResponse findByUsername(String username);

    //dùng cho reset-token
//    User getByUsername(String username);

    User getUserByEmail(String email);

    Long saveUser(User user);
    void update(UserUpdateRequest req, String currentUsername);
    void delete(Long id);

    void verifyOtp(String email, String otp);

    void resendOtp(String email);

    void registerStudent(StudentRegisterRequest req);

}
