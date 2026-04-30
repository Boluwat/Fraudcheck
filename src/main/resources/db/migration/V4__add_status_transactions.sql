ALTER TABLE transactions
    ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING';


COMMENT ON COLUMN transactions.status IS 'Final decision from fraud-check service';

CREATE INDEX idx_transactions_status ON transactions(status);

CREATE INDEX idx_transactions_status_time ON transactions(status, created_at DESC);