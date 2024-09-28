package com.turism.users.controllers;

import com.turism.users.dtos.RegisterClientDTO;
import com.turism.users.dtos.RegisterProviderDTO;
import com.turism.users.services.KeycloakService;
import com.turism.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final KeycloakService keycloakService;
    private final UserService userService;

    @Autowired
    public AuthController(KeycloakService keycloakService, UserService userService) {
        this.keycloakService = keycloakService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public String login() {
        return "Login";
    }

    @PostMapping("/client/register")
    public String registerClient(@RequestBody RegisterClientDTO registerClientDTO) {
        keycloakService.createClient(registerClientDTO.getName(), registerClientDTO.getPassword());
        userService.createUser(registerClientDTO.toUser());
        return "Register";
    }

    @PostMapping("/provider/register")
    public String registerProvider(@RequestBody RegisterProviderDTO registerProviderDTO) {
        keycloakService.createProvider(registerProviderDTO.getName(), registerProviderDTO.getPassword());
        userService.createUser(registerProviderDTO.toUser());
        return "Register";
    }
}
