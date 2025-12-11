-- V6: matches and leaderboard
CREATE TABLE IF NOT EXISTS matches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  started_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  ended_at TIMESTAMP WITH TIME ZONE,
  winner UUID,
  pot BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS match_players (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  match_id UUID NOT NULL,
  user_id UUID NOT NULL,
  rank INT,
  score BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_match_players_user ON match_players(user_id);
