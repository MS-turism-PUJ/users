package com.turism.users.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.turism.users.dtos.RegisterClientDTO;
import com.turism.users.dtos.RegisterProviderDTO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Order(1)
    @Test
    void registerClientTest() throws Exception {
        RegisterClientDTO registerClientDTO = new RegisterClientDTO("usernameTest", "nameTest", 20, "emailTest", "passwordTest", null, null, null);
        this.mockMvc.perform(post("/auth/client/register", registerClientDTO)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("access_token")));
    }

    @Order(2)
    @Test
    void loginClientTest() throws Exception {
        this.mockMvc.perform(post("/auth/login", "usernameTest", "passwordTest")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("access_token")));
    }

    @Order(3)
    @Test
    void registerProviderTest() throws Exception {
        RegisterProviderDTO registerProviderDTO = new RegisterProviderDTO("usernameTest", "nameTest", 20, 1234567891L, "emailTest", "passwordTest", "https://google.com", null, null, null);
        this.mockMvc.perform(post("/auth/provider/register", registerProviderDTO)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("access_token")));
    }

    @Order(4)
    @Test
    void loginProviderTest() throws Exception {
        this.mockMvc.perform(post("/auth/login", "usernameTest", "passwordTest")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("access_token")));
    }

}
