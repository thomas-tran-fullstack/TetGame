-- Add bet_level column to rooms table
ALTER TABLE rooms ADD COLUMN bet_level VARCHAR(20) DEFAULT 'BAN1';

-- Index for bet_level queries
CREATE INDEX idx_rooms_bet_level ON rooms(bet_level);
