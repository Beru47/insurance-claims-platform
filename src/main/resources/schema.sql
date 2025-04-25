CREATE TABLE IF NOT EXISTS user (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS clients (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nom VARCHAR(255),
    prenom VARCHAR(255),
    cin VARCHAR(255) NOT NULL UNIQUE,
    date_naissance DATE,
    email VARCHAR(255),
    telephone VARCHAR(255),
    adresse VARCHAR(255),
    nationalite VARCHAR(255),
    sexe VARCHAR(255),
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id)
    );

CREATE TABLE IF NOT EXISTS policies (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        numero_police VARCHAR(255) UNIQUE,
    type_assurance VARCHAR(255),
    date_debut DATETIME,
    date_fin DATETIME,
    valid BOOLEAN,
    client_id BIGINT,
    FOREIGN KEY (client_id) REFERENCES clients(id)
    );

CREATE TABLE IF NOT EXISTS policy_guarantees (
                                                 policy_id BIGINT,
                                                 guarantees VARCHAR(255),
    FOREIGN KEY (policy_id) REFERENCES policies(id)
    );

CREATE TABLE IF NOT EXISTS assets (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      type VARCHAR(255),
    identifier VARCHAR(255),
    description VARCHAR(255),
    policy_id BIGINT,
    FOREIGN KEY (policy_id) REFERENCES policies(id)
    );

CREATE TABLE IF NOT EXISTS experts (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nom VARCHAR(255),
    specialite VARCHAR(255),
    contact VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS admins (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      nom VARCHAR(255),
    email VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS sinistres (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         type VARCHAR(255),
    date DATETIME,
    lieu VARCHAR(255),
    description VARCHAR(255),
    status VARCHAR(255),
    priority_score INT,
    policy_id BIGINT,
    expert_id BIGINT,
    admin_id BIGINT,
    montant_indemnisation DECIMAL(19,2),
    numero_sinistre VARCHAR(255) UNIQUE,
    FOREIGN KEY (policy_id) REFERENCES policies(id),
    FOREIGN KEY (expert_id) REFERENCES experts(id),
    FOREIGN KEY (admin_id) REFERENCES admins(id)
    );

CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL UNIQUE
    );

INSERT INTO roles (name) VALUES ('ROLE_CLIENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_EXPERT') ON CONFLICT (name) DO NOTHING;