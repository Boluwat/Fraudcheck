CREATE TABLE transactions (
                                            id                  UUID PRIMARY KEY,
                                            merchant_id         VARCHAR(100) NOT NULL,
                                            masked_card_number  VARCHAR(20)  NOT NULL,      -- use masked version
                                            amount              NUMERIC(15,2) NOT NULL,
                                            ip_address          VARCHAR(45)  NOT NULL,
                                            risk_score          INTEGER      NOT NULL,
                                            is_fraudulent       BOOLEAN      NOT NULL DEFAULT FALSE,
                                            created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Important indexes for your fraud velocity queries
CREATE INDEX idx_transactions_merchant_time ON transactions(merchant_id, created_at);
CREATE INDEX idx_transactions_card_time    ON transactions(masked_card_number, created_at);
CREATE INDEX idx_transactions_ip           ON transactions(ip_address);