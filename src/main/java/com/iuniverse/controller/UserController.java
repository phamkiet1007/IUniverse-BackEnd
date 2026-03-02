package com.iuniverse.controller;

import com.iuniverse.controller.request.UserCreationRequest;
import com.iuniverse.controller.request.UserPasswordRequest;
import com.iuniverse.controller.request.UserUpdateRequest;
import com.iuniverse.controller.response.UserPageResponse;
import com.iuniverse.controller.response.UserResponse;
import com.iuniverse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "APIs for user management")
@Slf4j(topic = "USER-CONTROLLER")
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Retrieve a list of all users in the system")
    @GetMapping("/list")
    public Map<String, Object> getAllUsers(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String sortBy,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        log.info("Getting all users");
        UserPageResponse userResponseList = userService.findAll(keyword, sortBy, page, size);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "users");
        result.put("data", userResponseList);

        return result;
    }

    @Operation(summary = "Get user by ID", description = "Provide a user ID in the path to retrieve their details")
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable @Min(value = 1, message = "userId must be equal or greater than 1") Long id) {
        log.info("Getting user with ID: {}", id);

        UserResponse userResponse = userService.findById(id);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("status", HttpStatus.OK.value());
        result.put("message", "user");
        result.put("data", userResponse);

        return result;
    }

    @Operation(summary = "Get user by username", description = "Provide a username in the path to retrieve their details")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse response = userService.findByUsername(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Add new user", description = "Send a UserCreationRequest payload to create a new user account")
    @PostMapping("/add")
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserCreationRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED);
        result.put("message", "User created successfully");
        result.put("data", userService.save(request));
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update user", description = "Send a UserUpdateRequest to update user details (excluding password)")
    @PutMapping("/update")
    public ResponseEntity<Void> updateUser(@RequestBody @Valid UserUpdateRequest request) {
        log.info("Updating user {}", request);

        userService.update(request);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Delete user", description = "Remove a user from the system by their ID")
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID {}", id);

        userService.delete(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.RESET_CONTENT.value());
        result.put("message", "User deleted successfully");
        result.put("data", "");
        return result;
    }

    @Operation(summary = "Change password", description = "Dedicated API for changing a user's password")
    @PatchMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody @Valid UserPasswordRequest request) {
        log.info("Changing password for user {}", request);
        userService.changePassword(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.NO_CONTENT.value());
        result.put("message", "Password updated successfully");
        result.put("data", "");
        return result;

    }
}