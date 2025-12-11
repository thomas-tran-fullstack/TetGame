-- V3__add_rooms_table.sql

CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    game_type VARCHAR(50) NOT NULL,
    host_id UUID NOT NULL,
    max_players INTEGER NOT NULL,
    current_players INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_rooms_host_id ON rooms(host_id);
CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_rooms_game_type ON rooms(game_type);
CREATE INDEX idx_rooms_created_at ON rooms(created_at);

CREATE TABLE room_seats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    position INTEGER NOT NULL,
    player_id UUID,
    is_ready BOOLEAN NOT NULL DEFAULT false,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(room_id, position)
);

CREATE INDEX idx_room_seats_room_id ON room_seats(room_id);
CREATE INDEX idx_room_seats_player_id ON room_seats(player_id);
CREATE INDEX idx_room_seats_position ON room_seats(room_id, position);

CREATE TABLE room_spectators (
    room_id UUID NOT NULL,
    spectator_id UUID NOT NULL,
    PRIMARY KEY (room_id, spectator_id),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (spectator_id) REFERENCES users(id) ON DELETE CASCADE
);
