package com.turism.users.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.turism.users.dtos.LoginDTO;
import com.turism.users.models.User;
import com.turism.users.models.UserType;
import com.turism.users.repositories.UserRepository;
import com.turism.users.services.KeycloakService;

import org.keycloak.representations.AccessTokenResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakService keycloakService;

    @Autowired
    private UserRepository userRepository;

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z");

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("minio.url", minio::getS3URL);
        registry.add("minio.access.key", minio::getUserName);
        registry.add("minio.access.secret", minio::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    static KafkaConsumer<Object, Object> mockKafkaConsumer;

    static void createMockKafkaConsumer() {
        String groupId = "test-group";
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.turism.*");
        mockKafkaConsumer = new KafkaConsumer<>(properties, new JsonDeserializer<>(), new JsonDeserializer<>());
        mockKafkaConsumer.subscribe(List.of("usersQueue"));
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        minio.start();
        kafka.start();
        createMockKafkaConsumer();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        minio.stop();
        kafka.stop();
    }

    static final User mockClient = new User("clientUsernameTest", "clientNameTest", 20, "clientEmail@test.com",
            null,
            "clientDescriptionTest", "clientUsernameTest", "jpg", null, UserType.CLIENT, List.of());

    static final User mockProvider = new User("providerUsernameTest", "providerNameTest", 20,
            "providerEmail@test.com",
            1234567890L, "providerDescriptionTest", "providerUsernameTest", "png",
            "www.provider.webpage.com",
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

    @Test
    @Order(3)
    void userMessagesSentTest() throws Exception {
        ConsumerRecords<Object, Object> records = mockKafkaConsumer.poll(Duration.ofSeconds(5));

        assertEquals(2, records.count());

        List<String> actualMessages = new ArrayList<>();
        for (ConsumerRecord<Object, Object> record : records) {
            actualMessages.add(record.value().toString());
        }

        User mockClientSaved = userRepository.findByUsername(mockClient.getUsername());
        User mockProviderSaved = userRepository.findByUsername(mockProvider.getUsername());

        String expectedClientMessage = new Gson().toJson(mockClientSaved.toUserMessageDTO());
        String expectedProviderMessage = new Gson().toJson(mockProviderSaved.toUserMessageDTO());

        assertTrue(actualMessages.contains(expectedClientMessage), "Client message not found in Kafka");
        assertTrue(actualMessages.contains(expectedProviderMessage), "Provider message not found in Kafka");
    }

}
