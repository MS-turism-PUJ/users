package com.turism.users.controllers;

import com.turism.users.models.User;
import com.turism.users.services.MinioService;
import com.turism.users.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final MinioService minioService;

    @Autowired
    public UserController(UserService userService, MinioService minioService) {
        this.userService = userService;
        this.minioService = minioService;
    }

    @PostMapping(value = "/upload/photo", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadPhoto(@RequestHeader("X-Preferred-Username") String username, @RequestParam("photo") MultipartFile photo) {
        try {
            minioService.uploadFile(photo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading photo");
        }
        User user = userService.getUserByUsername(username);
        user.setPhoto(photo.getOriginalFilename());
        return ResponseEntity.ok().body("Photo uploaded successfully");
    }
}
