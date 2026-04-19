# Loan Wallet System (Spring Boot Fintech Backend)

## Overview
Loan Wallet System is a fintech backend application built with Spring Boot that enables users to manage wallets, perform transactions, apply for loans, and simulate payment confirmations via webhook integration.

The system implements secure authentication, business rules for lending, and a transaction-based wallet system aligned with real-world fintech architecture.

## Features

### Authentication & Security
- User signup and login
- JWT-based authentication
- Password reset with OTP verification
- Token blacklist for logout

### User & Profile
- Retrieve user profile
- Update user profile

### Wallet
- Wallet creation on signup
- View wallet balance
- Initiate wallet funding (creates pending transaction)
- Wallet credited only after webhook confirmation

### Transactions
- View all user transactions
- View single transaction
- Tracks funding, loan disbursement, and repayment

### Loan Management
- Apply for loan (based on wallet balance)
- Approve loan
- Disburse loan (credits wallet)
- Repay loan (partial or full)
- View loans

### Webhook (Payment Simulation)
- Simulates payment gateway callback
- Confirms transactions and updates wallet

## Tech Stack
- Java 17
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- Maven
- Mockito & JUnit

## Project Structure
```text
com.koins.loanwallet
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── enums
├── exception
├── repository
├── security
├── service
│   └── impl
├── util
```

## Database Setup
1. Install PostgreSQL
2. Create database:
```sql
CREATE DATABASE loan_wallet_db;
```

3. Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/loan_wallet_db
    username: postgres
    password: your_password
```

Flyway will automatically create tables on application startup.

## Running the Application
```bash
mvn clean install
mvn spring-boot:run
```

Application runs on:
```text
http://localhost:8080
```

## Swagger UI
```text
http://localhost:8080/swagger-ui/index.html
```

## Authentication
All protected endpoints require:
```text
Authorization: Bearer <JWT_TOKEN>
```

## API Endpoints

### Auth
- POST `/api/v1/auth/signup`
- POST `/api/v1/auth/login`
- POST `/api/v1/auth/forgot-password`
- POST `/api/v1/auth/resend-otp`
- POST `/api/v1/auth/reset-password`
- POST `/api/v1/auth/logout`

### User
- GET `/api/v1/users/me`
- PUT `/api/v1/users/me`

### Wallet
- GET `/api/v1/wallet`
- POST `/api/v1/wallet/fund`
- GET `/api/v1/wallet/transactions`

### Transactions
- GET `/api/v1/transactions`
- GET `/api/v1/transactions/{transactionId}`

### Loans
- POST `/api/v1/loans`
- GET `/api/v1/loans`
- GET `/api/v1/loans/{loanId}`
- POST `/api/v1/loans/{loanId}/approve`
- POST `/api/v1/loans/{loanId}/disburse`
- POST `/api/v1/loans/{loanId}/repay`

### Webhook
- POST `/api/v1/webhooks/payments`

## Sample Flow

### Signup
```json
{
  "fullName": "Emmanuel Amedu",
  "email": "emmanuel@test.com",
  "phoneNumber": "08012345678",
  "password": "password123",
  "bvnOrNin": "12345678901"
}
```

### Fund Wallet
```json
{
  "amount": 5000
}
```

### Webhook Confirmation
```json
{
  "reference": "FUND-XXXX",
  "status": "success",
  "amount": 5000
}
```

### Apply for Loan
```json
{
  "loanAmount": 10000,
  "loanDurationDays": 30
}
```

## Business Rules
- Wallet must be funded before loan application
- Loan amount must not exceed 3× wallet balance
- Loan must be approved before disbursement
- Repayment reduces wallet balance
- Webhook confirms payment before wallet credit

## Unit Tests
Run:
```bash
mvn test
```

## Future Improvements
- Admin-only approval/disbursement
- Real payment gateway integration
- Scheduler for overdue/defaulted loans
- Dockerization

## Author
Emmanuel Amedu
## Usage Notice
This project is submitted as part of a technical assessment.
It is not licensed for commercial or production use without explicit permission.
