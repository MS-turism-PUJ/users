package com.turism.users.controllers;

import com.turism.users.dtos.ErrorDTO;
import com.turism.users.dtos.LoginDTO;
import com.turism.users.dtos.RegisterClientDTO;
import com.turism.users.dtos.RegisterProviderDTO;
import com.turism.users.models.User;
import com.turism.users.services.KeycloakService;
import com.turism.users.services.MessageQueueService;
import com.turism.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final KeycloakService keycloakService;
    private final UserService userService;
    private final MessageQueueService messageQueueService;

    @Autowired
    public AuthController(KeycloakService keycloakService, UserService userService, MessageQueueService messageQueueService) {
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.messageQueueService = messageQueueService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(keycloakService.authenticate(loginDTO.getUsername(), loginDTO.getPassword()));
    }

    @PostMapping("/client/register")
    public ResponseEntity<?> registerClient(@RequestBody RegisterClientDTO registerClientDTO) {
        if (!registerClientDTO.valid()) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Invalid data", "/auth/client/register", 400));
        }
        User user;
        try {
            user = userService.createUser(registerClientDTO.toUser());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Username or email already in use", "/auth/client/register", 400));
        }
        keycloakService.createClient(registerClientDTO.getUsername(), registerClientDTO.getEmail(), registerClientDTO.getName(), registerClientDTO.getPassword());
        messageQueueService.sendMessage(user.toUserMessageDTO());
        return ResponseEntity.ok(keycloakService.authenticate(registerClientDTO.getUsername(), registerClientDTO.getPassword()));
    }

    @PostMapping("/provider/register")
    public ResponseEntity<?> registerProvider(@RequestBody RegisterProviderDTO registerProviderDTO) {
        if (!registerProviderDTO.valid()) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Invalid data", "/auth/client/register", 400));
        }
        User user;
        try {
            user = userService.createUser(registerProviderDTO.toUser());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Username or email already in use", "/auth/client/register", 400));
        }
        keycloakService.createProvider(registerProviderDTO.getUsername(), registerProviderDTO.getEmail(), registerProviderDTO.getName(), registerProviderDTO.getPassword());
        messageQueueService.sendMessage(user.toUserMessageDTO());
        return ResponseEntity.ok(keycloakService.authenticate(registerProviderDTO.getUsername(), registerProviderDTO.getPassword()));
    }
}
