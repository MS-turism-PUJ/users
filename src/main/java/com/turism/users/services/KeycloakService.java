package com.turism.users.services;

import jakarta.annotation.PostConstruct;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
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
    public void initKeycloak() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUser)
                .password(adminPassword)
                .build();
    }

    private void createUser(String username, String password, String roleName) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);

        // Configuración de la contraseña
        CredentialRepresentation passwordCredential = new CredentialRepresentation();
        passwordCredential.setTemporary(false); // False si no es temporal
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

    public void createClient(String username, String password) {
        createUser(username, password, "ROLE_CLIENT");
    }
    public void createProvider(String username, String password) {
        createUser(username, password, "ROLE_PROVIDER");
    }

    public Keycloak authenticate(String username, String password) {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId("TurismoClient")
                .username(username)
                .password(password)
                .build();
    }
}
