package com.example.taskworklife.repo;

import com.example.taskworklife.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
    User findUserByEmail(String email);

    boolean existsByEmail(String email);
}
