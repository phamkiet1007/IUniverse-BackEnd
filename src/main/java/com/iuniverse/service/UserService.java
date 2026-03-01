package com.iuniverse.service;

import com.iuniverse.controller.request.UserCreationRequest;
import com.iuniverse.controller.request.UserPasswordRequest;
import com.iuniverse.controller.request.UserUpdateRequest;
import com.iuniverse.controller.response.UserPageResponse;
import com.iuniverse.controller.response.UserResponse;

import java.util.List;

public interface UserService {

    UserPageResponse findAll(String keyword, String sortBy, int page, int size);
    UserResponse findById(Long id);
    UserResponse findByUsername(String username);
    Long save(UserCreationRequest req);
    void update(UserUpdateRequest req);
    void delete(Long id);
    void changePassword(UserPasswordRequest req);



}
