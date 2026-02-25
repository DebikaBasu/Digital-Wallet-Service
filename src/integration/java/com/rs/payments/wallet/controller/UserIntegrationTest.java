package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.BaseIntegrationTest;
import com.rs.payments.wallet.dto.CreateUserRequest;
import com.rs.payments.wallet.exception.DuplicateResourceException;
import com.rs.payments.wallet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserIntegrationTest extends BaseIntegrationTest {

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setup() {
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false; // disable exception throwing
            }
        });
    }

    @Test
    void shouldCreateUser() {
        CreateUserRequest request = new CreateUserRequest("testuser", "test@example.com");

        String url = "http://localhost:" + port + "/users";
        ResponseEntity<User> response = restTemplate.postForEntity(url, request, User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldCreateUserAndIgnoreProvidedId() {
        UUID providedId = UUID.randomUUID();
        String jsonRequest = String.format("{\"id\":\"%s\", \"username\":\"testuser2\", \"email\":\"test2@example.com\"}", providedId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        String url = "http://localhost:" + port + "/users";
        ResponseEntity<User> response = restTemplate.postForEntity(url, entity, User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotEqualTo(providedId);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser2");
    }


    @Test
    void shouldReturnConflictWhenDuplicateEmail() {
        CreateUserRequest first = new CreateUserRequest("user1", "duplicate@example.com");
        CreateUserRequest second = new CreateUserRequest("user2", "duplicate@example.com");

        String url = "http://localhost:" + port + "/users";
        restTemplate.postForEntity(url, first, User.class);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(second),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturnBadRequestWhenEmailInvalid() {
        CreateUserRequest request = new CreateUserRequest("testuser3", "not-an-email");

        String url = "http://localhost:" + port + "/users";

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}