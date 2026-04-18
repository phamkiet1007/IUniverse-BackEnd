package com.iuniverse.controller;


import com.iuniverse.controller.request.StudentRegisterRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "APIs for user management")
@Slf4j(topic = "USER-CONTROLLER")
@Validated
public class UserController {

    private final UserService userService;

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


    @Operation(summary = "Update user", description = "Send a UserUpdateRequest to update user details (excluding password)")
    @PutMapping("/update")
    public ResponseEntity<Void> updateUser(
            @RequestBody @Valid UserUpdateRequest request,
            java.security.Principal principal) {

        log.info("Forwarding update request for user: {}", principal.getName());

        userService.update(request, principal.getName());

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @PostMapping("/register")
    public ResponseEntity<Object> registerStudent(@RequestBody StudentRegisterRequest request) {
        userService.registerStudent(request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 201);
        response.put("message", "Register successfully! OTP has been sent to your email.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}