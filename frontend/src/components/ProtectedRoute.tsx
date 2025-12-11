import { ReactNode, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store';

// Allow skipping auth when VITE_SKIP_AUTH=true or localStorage.skipAuth=true
const SKIP_AUTH = import.meta.env.VITE_SKIP_AUTH === 'true' || localStorage.getItem('skipAuth') === 'true';
const DEV_USER = {
  id: '11111111-1111-1111-1111-111111111111',
  username: 'testuser',
  fullName: 'Test User',
  avatarUrl: null,
};

interface ProtectedRouteProps {
  children: ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isLoggedIn } = useAuthStore();

  // If skip flag set, auto-inject dev user and allow access
  useEffect(() => {
    if (SKIP_AUTH) {
      const { setUser } = useAuthStore.getState();
      const token = localStorage.getItem('accessToken') || 'dev-token';
      localStorage.setItem('accessToken', token);
      setUser(DEV_USER as any);
    }
  }, []);

  if (!isLoggedIn && !SKIP_AUTH) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
