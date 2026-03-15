package com.iuniverse.model;

import com.iuniverse.common.Gender;
import com.iuniverse.common.UserStatus;
import com.iuniverse.common.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "tbl_user")
public class User extends AbstractEntity<Long> implements UserDetails, Serializable {


    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", length = 255)
    private Gender gender;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date birthday;

    @Column(name = "username", unique = true, nullable = false, length = 255)
    private String username;

    @Column(name = "phone", length = 15)
    private String phoneNumber;

    @Column(name = "email",  length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "user_type", length = 255)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", length = 255)
    private UserStatus status;

    // Chữ "user" ở đây chính là tên biến User bên trong class Address
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Address address;

    @OneToMany(mappedBy = "user")
    private Set<UserHasRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<GroupHasUser> groups = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(status);
    }
}
