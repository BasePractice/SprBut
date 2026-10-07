CREATE TABLE account (
    id UUID PRIMARY KEY,
    login VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE session (
    id UUID PRIMARY KEY,
    account UUID NOT NULL REFERENCES account (id) ON DELETE CASCADE,
    created TIMESTAMPTZ NOT NULL,
    expires TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL
);

CREATE INDEX session_account ON session (account);

CREATE TABLE refresh (
    hash CHAR(64) PRIMARY KEY,
    session UUID NOT NULL REFERENCES session (id) ON DELETE CASCADE,
    used BOOLEAN NOT NULL,
    expires TIMESTAMPTZ NOT NULL
);
