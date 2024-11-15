package com.turism.users.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import com.turism.users.services.MinioService;
import com.turism.users.services.UserService;

import org.keycloak.representations.AccessTokenResponse;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakService keycloakService;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageQueueService messageQueueService;

    @MockBean
    private MinioService minioService;

    @Test
    void registerClientTest() throws Exception {
        User mockUser = new User("usernameTest", "nameTest", 20, "email@test.com", null,
                "descriptionTest", "usernameTest", "jpg", null, UserType.CLIENT, List.of());

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "profile-picture.jpg",
                "image/jpeg",
                "Fake image content".getBytes());

        AccessTokenResponse mockRes = new AccessTokenResponse();
        mockRes.setToken("fake_token");
        mockRes.setTokenType("Bearer");
        mockRes.setRefreshToken("fake_refresh_token");
        mockRes.setOtherClaims("role", UserType.CLIENT.toString());

        when(userService.createUser(any(User.class))).thenReturn(mockUser);

        when(keycloakService.authenticate(anyString(), anyString()))
                .thenReturn(mockRes);

        doNothing().when(minioService).uploadFile(mockUser.getPhoto(), photo);

        mockMvc.perform(multipart("/auth/client/register")
                .file(photo)
                .param("username", mockUser.getUsername())
                .param("name", mockUser.getName())
                .param("age", mockUser.getAge().toString())
                .param("email", mockUser.getEmail())
                .param("password", "passwordTest")
                .param("description", mockUser.getDescription()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("fake_token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.refresh_token").value("fake_refresh_token"))
                .andExpect(jsonPath("$.role").value(UserType.CLIENT.toString()));
    }

    @Test
    void loginClientTest() throws Exception {
        User mockUser = new User("usernameTest", "nameTest", 20, "email@test.com", null,
                "descriptionTest", "usernameTest", "jpg", null, UserType.CLIENT, List.of());

        AccessTokenResponse mockRes = new AccessTokenResponse();
        mockRes.setToken("fake_token");
        mockRes.setTokenType("Bearer");
        mockRes.setRefreshToken("fake_refresh_token");
        mockRes.setOtherClaims("role", UserType.CLIENT.toString());

        when(userService.getUserByUsername(mockUser.getUsername())).thenReturn(mockUser);

        when(keycloakService.authenticate(anyString(), anyString()))
                .thenReturn(mockRes);

        LoginDTO loginDTO = new LoginDTO(mockUser.getUsername(), "passwordTest");

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
    void registerProviderTest() throws Exception {
        User mockUser = new User("usernameTest", "nameTest", 20, "email@test.com", 1234567890L,
                "descriptionTest", "usernameTest", "jpg", "www.google.com", UserType.PROVIDER, List.of());

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "profile-picture.jpg",
                "image/jpeg",
                "Fake image content".getBytes());

        AccessTokenResponse mockRes = new AccessTokenResponse();
        mockRes.setToken("fake_token");
        mockRes.setTokenType("Bearer");
        mockRes.setRefreshToken("fake_refresh_token");
        mockRes.setOtherClaims("role", UserType.CLIENT.toString());

        when(userService.createUser(any(User.class))).thenReturn(mockUser);

        when(keycloakService.authenticate(anyString(), anyString()))
                .thenReturn(mockRes);

        doNothing().when(minioService).uploadFile(mockUser.getPhoto(), photo);

        mockMvc.perform(multipart("/auth/provider/register")
                .file(photo)
                .param("username", mockUser.getUsername())
                .param("name", mockUser.getName())
                .param("age", mockUser.getAge().toString())
                .param("email", mockUser.getEmail())
                .param("password", "passwordTest")
                .param("description", mockUser.getDescription())
                .param("webPage", mockUser.getWebPage())
                .param("phone", mockUser.getPhone().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("fake_token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.refresh_token").value("fake_refresh_token"))
                .andExpect(jsonPath("$.role").value(UserType.PROVIDER.toString()));
    }

    @Test
    void loginProviderTest() throws Exception {
        User mockUser = new User("usernameTest", "nameTest", 20, "email@test.com", 1234567890L,
                "descriptionTest", "usernameTest", "jpg", "www.google.com", UserType.PROVIDER, List.of());

        AccessTokenResponse mockRes = new AccessTokenResponse();
        mockRes.setToken("fake_token");
        mockRes.setTokenType("Bearer");
        mockRes.setRefreshToken("fake_refresh_token");
        mockRes.setOtherClaims("role", UserType.PROVIDER.toString());

        when(userService.getUserByUsername(mockUser.getUsername())).thenReturn(mockUser);

        when(keycloakService.authenticate(anyString(), anyString()))
                .thenReturn(mockRes);

        LoginDTO loginDTO = new LoginDTO(mockUser.getUsername(), "passwordTest");

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
