import axios from 'axios';
import type { AxiosInstance } from 'axios';
import type { AuthResponse, User, RoomResponse, PageResponse } from '../types';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

class ApiService {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add token to requests
    this.client.interceptors.request.use((config) => {
      const token = localStorage.getItem('accessToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Handle token refresh on 401
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          try {
            const refreshToken = localStorage.getItem('refreshToken');
            const response = await axios.post(`${API_URL}/auth/refresh`, {
              refreshToken,
            });
            localStorage.setItem('accessToken', response.data.accessToken);
            return this.client(originalRequest);
          } catch {
            localStorage.clear();
            window.location.href = '/login';
          }
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth endpoints
  async login(email: string, password: string): Promise<AuthResponse> {
    const response = await this.client.post('/auth/login', { email, password });
    return response.data;
  }

  async register(email: string, username: string, password: string): Promise<AuthResponse> {
    const response = await this.client.post('/auth/register', {
      email,
      username,
      password,
    });
    return response.data;
  }

  async getCurrentUser(): Promise<User> {
    const response = await this.client.get('/auth/me');
    return response.data;
  }

  async logout(): Promise<void> {
    await this.client.post('/auth/logout');
  }

  // Room endpoints
  async getRooms(page: number = 0, size: number = 10): Promise<PageResponse<RoomResponse>> {
    const response = await this.client.get('/rooms', {
      params: { page, size },
    });
    return response.data;
  }

  async getRoom(roomId: string): Promise<RoomResponse> {
    const response = await this.client.get(`/rooms/${roomId}`);
    return response.data;
  }

  async createRoom(name: string, betLevel: string): Promise<RoomResponse> {
    const response = await this.client.post('/rooms', {
      name,
      betLevel,
      maxPlayers: 4,
    });
    return response.data;
  }

  async joinRoom(roomId: string): Promise<RoomResponse> {
    const response = await this.client.post(`/rooms/${roomId}/join`);
    return response.data;
  }

  async leaveRoom(roomId: string): Promise<void> {
    await this.client.post(`/rooms/${roomId}/leave`);
  }

  async markReady(roomId: string, ready: boolean): Promise<RoomResponse> {
    const response = await this.client.post(`/rooms/${roomId}/ready`, { ready });
    return response.data;
  }
}

export default new ApiService();
