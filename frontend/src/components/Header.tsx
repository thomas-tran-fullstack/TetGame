import { useAuthStore } from '../store';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import './Header.css';

export default function Header() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await apiService.logout();
      logout();
      localStorage.clear();
      navigate('/login');
    } catch (error) {
      console.error('Logout error:', error);
      logout();
      navigate('/login');
    }
  };

  if (!user) return null;

  return (
    <header className="header">
      <div className="header-container">
        <div className="header-left">
          <h1 className="header-logo">🃏 TetGame</h1>
        </div>

        <div className="header-center">
          <span className="header-game-title">Tiến Lên Online</span>
        </div>

        <div className="header-right">
          <div className="wallet-info">
            <span className="xu-icon">💰</span>
            <span className="balance">{user.wallet?.balance || 0}</span>
          </div>

          <div className="user-profile">
            <img
              src={user.avatar || 'https://via.placeholder.com/40'}
              alt={user.username}
              className="user-avatar"
            />
            <div className="user-info">
              <p className="user-name">{user.fullName || user.username}</p>
              <p className="user-rank">Rank: {user.rank?.points || 0}</p>
            </div>
          </div>

          <button className="logout-btn" onClick={handleLogout}>
            Đăng xuất
          </button>
        </div>
      </div>
    </header>
  );
}
