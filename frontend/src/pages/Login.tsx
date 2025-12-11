import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import websocketService from '../services/websocket';
import { useAuthStore } from '../store';
import './Auth.css';

export default function Login() {
  const navigate = useNavigate();
  const { setUser } = useAuthStore();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const demoAccounts = [
    { username: 'testuser', password: 'Passw0rd!' },
    { username: 'player2', password: 'Passw0rd!' },
  ];

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await apiService.login(username, password);
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('userId', response.user.id);
      setUser(response.user);

      // Connect WebSocket
      await websocketService.connect(response.accessToken);

      navigate('/rooms');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Đăng nhập thất bại');
    } finally {
      setLoading(false);
    }
  };

  const quickLogin = (acc: { username: string; password: string }) => {
    setUsername(acc.username);
    setPassword(acc.password);
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>🃏 Tiến Lên Online</h1>
        <form onSubmit={handleLogin}>
          <input
            type="text"
            placeholder="Tài khoản"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            className="auth-input"
          />
          <input
            type="password"
            placeholder="Mật khẩu"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="auth-input"
          />
          {error && <div className="error-message">{error}</div>}
          <button type="submit" disabled={loading} className="auth-button">
            {loading ? 'Đang đăng nhập...' : 'Đăng Nhập'}
          </button>
        </form>

        <div className="demo-accounts">
          <p style={{ fontSize: '12px', color: '#999', marginTop: '20px' }}>Tài khoản demo:</p>
          {demoAccounts.map((acc, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => quickLogin(acc)}
              style={{
                display: 'block',
                width: '100%',
                padding: '8px',
                margin: '5px 0',
                fontSize: '12px',
                background: '#f0f0f0',
                border: '1px solid #ccc',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              {acc.username} / {acc.password}
            </button>
          ))}
        </div>

        <p className="auth-link">
          Chưa có tài khoản? <a href="/register">Đăng ký</a>
        </p>
      </div>
    </div>
  );
}
