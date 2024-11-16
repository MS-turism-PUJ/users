package com.turism.users.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class KeycloakService {
    @Value("${keycloak.admin.username}")
    private String adminUser;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    private final String realm = "TurismoRealm";

    private Keycloak keycloak;

    @PostConstruct
    private void initKeycloak() {
        authenticateAdmin();
    }

    private void authenticateAdmin() {
        log.info("Authenticating as admin");
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUser)
                .password(adminPassword)
                .build();
    }

    private void createUser(String username, String email, String name, String password, String roleName) {
        log.info("Creating user {}", username);
        authenticateAdmin();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setFirstName(name);
        user.setLastName("no last name");

        // Configuración de la contraseña
        CredentialRepresentation passwordCredential = new CredentialRepresentation();
        passwordCredential.setTemporary(false);
        passwordCredential.setType(CredentialRepresentation.PASSWORD);
        passwordCredential.setValue(password);

        // Crear usuario en Keycloak
        keycloak.realm(realm)
                .users()
                .create(user);

        // Asignar la contraseña al usuario
        String userId = keycloak.realm(realm)
                .users()
                .search(username).get(0).getId();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .resetPassword(passwordCredential);

        // Asignar el rol al usuario
        RoleRepresentation userRole = keycloak.realm(realm)
                .roles()
                .get(roleName)
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(Collections.singletonList(userRole));
    }

    public void createClient(String username, String email, String name, String password) {
        log.info("Creating client {}", username);
        createUser(username, email, name, password, "ROLE_CLIENT");
    }
    public void createProvider(String username, String email, String name, String password) {
        log.info("Creating provider {}", username);
        createUser(username, email, name, password, "ROLE_PROVIDER");
    }

    public AccessTokenResponse authenticate(String username, String password) {
        log.info("Authenticating as {}", username);
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId("webapp")
                .username(username)
                .password(password)
                .build();

        return keycloak.tokenManager().getAccessToken();
    }

    public AccessTokenResponse refresh(String refreshToken) {
        log.info("Refreshing token");
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId("webapp")
                .grantType(OAuth2Constants.REFRESH_TOKEN)
                .authorization("Bearer " + refreshToken)
                .build();

        return keycloak.tokenManager().getAccessToken();
    }
}
