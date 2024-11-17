package com.turism.users.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.turism.users.models.User;
import com.turism.users.models.UserType;
import com.turism.users.repositories.UserRepository;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2024-11-07T00-52-20Z.fips");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("minio.url", minio::getS3URL);
        registry.add("minio.access.key", minio::getUserName);
        registry.add("minio.access.secret", minio::getPassword);
    }

    static final User mockClient = new User("clientUsernameTest", "clientNameTest", 20, "clientEmail@test.com", null,
            "clientDescriptionTest", "clientUsernameTest", "jpg", null, UserType.CLIENT, List.of());

    static final MockMultipartFile mockPhotoJPG = new MockMultipartFile(
            "photo",
            "profile-picture.jpg",
            "image/jpeg",
            "Fake image content".getBytes());

    @BeforeAll
    static void beforeAll(@Autowired UserRepository userRepository) {
        postgres.start();
        minio.start();
        userRepository.save(mockClient);
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        minio.stop();
    }

    @Test
    @Order(1)
    void uploadPhotoTest() throws Exception {
        mockMvc.perform(multipart("/upload/photo")
                .file(mockPhotoJPG)
                .header("X-Preferred-Username", mockClient.getUsername()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void getPhotoTest() throws Exception {
        mockMvc.perform(get("/photo")
                .header("X-Preferred-Username", mockClient.getUsername()))
                .andExpect(status().isOk());
    }
}
