-- Master data for customers (identity/profile)
CREATE TABLE IF NOT EXISTS customers (
                                         id UUID PRIMARY KEY,
                                         name VARCHAR(128) NOT NULL,
    email VARCHAR(320) NOT NULL,           -- RFC upper bound
    password_hash VARCHAR(100) NOT NULL,   -- bcrypt ~60 chars
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (email)
    );
