package com.iuniverse.repository;

import com.iuniverse.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT u FROM User u WHERE u.status='ACTIVE' " +
            "and (lower(u.firstName) like :keyword " +
            "or lower(u.lastName) like :keyword " +
            "or u.phoneNumber like :keyword " +
            "or lower(u.email) like :keyword ) ")
    Page<User> searchByKeyword(String keyword, Pageable pageable);

    User findByUsername(String username);
    User findByEmail(String email);
}
