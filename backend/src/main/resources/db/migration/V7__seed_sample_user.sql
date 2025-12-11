-- V7: seed a sample local user for testing
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser') THEN
    INSERT INTO users (id, username, password, email, full_name, enabled, created_at, updated_at)
    VALUES (
      '11111111-1111-1111-1111-111111111111',
      'testuser',
      -- BCrypt hash for Passw0rd! (cost 10)
      '$2a$10$wzQK5ZyqY0cJj8GmZ2zZBeR0Jv5mJb7vQKx2Ylq3Qf1sV0LZKq9pG',
      'testuser@example.com',
      'Test User',
      true,
      now(), now()
    );

    INSERT INTO wallets (id, user_id, balance, updated_at)
    VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 100000, now());
  END IF;
END $$;

-- Add second demo account
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'player2') THEN
    INSERT INTO users (id, username, password, email, full_name, enabled, created_at, updated_at)
    VALUES (
      '33333333-3333-3333-3333-333333333333',
      'player2',
      -- BCrypt hash for Passw0rd! (cost 10)
      '$2a$10$wzQK5ZyqY0cJj8GmZ2zZBeR0Jv5mJb7vQKx2Ylq3Qf1sV0LZKq9pG',
      'player2@example.com',
      'Player Two',
      true,
      now(), now()
    );

    INSERT INTO wallets (id, user_id, balance, updated_at)
    VALUES ('44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', 100000, now());
  END IF;
END $$;

-- Note: password above corresponds to plaintext: Passw0rd!
