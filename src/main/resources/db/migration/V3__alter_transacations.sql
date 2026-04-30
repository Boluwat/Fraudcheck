ALTER TABLE transactions
    RENAME COLUMN masked_card_number TO card_hash;

ALTER TABLE transactions
    ALTER COLUMN card_hash TYPE VARCHAR(100);   -- or TEXT