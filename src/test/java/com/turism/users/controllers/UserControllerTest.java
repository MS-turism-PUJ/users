package com.turism.users.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.turism.users.models.User;
import com.turism.users.services.KeycloakService;
import com.turism.users.services.MessageQueueService;
import com.turism.users.services.MinioService;
import com.turism.users.services.UserService;

@WebMvcTest(UserController.class)
@Transactional
public class UserControllerTest {

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
    void uploadPhotoTest() throws Exception {
        String username = "testuser";
        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg",
                "test image content".getBytes());

        when(userService.getUserByUsername(username)).thenReturn(new User());
        doNothing().when(minioService).uploadFile(username, photo);
        when(userService.addPhoto(any(User.class), eq(username), eq("jpg"))).thenReturn(new User());

        mockMvc.perform(multipart("/upload/photo")
                .file(photo)
                .header("X-Preferred-Username", username))
                .andExpect(status().isOk());
    }

    @Test
    void getPhotoTest() throws Exception {
        String username = "testuser";
        User user = new User();
        user.setPhoto("photo.jpg");
        user.setPhotoExtension("jpg");

        when(userService.getUserByUsername(username)).thenReturn(user);
        when(minioService.getObject(user.getPhoto())).thenReturn(new ByteArrayInputStream("test image content".getBytes()));

        mockMvc.perform(get("/photo")
                .header("X-Preferred-Username", username))
                .andExpect(status().isOk());
    }
}
