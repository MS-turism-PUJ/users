package com.turism.users.services;

import jakarta.annotation.PostConstruct;
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

@Service
public class KeycloakService {
    @Value("${keycloak.admin.username}")
    private String adminUser;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private final String serverUrl = "http://localhost:9000/";

    private final String realm = "TurismoRealm";

    private Keycloak keycloak;

    @PostConstruct
    private void initKeycloak() {
        authenticateAdmin();
    }

    private void authenticateAdmin() {
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUser)
                .password(adminPassword)
                .build();
    }

    private void createUser(String username, String email, String name, String password, String roleName) {
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
        createUser(username, email, name, password, "ROLE_CLIENT");
    }
    public void createProvider(String username, String email, String name, String password) {
        createUser(username, email, name, password, "ROLE_PROVIDER");
    }

    public AccessTokenResponse authenticate(String username, String password) {
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
