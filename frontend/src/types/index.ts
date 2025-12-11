// Card Types
export enum CardSuit {
  SPADES = 'SPADES',
  CLUBS = 'CLUBS',
  DIAMONDS = 'DIAMONDS',
  HEARTS = 'HEARTS',
}

export enum CardRank {
  THREE = 'THREE',
  FOUR = 'FOUR',
  FIVE = 'FIVE',
  SIX = 'SIX',
  SEVEN = 'SEVEN',
  EIGHT = 'EIGHT',
  NINE = 'NINE',
  TEN = 'TEN',
  JACK = 'JACK',
  QUEEN = 'QUEEN',
  KING = 'KING',
  ACE = 'ACE',
  TWO = 'TWO',
}

export interface Card {
  suit: CardSuit;
  rank: CardRank;
}

// Room Types
export enum BetLevel {
  BAN1 = 'BAN1',
  BAN2 = 'BAN2',
  BAN3 = 'BAN3',
  BAN4 = 'BAN4',
  BAN5 = 'BAN5',
}

export enum RoomStatus {
  WAITING = 'WAITING',
  PLAYING = 'PLAYING',
  FINISHED = 'FINISHED',
}

export interface Room {
  id: string;
  name: string;
  gameType: string;
  hostId: string;
  maxPlayers: number;
  currentPlayers: number;
  status: RoomStatus;
  betLevel: BetLevel;
  createdAt: string;
  updatedAt: string;
}

export interface RoomSeat {
  position: number;
  playerId: string | null;
  isReady: boolean;
  joinedAt: string;
}

export interface RoomResponse {
  id: string;
  name: string;
  gameType: string;
  hostId: string;
  maxPlayers: number;
  currentPlayers: number;
  status: RoomStatus;
  betLevel: BetLevel;
  seats: RoomSeat[];
  createdAt: string;
  updatedAt: string;
}

// Game Types
export interface GameState {
  roomId: string;
  hands: Record<string, Card[]>;
  currentPlayer: string;
  currentPile: Play | null;
  passedThisTurn: string[];
  gameLog: GameLog[];
}

export interface Play {
  type: PlayType;
  cards: Card[];
  primaryRankValue: number;
}

export enum PlayType {
  SINGLE = 'SINGLE',
  PAIR = 'PAIR',
  TRIPLE = 'TRIPLE',
  STRAIGHT = 'STRAIGHT',
  CONSECUTIVE_PAIRS = 'CONSECUTIVE_PAIRS',
  FOUR_OF_KIND = 'FOUR_OF_KIND',
  BOMB = 'BOMB',
  INVALID = 'INVALID',
}

export interface GameLog {
  playerId: string;
  cards: Card[];
  timestamp: number;
}

// User Types
export interface User {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  rank: number;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message: string;
  status: number;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  pageSize: number;
}
