-- Drop old indexes that are being replaced
DROP INDEX IF EXISTS idx_transactions_ip;
DROP INDEX IF EXISTS idx_transactions_status;

-- Merchant — already correct, ensure DESC for ORDER BY queries
DROP INDEX IF EXISTS idx_transactions_merchant_time;
CREATE INDEX idx_transactions_merchant_time
    ON transactions(merchant_id, created_at DESC);

-- Card — already correct, ensure DESC
DROP INDEX IF EXISTS idx_transactions_card_time;
CREATE INDEX idx_transactions_card_time
    ON transactions(card_hash, created_at DESC);

-- IP — add created_at which was missing
CREATE INDEX idx_transactions_ip_time
    ON transactions(ip_address, created_at DESC);

-- Status — partial index, much smaller and faster
DROP INDEX IF EXISTS idx_transactions_status_time;
CREATE INDEX idx_transactions_status_time
    ON transactions(status, created_at DESC)
    WHERE status IN ('FRAUD', 'REVIEW');