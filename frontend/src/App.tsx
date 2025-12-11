import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store';
import { useEffect } from 'react';
import apiService from './services/api';
import websocketService from './services/websocket';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Lobby from './pages/Lobby';
import RoomList from './pages/RoomList';
import RoomWait from './pages/RoomWait';
import GameBoard from './pages/GameBoard';
import GameDashboard from './pages/GameDashboard';

// Components
import MainLayout from './components/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';

import './App.css';

function App() {
  const { user, setUser, isLoggedIn } = useAuthStore();

  // Auto-load user on mount
  useEffect(() => {
    const initApp = async () => {
      const token = localStorage.getItem('accessToken');
      
      // Check if skipAuth in URL params
      const params = new URLSearchParams(window.location.search);
      const skipAuth = params.get('skipAuth') === 'true';

      if (skipAuth) {
        // Inject dev user (match `User` type)
        const devUser = {
          id: '11111111-1111-1111-1111-111111111111',
          username: 'testuser',
          email: 'testuser@example.com',
          fullName: 'Test User',
          avatar: null,
          wallet: {
            id: '22222222-2222-2222-2222-222222222222',
            userId: '11111111-1111-1111-1111-111111111111',
            balance: 100000,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
          rank: {
            id: 'r1',
            userId: '11111111-1111-1111-1111-111111111111',
            points: 0,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
        };
        localStorage.setItem('accessToken', 'dev-token');
        localStorage.setItem('userId', devUser.id);
        setUser(devUser as any);
        // Redirect to remove query param
        window.history.replaceState({}, '', '/lobby');
      } else if (token && !user) {
        try {
          const currentUser = await apiService.getCurrentUser();
          setUser(currentUser);
          // Reconnect WebSocket
          await websocketService.connect(token);
        } catch (error) {
          console.error('Failed to load user:', error);
          localStorage.clear();
        }
      }
    };

    initApp();

    return () => {
      websocketService.disconnect();
    };
  }, []);

  return (
    <Router>
      <Routes>
        {/* Auth Routes (no header) */}
        <Route
          path="/login"
          element={
            isLoggedIn ? <Navigate to="/lobby" replace /> : <Login />
          }
        />
        <Route
          path="/register"
          element={
            isLoggedIn ? <Navigate to="/lobby" replace /> : <Register />
          }
        />

        {/* Protected Routes */}
        <Route
          path="/lobby"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Lobby />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/rooms"
          element={
            <ProtectedRoute>
              <MainLayout>
                <RoomList />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/dashboard/:gameId"
          element={
            <ProtectedRoute>
              <MainLayout>
                <GameDashboard />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/rooms/:roomId"
          element={
            <ProtectedRoute>
              <MainLayout showHeader={true}>
                <RoomWait />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/game/:roomId"
          element={
            <ProtectedRoute>
              <MainLayout showHeader={false}>
                <GameBoard />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* Redirect to lobby on default */}
        <Route
          path="/"
          element={
            isLoggedIn ? <Navigate to="/lobby" replace /> : <Navigate to="/login" replace />
          }
        />

        {/* Catch-all 404 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
