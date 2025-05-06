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
                                         id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                         type VARCHAR(255),
    date TIMESTAMP,
    lieu VARCHAR(255),
    description TEXT,
    fraud_score DOUBLE PRECISION DEFAULT 0.0,
    status VARCHAR(50),
    priority_score INTEGER,
    policy_id BIGINT,
    expert_id BIGINT,
    admin_id BIGINT,
    montant_indemnisation DECIMAL,
    numero_sinistre VARCHAR(255) UNIQUE,
    vehicle_type VARCHAR(255),
    vehicle_make VARCHAR(255),
    vehicle_model VARCHAR(255),
    vehicle_year VARCHAR(255),
    vin VARCHAR(255),
    accident_type VARCHAR(255),
    third_party_involved BOOLEAN,
    police_report_number VARCHAR(255),
    property_address VARCHAR(255),
    damage_type VARCHAR(255),
    damage_extent VARCHAR(255),
    emergency_services_called BOOLEAN,
    medical_condition VARCHAR(255),
    treatment_location VARCHAR(255),
    treatment_date TIMESTAMP,
    doctor_name VARCHAR(255),
    medical_bill_amount DOUBLE PRECISION,
    hospitalization_required BOOLEAN,
    property_type VARCHAR(255),
    incident_cause VARCHAR(255),
    property_damage_description TEXT,
    estimated_loss_value DOUBLE PRECISION,
    business_interruption BOOLEAN,
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

CREATE TABLE IF NOT EXISTS media_references (
                                                id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                                file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    sinistre_id BIGINT,
    FOREIGN KEY (sinistre_id) REFERENCES sinistres(id) ON DELETE CASCADE

    CREATE TABLE IF NOT EXISTS sinistre_affected_areas (
                                                           sinistre_id BIGINT,
                                                           affected_area VARCHAR(255),
    FOREIGN KEY (sinistre_id) REFERENCES sinistres(id)
    );