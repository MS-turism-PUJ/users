package com.turism.users.controllers;

import com.turism.users.dtos.ErrorDTO;
import com.turism.users.dtos.ValidationErrorDTO;
import com.turism.users.models.User;
import com.turism.users.services.MinioService;
import com.turism.users.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/")
public class UserController {
    private final UserService userService;
    private final MinioService minioService;

    @Autowired
    public UserController(UserService userService, MinioService minioService) {
        this.userService = userService;
        this.minioService = minioService;
    }

    @PostMapping(value = "/upload/photo")
    public ResponseEntity<?> uploadPhoto(@RequestHeader("X-Preferred-Username") String username, @RequestParam("photo") MultipartFile photo) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(null, "User not found"));
        }
        try {
            minioService.uploadFile(username, photo);
        } catch (Exception e) {
            log.error("Error uploading photo", e);
            return ResponseEntity.badRequest().body(new ValidationErrorDTO("photo", "Error uploading photo"));
        }
        user = userService.addPhoto(user, username);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping(value = "/photo")
    public ResponseEntity<?> getPhoto(@RequestHeader("X-Preferred-Username") String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(null, "User not found"));
        }
        if (user.getPhoto() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("photo", "User has no photo"));
        }
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(IOUtils.toByteArray(minioService.getObject(user.getPhoto())));
        } catch (Exception e) {
            log.error("Error getting photo", e);
            return ResponseEntity.badRequest().body("Error getting photo");
        }
    }
}
