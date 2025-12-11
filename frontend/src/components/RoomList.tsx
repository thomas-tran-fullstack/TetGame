import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import { useRoomStore } from '../store';
import type { RoomResponse } from '../types';
import { BetLevel } from '../types';
import './RoomList.css';

export default function RoomList() {
  const navigate = useNavigate();
  const { rooms, setRooms } = useRoomStore();
  const [isCreating, setIsCreating] = useState(false);
  const [roomName, setRoomName] = useState('');
  const [betLevel, setBetLevel] = useState<BetLevel>(BetLevel.BAN1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadRooms();
  }, []);

  const loadRooms = async () => {
    try {
      setLoading(true);
      const data = await apiService.getRooms(0, 10);
      setRooms(data.content);
    } catch (error) {
      console.error('Failed to load rooms:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRoom = async () => {
    if (!roomName.trim()) {
      alert('Tên phòng không được trống');
      return;
    }

    try {
      const newRoom = await apiService.createRoom(roomName, betLevel);
      setRooms([...rooms, newRoom]);
      setIsCreating(false);
      setRoomName('');
      setBetLevel(BetLevel.BAN1);
    } catch (error) {
      console.error('Failed to create room:', error);
      alert('Tạo phòng thất bại');
    }
  };

  const handleJoinRoom = async (roomId: string) => {
    try {
      await apiService.joinRoom(roomId);
      navigate(`/game/${roomId}`);
    } catch (error) {
      console.error('Failed to join room:', error);
      alert('Tham gia phòng thất bại');
    }
  };

  const betLevelInfo = {
    BAN1: '5k-10k',
    BAN2: '10k-20k',
    BAN3: '50k-100k',
    BAN4: '100k-200k',
    BAN5: '500k-1m',
  };

  return (
    <div className="room-list-container">
      <div className="room-list-header">
        <h1>🃏 Phòng Chơi Tiến Lên</h1>
        <button className="btn-create" onClick={() => setIsCreating(true)}>
          + Tạo Phòng Mới
        </button>
      </div>

      {isCreating && (
        <div className="create-room-modal">
          <div className="create-room-form">
            <h2>Tạo Phòng Chơi</h2>
            <input
              type="text"
              placeholder="Tên phòng"
              value={roomName}
              onChange={(e) => setRoomName(e.target.value)}
              className="input"
            />
            <select
              value={betLevel}
              onChange={(e) => setBetLevel(e.target.value as BetLevel)}
              className="input"
            >
              <option value="BAN1">BAN1 (5k-10k)</option>
              <option value="BAN2">BAN2 (10k-20k)</option>
              <option value="BAN3">BAN3 (50k-100k)</option>
              <option value="BAN4">BAN4 (100k-200k)</option>
              <option value="BAN5">BAN5 (500k-1m)</option>
            </select>
            <div className="modal-buttons">
              <button className="btn-primary" onClick={handleCreateRoom}>
                Tạo
              </button>
              <button className="btn-secondary" onClick={() => setIsCreating(false)}>
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="loading">Đang tải phòng...</div>
      ) : rooms.length === 0 ? (
        <div className="no-rooms">
          <p>Chưa có phòng nào. Hãy tạo một phòng mới!</p>
        </div>
      ) : (
        <div className="rooms-grid">
          {rooms.map((room) => (
            <div key={room.id} className="room-card">
              <div className="room-header">
                <h3>{room.name}</h3>
                <span className={`bet-level ${room.betLevel.toLowerCase()}`}>
                  {betLevelInfo[room.betLevel]}
                </span>
              </div>
              <div className="room-info">
                <p>👥 {room.currentPlayers}/{room.maxPlayers} người chơi</p>
                <p>🎰 {room.status === 'WAITING' ? 'Chờ đủ người' : 'Đang chơi'}</p>
                <p>👤 Chủ phòng: {room.hostId}</p>
              </div>
              <button
                className="btn-join"
                onClick={() => handleJoinRoom(room.id)}
                disabled={room.status !== 'WAITING' || room.currentPlayers >= room.maxPlayers}
              >
                {room.status !== 'WAITING' ? 'Đang chơi' : 'Tham gia'}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
