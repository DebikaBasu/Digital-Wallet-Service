package com.debika.payments.wallet.service.impl;

import java.util.UUID;
import com.debika.payments.wallet.exception.DuplicateResourceException;
import com.debika.payments.wallet.model.User;
import com.debika.payments.wallet.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        UUID id = UUID.randomUUID();
        User savedUser = new User(id, "testuser", "test@example.com", null);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(savedUser);

        // When
        User result = userService.createUser(user);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowWhenDuplicateUsername() {
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("new@example.com");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(user));
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowWhenDuplicateEmail() {
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("existing@example.com");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(user));
        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}