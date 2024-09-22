package com.turism.users.repositories;

import com.turism.users.models.SocialMedia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialMediaRepository extends JpaRepository<SocialMedia, String> {
}
