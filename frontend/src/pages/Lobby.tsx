import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import './Lobby.css';

const GAME_TYPES = [
  { name: 'Tiến Lên', id: 'TIEN_LEN', icon: '🃏' },
  { name: 'Ba Lá', id: 'BA_LA', icon: '🎴' },
  { name: 'Bài Binh', id: 'BAI_BINH', icon: '🎲' },
  { name: 'Xì Dách', id: 'XI_DACH', icon: '💳' },
  { name: 'Bầu Cua', id: 'BAU_CUA', icon: '🎰' },
  { name: 'Lô Tô', id: 'LO_TO', icon: '🎯' },
];

export default function Lobby() {
  const navigate = useNavigate();
  const [currentGameIndex, setCurrentGameIndex] = useState(0);
  const [dragStartX, setDragStartX] = useState<number | null>(null);
  const [findRoomId, setFindRoomId] = useState('');
  const [showCreateRoom, setShowCreateRoom] = useState(false);
  const [loading, setLoading] = useState(false);

  const currentGame = GAME_TYPES[currentGameIndex];

  const handlePrevGame = () => {
    setCurrentGameIndex((prev) => (prev - 1 + GAME_TYPES.length) % GAME_TYPES.length);
  };

  const handleNextGame = () => {
    setCurrentGameIndex((prev) => (prev + 1) % GAME_TYPES.length);
  };

  const handlePlayGame = () => {
    navigate(`/dashboard/${currentGame.id}`);
  };

  const handleFindRoom = () => {
    if (!findRoomId) return alert('Nhập ID phòng');
    navigate(`/rooms/${findRoomId}`);
  };

  // Drag handlers for carousel
  const onPointerDown = (e: React.PointerEvent) => {
    setDragStartX(e.clientX);
  };

  const onPointerUp = (e: React.PointerEvent) => {
    if (dragStartX == null) return;
    const delta = e.clientX - dragStartX;
    if (Math.abs(delta) > 40) {
      if (delta < 0) handleNextGame(); else handlePrevGame();
    }
    setDragStartX(null);
  };

  const handleCreateRoom = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);

    const formData = new FormData(e.currentTarget);
    const name = formData.get('roomName') as string;
    const betLevel = formData.get('betLevel') as string;

    try {
      const response = await apiService.createRoom(name, betLevel);
      navigate(`/rooms/${response.id}`);
      setShowCreateRoom(false);
    } catch (error) {
      console.error('Failed to create room:', error);
      alert('Lỗi tạo phòng chơi');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="lobby-container">
      <div className="lobby-header">
        <h1 className="lobby-title">🃏 Chọn Trò Chơi</h1>
        <p className="lobby-subtitle">Chọn một trò chơi yêu thích và bắt đầu chơi</p>
      </div>

      <div className="game-selector">
        <button className="nav-btn prev" onClick={handlePrevGame}>
          {'<'}
        </button>

        <div className="game-carousel"
             onPointerDown={onPointerDown}
             onPointerUp={onPointerUp}
        >
          {GAME_TYPES.map((game, index) => (
            <div
              key={game.id}
              className={`game-card ${index === currentGameIndex ? 'active' : ''}`}
            >
              <div className="game-icon">{game.icon}</div>
              <div className="game-name">{game.name}</div>
            </div>
          ))}
        </div>

        <button className="nav-btn next" onClick={handleNextGame}>
          {'>'}
        </button>
      </div>

      <div className="current-game-display">
        <div className="game-showcase">
          <div className="big-icon">{currentGame.icon}</div>
          <h2 className="game-title">{currentGame.name}</h2>
          <p className="game-desc">Trò chơi truyền thống Việt Nam</p>
        </div>
      </div>

      <div className="lobby-actions">
        <button className="btn-play" onClick={handlePlayGame}>
          🎮 Chơi Ngay
        </button>
        <button className="btn-create" onClick={() => setShowCreateRoom(!showCreateRoom)}>
          ➕ Tạo Phòng
        </button>
        <div className="find-room">
          <input
            type="text"
            placeholder="Tìm phòng theo ID"
            value={findRoomId}
            onChange={(e) => setFindRoomId(e.target.value)}
            className="find-input"
          />
          <button className="btn-find" onClick={handleFindRoom}>🔎 Tìm</button>
        </div>
      </div>

      {showCreateRoom && (
        <div className="create-room-modal">
          <div className="modal-content">
            <h3>Tạo Phòng Chơi</h3>
            <form onSubmit={handleCreateRoom}>
              <div className="form-group">
                <label>Tên Phòng</label>
                <input
                  type="text"
                  name="roomName"
                  placeholder="Nhập tên phòng"
                  required
                  minLength={3}
                  maxLength={100}
                />
              </div>

              <div className="form-group">
                <label>Hạn Mức Tiền</label>
                <select name="betLevel" defaultValue="BAN1">
                  <option value="BAN1">5K - 10K</option>
                  <option value="BAN2">10K - 20K</option>
                  <option value="BAN3">50K - 100K</option>
                  <option value="BAN4">100K - 200K</option>
                  <option value="BAN5">500K - 1M</option>
                </select>
              </div>

              <div className="form-actions">
                <button type="submit" disabled={loading} className="btn-submit">
                  {loading ? '⏳ Đang tạo...' : '✅ Tạo Phòng'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreateRoom(false)}
                  className="btn-cancel"
                >
                  ❌ Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
