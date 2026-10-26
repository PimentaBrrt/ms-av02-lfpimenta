DROP TABLE IF EXISTS login_attempts;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE authorities (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR_IGNORECASE(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username)
);

CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);

CREATE TABLE login_attempts (
    username VARCHAR(100) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_failed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuarios (
    username VARCHAR(100) PRIMARY KEY,
    senha VARCHAR(10)
);

INSERT INTO usuarios (username, senha) VALUES ('admin', 'admin');
INSERT INTO usuarios (username, senha) VALUES ('admin2', 'admin2');
INSERT INTO usuarios (username, senha) VALUES ('manutencao', 'manut');
INSERT INTO usuarios (username, senha) VALUES ('maria', 'maria');

INSERT INTO users (username, password, enabled) VALUES ('admin', '{noop}admin', TRUE);
INSERT INTO users (username, password, enabled) VALUES ('admin2', '{noop}admin2', TRUE);
INSERT INTO users (username, password, enabled) VALUES ('manutencao', '{noop}manut', TRUE);
INSERT INTO users (username, password, enabled) VALUES ('maria', '{noop}maria', TRUE);

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('admin2', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('manutencao', 'ROLE_MANUTENCAO');
INSERT INTO authorities (username, authority) VALUES ('maria', 'ROLE_USUARIO');
