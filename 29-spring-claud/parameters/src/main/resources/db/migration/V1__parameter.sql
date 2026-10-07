CREATE TABLE parameter (
    owner VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    value TEXT NOT NULL,
    PRIMARY KEY (owner, name)
);
