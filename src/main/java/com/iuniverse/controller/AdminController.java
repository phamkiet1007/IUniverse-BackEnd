package com.iuniverse.controller;


import com.iuniverse.common.UserStatus;
import com.iuniverse.controller.request.UserCreationRequest;
import com.iuniverse.controller.response.UserPageResponse;
import com.iuniverse.service.AdminService;
import com.iuniverse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin Controller", description = "APIs for admin management")
@Slf4j(topic = "ADMIN-CONTROLLER")
@Validated
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @Operation(summary = "Add new user", description = "Send a UserCreationRequest payload to create a new user account")
    @PostMapping("/add")
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserCreationRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED);
        result.put("message", "User created successfully");
        result.put("data", adminService.save(request));
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Change user status", description = "Update user status (ACTIVE, INACTIVE, BANNED, NONE)")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Object> changeUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {

        adminService.changeUserStatus(id, status);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Update status of this user to: " + status.name());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Delete course", description = "Admin deletes a course and all cascading relationships")
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Object> deleteCourse(@PathVariable Long id) {

        adminService.deleteCourse(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Delete course successfully!");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get all users", description = "Retrieve a list of all users in the system")
    @GetMapping("/users")
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
}
