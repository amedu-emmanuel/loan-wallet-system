CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(120) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    bvn_or_nin VARCHAR(30) NOT NULL,
    account_status VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_phone_number UNIQUE (phone_number)
);

CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_wallet_user UNIQUE (user_id)
);

CREATE TABLE loans (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    loan_amount NUMERIC(19,2) NOT NULL,
    interest_rate NUMERIC(5,2) NOT NULL,
    loan_duration_days INTEGER NOT NULL,
    loan_status VARCHAR(20) NOT NULL,
    repayment_schedule TEXT,
    total_repayable_amount NUMERIC(19,2) NOT NULL,
    amount_repaid NUMERIC(19,2) NOT NULL,
    due_date DATE,
    disbursed_at TIMESTAMP,
    repaid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    transaction_status VARCHAR(20) NOT NULL,
    reference_number VARCHAR(50) NOT NULL,
    transaction_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    CONSTRAINT uk_transaction_reference UNIQUE (reference_number)
);

CREATE TABLE otps (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE token_blacklist (
    id UUID PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    blacklisted_at TIMESTAMP NOT NULL
);