# Phase 4 — Room System

This document describes the Room System implemented in Phase 4.

## Key Concepts

- Room: A game room with metadata (name, game type, host, max players, status).
- RoomSeat: Seat entries for a room (position 0..N-1) containing optional player assignment and ready flag.
- Spectator: Users who watch without occupying a seat.
- Redis channels: `room:{roomId}:updates`, `room:{roomId}:seats`, `room:{roomId}:player-list` for broadcasting.

## REST APIs

Base: `POST /api/rooms` create room (authenticated)
GET `/api/rooms` list with pagination and filters
GET `/api/rooms/{roomId}` details
POST `/api/rooms/{roomId}/join` join
POST `/api/rooms/{roomId}/leave` leave
POST `/api/rooms/{roomId}/ready?ready=true` mark ready
POST `/api/rooms/{roomId}/spectators` add spectator
DELETE `/api/rooms/{roomId}/spectators` remove spectator
GET `/api/rooms/{roomId}/spectators` list spectators

## WebSocket

Clients subscribe to `/topic/room/{roomId}/updates`, `/topic/room/{roomId}/seats`, `/topic/room/{roomId}/player-list` to receive real-time updates.

## Database

New Flyway migration `V3__add_rooms_table.sql` creates `rooms`, `room_seats`, `room_spectators` tables.

## Cleanup

`RoomCleanupTask` deletes `FINISHED` rooms older than 30 minutes every 5 minutes.

## Notes

- Room state is cached in Redis under key `room:{roomId}` to speed up reads.
- Room events are published to Redis to support multi-instance deployments.

