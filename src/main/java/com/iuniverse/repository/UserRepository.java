package com.iuniverse.repository;

import com.iuniverse.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "SELECT u FROM UserEntity u WHERE u.status='ACTIVE' " +
            "and (lower(u.firstName) like :keyword " +
            "or lower(u.lastName) like :keyword " +
            "or u.phoneNumber like :keyword " +
            "or lower(u.email) like :keyword ) ")
    Page<UserEntity> searchByKeyword(String keyword, Pageable pageable);

    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
}
