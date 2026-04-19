# API Documentation

## Base URL
`http://localhost:8080`

## Authentication
Protected endpoints require:
`Authorization: Bearer <JWT_TOKEN>`

## Auth APIs

### Signup
- **POST** `/api/v1/auth/signup`

Request:
```json
{
  "fullName": "Emmanuel Amedu",
  "email": "emmanuel@test.com",
  "phoneNumber": "08012345678",
  "password": "password123",
  "bvnOrNin": "12345678901"
}
```

### Login
- **POST** `/api/v1/auth/login`

Request:
```json
{
  "email": "emmanuel@test.com",
  "password": "password123"
}
```

### Forgot Password
- **POST** `/api/v1/auth/forgot-password`

Request:
```json
{
  "email": "emmanuel@test.com"
}
```

### Resend OTP
- **POST** `/api/v1/auth/resend-otp`

Request:
```json
{
  "email": "emmanuel@test.com"
}
```

### Reset Password
- **POST** `/api/v1/auth/reset-password`

Request:
```json
{
  "email": "emmanuel@test.com",
  "otpCode": "123456",
  "newPassword": "newpassword123"
}
```

### Logout
- **POST** `/api/v1/auth/logout`

Header:
`Authorization: Bearer <JWT_TOKEN>`

## User APIs

### Get Current User
- **GET** `/api/v1/users/me`

### Update Current User
- **PUT** `/api/v1/users/me`

Request:
```json
{
  "fullName": "Emmanuel Amedu Updated",
  "phoneNumber": "08012345679",
  "bvnOrNin": "12345678901"
}
```

## Wallet APIs

### Get Wallet
- **GET** `/api/v1/wallet`

### Initiate Wallet Funding
- **POST** `/api/v1/wallet/fund`

Request:
```json
{
  "amount": 5000
}
```

Response includes pending transaction reference for webhook confirmation.

### Get Wallet Transactions
- **GET** `/api/v1/wallet/transactions`

## Transaction APIs

### Get All Transactions
- **GET** `/api/v1/transactions`

### Get Single Transaction
- **GET** `/api/v1/transactions/{transactionId}`

## Loan APIs

### Apply for Loan
- **POST** `/api/v1/loans`

Request:
```json
{
  "loanAmount": 10000,
  "loanDurationDays": 30
}
```

### Get My Loans
- **GET** `/api/v1/loans`

### Get One Loan
- **GET** `/api/v1/loans/{loanId}`

### Approve Loan
- **POST** `/api/v1/loans/{loanId}/approve`

### Disburse Loan
- **POST** `/api/v1/loans/{loanId}/disburse`

### Repay Loan
- **POST** `/api/v1/loans/{loanId}/repay`

Request:
```json
{
  "amount": 2000
}
```

## Webhook API

### Confirm Payment
- **POST** `/api/v1/webhooks/payments`

Request:
```json
{
  "reference": "FUND-XXXXXXXXXXXX",
  "status": "success",
  "amount": 5000
}
```

## Response Format
All APIs return:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

## Swagger UI
Open:
`http://localhost:8080/swagger-ui/index.html`
