-- Drop existing tables if they exist
DROP TABLE IF EXISTS merchant_mapping;
DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS benefit_category;

-- Create tables
CREATE TABLE benefit_category (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Identificador único para cada categoria, chave primária
                                  mcc VARCHAR(255),                     -- Merchant Category Code, pode ser nulo
                                  balance DOUBLE NOT NULL,              -- Saldo da categoria, não nulo
                                  category VARCHAR(255) NOT NULL        -- Nome da categoria, não nulo
);
CREATE TABLE merchant_mapping (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Identificador único para cada mapeamento, chave primária
                                  merchant VARCHAR(255) NOT NULL,       -- Nome do comerciante, não nulo
                                  corrected_mcc VARCHAR(255)            -- MCC corrigido, pode ser nulo
);

-- Table `transaction`
CREATE TABLE transaction (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             mcc VARCHAR(255),
                             amount DOUBLE,
                             merchant VARCHAR(255)
);


-- Insert initial data into benefit_category
INSERT INTO benefit_category (mcc, balance, category) VALUES ('5411', 100.0, 'FOOD');
INSERT INTO benefit_category (mcc, balance, category) VALUES (NULL, 200.0, 'CASH');
INSERT INTO benefit_category (mcc, balance, category) VALUES ('5811', 300.0, 'MEAL');

-- Insert initial data into transaction table for example purposes
INSERT INTO transaction (mcc, amount, merchant) VALUES ('5411', 50.0, 'Grocery Store');
INSERT INTO transaction (mcc, amount, merchant) VALUES ('5812', 150.0, 'Restaurant');
INSERT INTO transaction (mcc, amount, merchant) VALUES ('9999', 200.0, 'Unknown Merchant');

-- Insert initial data into merchant_mapping
INSERT INTO merchant_mapping (merchant, corrected_mcc) VALUES ('Grocery Store', '5411');
INSERT INTO merchant_mapping (merchant, corrected_mcc) VALUES ('Restaurant', '5812');
INSERT INTO merchant_mapping (merchant, corrected_mcc) VALUES ('ATM', '6011');