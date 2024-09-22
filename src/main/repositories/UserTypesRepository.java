package com.turism.users.repositories;

import com.turism.users.models.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserType, String> {
}
