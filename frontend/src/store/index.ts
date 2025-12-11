import { create } from 'zustand';
import type { User, Room, RoomResponse, GameState, Card } from '../types';

interface AuthStore {
  user: User | null;
  isLoggedIn: boolean;
  setUser: (user: User | null) => void;
  logout: () => void;
}

interface RoomStore {
  rooms: RoomResponse[];
  currentRoom: RoomResponse | null;
  setRooms: (rooms: RoomResponse[]) => void;
  setCurrentRoom: (room: RoomResponse | null) => void;
  updateRoomSeats: (roomId: string, seats: any[]) => void;
}

interface GameStore {
  gameState: GameState | null;
  selectedCards: Card[];
  isMyTurn: boolean;
  setGameState: (state: GameState | null) => void;
  setSelectedCards: (cards: Card[]) => void;
  toggleCardSelection: (card: Card) => void;
  clearSelectedCards: () => void;
  setIsMyTurn: (isMyTurn: boolean) => void;
}

interface ChatStore {
  messages: Array<{ sender: string; text: string; timestamp: number }>;
  addMessage: (message: any) => void;
  clearMessages: () => void;
}

// Auth Store
export const useAuthStore = create<AuthStore>((set) => ({
  user: null,
  isLoggedIn: false,
  setUser: (user) => set({ user, isLoggedIn: user !== null }),
  logout: () => set({ user: null, isLoggedIn: false }),
}));

// Room Store
export const useRoomStore = create<RoomStore>((set) => ({
  rooms: [],
  currentRoom: null,
  setRooms: (rooms) => set({ rooms }),
  setCurrentRoom: (room) => set({ currentRoom: room }),
  updateRoomSeats: (roomId, seats) =>
    set((state) => {
      if (state.currentRoom?.id === roomId) {
        return {
          currentRoom: {
            ...state.currentRoom,
            seats,
          },
        };
      }
      return state;
    }),
}));

// Game Store
export const useGameStore = create<GameStore>((set) => ({
  gameState: null,
  selectedCards: [],
  isMyTurn: false,
  setGameState: (gameState) => set({ gameState }),
  setSelectedCards: (cards) => set({ selectedCards: cards }),
  toggleCardSelection: (card) =>
    set((state) => {
      const isSelected = state.selectedCards.some(
        (c) => c.suit === card.suit && c.rank === card.rank
      );
      if (isSelected) {
        return {
          selectedCards: state.selectedCards.filter(
            (c) => !(c.suit === card.suit && c.rank === card.rank)
          ),
        };
      } else {
        return {
          selectedCards: [...state.selectedCards, card],
        };
      }
    }),
  clearSelectedCards: () => set({ selectedCards: [] }),
  setIsMyTurn: (isMyTurn) => set({ isMyTurn }),
}));

// Chat Store
export const useChatStore = create<ChatStore>((set) => ({
  messages: [],
  addMessage: (message) =>
    set((state) => ({
      messages: [...state.messages, message],
    })),
  clearMessages: () => set({ messages: [] }),
}));
