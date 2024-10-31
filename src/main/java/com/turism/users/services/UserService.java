package com.turism.users.services;

import com.turism.users.models.User;
import com.turism.users.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        log.info("Creating user: " + user.getUsername());
        return userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        log.info("Getting user by username: " + username);
        return userRepository.findByUsername(username);
    }

    public User addPhoto(User user, String photo, String extension) {
        log.info("Adding photo to user: " + user.getUsername());
        user.setPhoto(photo);
        user.setPhotoExtension(extension);
        return userRepository.save(user);
    }
}
