package com.debika.payments.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.debika.payments.wallet.dto.CreateUserRequest;
import com.debika.payments.wallet.exception.DuplicateResourceException;
import com.debika.payments.wallet.exception.GlobalExceptionHandler;
import com.debika.payments.wallet.model.User;
import com.debika.payments.wallet.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create user")
    void shouldCreateUser() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("testuser", "test@example.com");

        User createdUser = new User(UUID.randomUUID(), "testuser", "test@example.com", null);
        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should return 409 when username already exists")
    void shouldReturn409WhenDuplicateUsername() throws Exception {
        CreateUserRequest request = new CreateUserRequest("existinguser", "new@example.com");
        when(userService.createUser(any(User.class)))
                .thenThrow(new DuplicateResourceException("Username already exists"));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username already exists"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void shouldReturn409WhenDuplicateEmail() throws Exception {
        CreateUserRequest request = new CreateUserRequest("newuser", "existing@example.com");
        when(userService.createUser(any(User.class)))
                .thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should return 400 when username is blank")
    void shouldReturn400WhenUsernameBlank() throws Exception {
        CreateUserRequest request = new CreateUserRequest("", "test@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should return 400 when email is blank")
    void shouldReturn400WhenEmailBlank() throws Exception {
        CreateUserRequest request = new CreateUserRequest("testuser", "");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid")
    void shouldReturn400WhenEmailInvalid() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("testuser", "not-an-email");

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }
}