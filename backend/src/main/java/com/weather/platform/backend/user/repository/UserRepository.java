package com.weather.platform.backend.user.repository;

import com.weather.platform.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
