-- V3__reset_transactions_table.sql

-- Clear all existing data
TRUNCATE TABLE transactions RESTART IDENTITY;

-- Optional: Add comment
COMMENT ON TABLE transactions IS 'Transaction logs from sidecar service - Reset on 2026-04-28';

-- Re-apply important indexes (in case they were dropped)
CREATE INDEX IF NOT EXISTS idx_transactions_merchant_time
    ON transactions(merchant_id, created_at);

CREATE INDEX IF NOT EXISTS idx_transactions_card_time
    ON transactions(card_hash, created_at);

CREATE INDEX IF NOT EXISTS idx_transactions_ip
    ON transactions(ip_address);

CREATE INDEX IF NOT EXISTS idx_transactions_status
    ON transactions(status);