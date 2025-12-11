import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import apiService from '../services/api';
import websocketService from '../services/websocket';
import type { RoomResponse } from '../types';
import './RoomList.css';

export default function RoomList() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [rooms, setRooms] = useState<RoomResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [joinRoomId, setJoinRoomId] = useState('');
  const [roomPassword, setRoomPassword] = useState('');

  const gameType = searchParams.get('game') || 'TIEN_LEN';

  useEffect(() => {
    loadRooms();
    // Subscribe to room updates
    websocketService.subscribe('/topic/lobby', () => {
      loadRooms();
    });
  }, [gameType]);

  const loadRooms = async () => {
    setLoading(true);
    try {
      const response = await apiService.getRooms(0, 20);
      setRooms(response.content || []);
      setError('');
    } catch (err) {
      setError('Failed to load rooms');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleJoinRoom = async (roomId: string) => {
    setJoinRoomId(roomId);
    setShowJoinModal(true);
  };

  const handleConfirmJoin = async () => {
    try {
      const response = await apiService.joinRoom(joinRoomId);
      navigate(`/rooms/${response.id}`);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to join room');
    } finally {
      setShowJoinModal(false);
      setRoomPassword('');
    }
  };

  const handleCreateRoom = () => {
    navigate('/lobby');
  };

  if (loading) {
    return (
      <div className="roomlist-container">
        <p className="loading">⏳ Đang tải phòng chơi...</p>
      </div>
    );
  }

  return (
    <div className="roomlist-container">
      <div className="roomlist-header">
        <h1>Danh Sách Phòng Chơi</h1>
        <button className="btn-create-room" onClick={handleCreateRoom}>
          ➕ Tạo Phòng
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {rooms.length === 0 ? (
        <div className="empty-state">
          <p>😴 Chưa có phòng chơi nào</p>
          <button className="btn-create-new" onClick={handleCreateRoom}>
            Tạo phòng chơi đầu tiên
          </button>
        </div>
      ) : (
        <div className="rooms-grid">
          {rooms.map((room) => (
            <div key={room.id} className="room-card">
              <div className="room-header">
                <h3 className="room-name">{room.name}</h3>
                <span className={`room-status ${room.status.toLowerCase()}`}>
                  {room.status === 'WAITING' ? '⏳ Chờ' : '🎮 Chơi'}
                </span>
              </div>

              <div className="room-info">
                <p className="room-detail">
                  💰 <strong>{room.betLevel}</strong>
                </p>
                <p className="room-detail">
                  👥 <strong>{room.currentPlayers}/{room.maxPlayers}</strong> người
                </p>
              </div>

              <div className="room-players">
                {room.seats?.map((seat, idx) => (
                  <div
                    key={idx}
                    className={`seat ${seat.playerId ? 'occupied' : 'empty'}`}
                    title={seat.playerId ? `Ghế ${idx + 1}` : `Trống`}
                  >
                    {seat.playerId ? '👤' : '❌'}
                  </div>
                ))}
              </div>

              <button
                className={`btn-join ${room.currentPlayers >= room.maxPlayers ? 'disabled' : ''}`}
                onClick={() => handleJoinRoom(room.id)}
                disabled={room.currentPlayers >= room.maxPlayers}
              >
                {room.currentPlayers >= room.maxPlayers ? '🚫 Full' : '✅ Vào'}
              </button>
            </div>
          ))}
        </div>
      )}

      {showJoinModal && (
        <div className="modal-overlay">
          <div className="modal-dialog">
            <h3>Vào Phòng Chơi</h3>
            {roomPassword !== null && (
              <div className="form-group">
                <label>Mật Khẩu Phòng (nếu có)</label>
                <input
                  type="password"
                  placeholder="Nhập mật khẩu"
                  value={roomPassword}
                  onChange={(e) => setRoomPassword(e.target.value)}
                />
              </div>
            )}
            <div className="modal-actions">
              <button className="btn-submit" onClick={handleConfirmJoin}>
                ✅ Vào
              </button>
              <button className="btn-cancel" onClick={() => setShowJoinModal(false)}>
                ❌ Hủy
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
