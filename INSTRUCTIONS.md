# Digital Wallet Microservice

This project is a Spring Boot–based microservice that I am building to simulate a digital wallet system within a payments platform. It started from a basic project structure and has been extended to include core wallet operations, robust validation, and production-oriented design practices.

The goal of this project is to demonstrate my ability to design and develop scalable backend services, implement transactional workflows, and follow clean architecture principles in a real-world fintech-like system.

### Getting Started


1. Open the project in IntelliJ IDEA (or your preferred IDE supporting Spring Boot and Gradle).
2. Verify setup:
    - Run `./gradlew test` to execute existing tests.
    - Start the full stack with `docker-compose up --build` (requires Docker).
    - Access the app at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui.html`.
3. Initialize Git if needed: `git init` (though history is included) and ensure commits are tracked.


#### User Stories

1. **User Registration**  
   As a new customer, I want to register an account, so that I can use the wallet service.  
   **Acceptance Criteria**:
    - POST /users with {username, email} succeeds with 201 Created.
    - Duplicate username or email returns 409 Conflict.
    - Invalid fields return 400 Bad Request.

2. **Wallet Creation**  
   As a registered user, I want to create a digital wallet, so that I can hold funds.  
   **Acceptance Criteria**:
    - POST /wallets with {userId} (UUID) succeeds with 201 Created and balance = 0.
    - User not found returns 404.
    - User already has wallet returns 400 Bad Request.

3. **Deposit Funds**  
   As a wallet owner, I want to deposit funds, so that my balance increases.  
   **Acceptance Criteria**:
    - POST /wallets/{id}/deposit with {amount > 0} updates balance atomically.
    - Transaction record created (type=DEPOSIT).
    - Invalid amount returns 400.
    - Success returns updated wallet.

4. **Withdraw Funds**  
   As a wallet owner, I want to withdraw funds, so that my balance decreases.  
   **Acceptance Criteria**:
    - POST /wallets/{id}/withdraw with {amount > 0} succeeds only if sufficient balance.
    - Insufficient funds returns 400, no change to balance.
    - Transaction record created (type=WITHDRAWAL).
    - Balance never goes negative.

5. **Peer-to-Peer Transfer**  
   As a wallet owner, I want to transfer funds to another wallet, so that I can pay others.  
   **Acceptance Criteria**:
    - POST /transfers with {fromWalletId, toWalletId, amount > 0} is atomic.
    - Insufficient funds rolls back entirely, returns 400.
    - Two transaction records created (TRANSFER_OUT, TRANSFER_IN).
    - Success returns transfer details.

6. **Balance Inquiry**  
   As a wallet owner, I want to view my balance, so that I can check available funds.  
   **Acceptance Criteria**:
    - GET /wallets/{id}/balance returns current balance.
    - Wallet not found returns 404.

### Best Practices

- Used SOLID principles in service and controller layers.
- Used @Transactional for atomic operations.
- Validated inputs with Jakarta Bean Validation.
- Handled exceptions globally with @ControllerAdvice.
- 100% test coverage for new features using JUnit 5, Mockito, and integration tests with Testcontainers.
- Followed REST conventions: appropriate HTTP methods, status codes, and response bodies.
- Used BigDecimal for monetary values.
- Kept classes focused (Single Responsibility Principle).