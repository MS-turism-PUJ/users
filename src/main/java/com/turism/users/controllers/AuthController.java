package com.turism.users.controllers;

import com.turism.users.dtos.ErrorDTO;
import com.turism.users.dtos.LoginDTO;
import com.turism.users.dtos.RegisterClientDTO;
import com.turism.users.dtos.RegisterProviderDTO;
import com.turism.users.dtos.ValidationErrorDTO;
import com.turism.users.models.User;
import com.turism.users.services.KeycloakService;
import com.turism.users.services.MessageQueueService;
import com.turism.users.services.MinioService;
import com.turism.users.services.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final KeycloakService keycloakService;
    private final UserService userService;
    private final MessageQueueService messageQueueService;
    private final MinioService minioService;

    @Autowired
    public AuthController(KeycloakService keycloakService, UserService userService, MessageQueueService messageQueueService, MinioService minioService) {
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.messageQueueService = messageQueueService;
        this.minioService = minioService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(keycloakService.authenticate(loginDTO.getUsername(), loginDTO.getPassword()));
    }

    @PostMapping("/client/register")
    public ResponseEntity<?> registerClient(@Valid @ModelAttribute RegisterClientDTO registerClientDTO) {
        User user;
        try {
            user = userService.createUser(registerClientDTO.toUser());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Username or email already in use"));
        }

        if (registerClientDTO.getPhoto() != null) {
            try {
                minioService.uploadFile(user.getUsername(), registerClientDTO.getPhoto());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new ErrorDTO("Error uploading photo"));
            }
        }

        keycloakService.createClient(registerClientDTO.getUsername(), registerClientDTO.getEmail(), registerClientDTO.getName(), registerClientDTO.getPassword());
        messageQueueService.sendMessage(user.toUserMessageDTO());
        return ResponseEntity.ok(keycloakService.authenticate(registerClientDTO.getUsername(), registerClientDTO.getPassword()));
    }

    @PostMapping("/provider/register")
    public ResponseEntity<?> registerProvider(@Valid @ModelAttribute RegisterProviderDTO registerProviderDTO) {
        User user;
        try {
            user = userService.createUser(registerProviderDTO.toUser());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(new ErrorDTO("username or password", "Username or email already in use"));
        }

        if (registerProviderDTO.getPhoto() != null) {
            try {
                minioService.uploadFile(user.getUsername(), registerProviderDTO.getPhoto());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                System.out.println(e);
                return ResponseEntity.badRequest().body(new ValidationErrorDTO("photo", "Error uploading photo"));
            }
        }

        keycloakService.createProvider(registerProviderDTO.getUsername(), registerProviderDTO.getEmail(), registerProviderDTO.getName(), registerProviderDTO.getPassword());
        messageQueueService.sendMessage(user.toUserMessageDTO());
        return ResponseEntity.ok(keycloakService.authenticate(registerProviderDTO.getUsername(), registerProviderDTO.getPassword()));
    }
}
