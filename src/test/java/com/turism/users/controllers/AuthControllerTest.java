package com.turism.users.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.google.gson.Gson;
import com.turism.users.dtos.LoginDTO;
import com.turism.users.models.User;
import com.turism.users.models.UserType;
import com.turism.users.services.KeycloakService;
import com.turism.users.services.MessageQueueService;

import org.keycloak.representations.AccessTokenResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakService keycloakService;

    @MockBean
    private MessageQueueService messageQueueService;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("minio.url", minio::getS3URL);
        registry.add("minio.access.key", minio::getUserName);
        registry.add("minio.access.secret", minio::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        minio.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        minio.stop();
    }

    static final User mockClient = new User("clientUsernameTest", "clientNameTest", 20, "clientEmail@test.com", null,
            "clientDescriptionTest", "clientUsernameTest", "jpg", null, UserType.CLIENT, List.of());

    static final User mockProvider = new User("providerUsernameTest", "providerNameTest", 20, "providerEmail@test.com",
            1234567890L, "providerDescriptionTest", "providerUsernameTest", "png", "www.provider.webpage.com",
            UserType.PROVIDER, List.of());

    static final String mockClientPassword = "clientPasswordTest";

    static final String mockProviderPassword = "providerPasswordTest";

    static final MockMultipartFile mockPhotoJPG = new MockMultipartFile(
            "photo",
            "profile-picture.jpg",
            "image/jpeg",
            "Fake image content".getBytes());

    static final MockMultipartFile mockPhotoPNG = new MockMultipartFile(
            "photo",
            "profile-picture.png",
            "image/png",
            "Fake image content".getBytes());

    @Test
    @Order(1)
    void registerClientTest() throws Exception {
        AccessTokenResponse mockRes = new AccessTokenResponse();
        mockRes.setToken("fake_token");
        mockRes.setTokenType("Bearer");
        mockRes.setRefreshToken("fake_refresh_token");
        mockRes.setOtherClaims("role", UserType.CLIENT.toString());

        when(keycloakService.authenticate(anyString(), anyString()))
                .thenReturn(mockRes);

        // doNothing().when(minioService).uploadFile(mockClient.getPhoto(), mockPhotoJPG);

        mockMvc.perform(multipart("/auth/client/register")
                .file(mockPhotoJPG)
                .param("username", mockClient.getUsername())
                .param("name", mockClient.getName())
                .param("age", mockClient.getAge().toString())
                .param("email", mockClient.getEmail())
                .param("password", mockClientPassword)
                .param("description", mockClient.getDescription()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("fake_token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.refresh_token").value("fake_refresh_token"))
                .andExpect(jsonPath("$.role").value(UserType.CLIENT.toString()));
    }

    @Test
    @Order(2)
    void loginClientTest() throws Exception {
        AccessTokenResponse mockRes = new AccessTokenResponse();
        mockRes.setToken("fake_token");
        mockRes.setTokenType("Bearer");
        mockRes.setRefreshToken("fake_refresh_token");
        mockRes.setOtherClaims("role", UserType.CLIENT.toString());

        when(keycloakService.authenticate(anyString(), anyString()))
                .thenReturn(mockRes);

        LoginDTO loginDTO = new LoginDTO(mockClient.getUsername(), mockClientPassword);

        Gson gson = new Gson();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(loginDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("fake_token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.refresh_token").value("fake_refresh_token"))
                .andExpect(jsonPath("$.role").value(UserType.CLIENT.toString()));
    }

    @Test
    @Order(1)
    void registerProviderTest() throws Exception {
        AccessTokenResponse mockRes = new AccessTokenResponse();
        mockRes.setToken("fake_token");
        mockRes.setTokenType("Bearer");
        mockRes.setRefreshToken("fake_refresh_token");
        mockRes.setOtherClaims("role", UserType.CLIENT.toString());

        when(keycloakService.authenticate(anyString(), anyString()))
                .thenReturn(mockRes);

        // doNothing().when(minioService).uploadFile(mockProvider.getPhoto(), mockPhotoPNG);

        mockMvc.perform(multipart("/auth/provider/register")
                .file(mockPhotoPNG)
                .param("username", mockProvider.getUsername())
                .param("name", mockProvider.getName())
                .param("age", mockProvider.getAge().toString())
                .param("email", mockProvider.getEmail())
                .param("password", mockProviderPassword)
                .param("description", mockProvider.getDescription())
                .param("webPage", mockProvider.getWebPage())
                .param("phone", mockProvider.getPhone().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("fake_token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.refresh_token").value("fake_refresh_token"))
                .andExpect(jsonPath("$.role").value(UserType.PROVIDER.toString()));
    }

    @Test
    @Order(2)
    void loginProviderTest() throws Exception {
        AccessTokenResponse mockRes = new AccessTokenResponse();
        mockRes.setToken("fake_token");
        mockRes.setTokenType("Bearer");
        mockRes.setRefreshToken("fake_refresh_token");
        mockRes.setOtherClaims("role", UserType.PROVIDER.toString());

        when(keycloakService.authenticate(anyString(), anyString()))
                .thenReturn(mockRes);

        LoginDTO loginDTO = new LoginDTO(mockProvider.getUsername(), mockProviderPassword);

        Gson gson = new Gson();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(loginDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("fake_token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.refresh_token").value("fake_refresh_token"))
                .andExpect(jsonPath("$.role").value(UserType.PROVIDER.toString()));
    }

}
