package com.debika.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new user")
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username of the user", example = "johndoe")
    private String username;

    @NotBlank(message = "Email is required")
    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    @Email(message = "Invalid email format")
    private String email;
}
