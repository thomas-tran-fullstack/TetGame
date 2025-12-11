-- V5: tasks, user_tasks, wheel_spins
CREATE TABLE IF NOT EXISTS tasks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  task_key VARCHAR(100) NOT NULL UNIQUE,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  reward BIGINT DEFAULT 0,
  repeat_daily BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_tasks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  task_key VARCHAR(100) NOT NULL,
  completed BOOLEAN DEFAULT FALSE,
  progress INT DEFAULT 0,
  completed_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS wheel_spins (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  prize VARCHAR(255),
  amount BIGINT DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- seed a few tasks (if not exists)
INSERT INTO tasks (task_key, title, description, reward, repeat_daily)
VALUES
('play_5_games','Chơi 5 ván','Chơi 5 ván trong ngày để nhận tiền thưởng',1000, true),
('win_1_game','Thắng 1 ván','Thắng 1 ván để nhận thưởng',500, true)
ON CONFLICT (task_key) DO NOTHING;
