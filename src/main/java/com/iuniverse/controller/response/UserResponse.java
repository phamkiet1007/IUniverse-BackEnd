package com.iuniverse.controller.response;


import com.iuniverse.common.Gender;
import com.iuniverse.common.Role;
import com.iuniverse.controller.request.AddressRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserResponse implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Date birthday;
    private String username;
    private String phoneNumber;
    private String email;
    private AddressRequest address;
    private Role role;
    private String status;
}
