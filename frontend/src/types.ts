// Game & Card Types
export enum CardSuit {
  SPADES = 'SPADES',
  CLUBS = 'CLUBS',
  DIAMONDS = 'DIAMONDS',
  HEARTS = 'HEARTS'
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
  TWO = 'TWO'
}

export interface Card {
  suit: CardSuit;
  rank: CardRank;
}

export enum BetLevel {
  BAN1 = 'BAN1', // 5k-10k
  BAN2 = 'BAN2', // 10k-20k
  BAN3 = 'BAN3', // 50k-100k
  BAN4 = 'BAN4', // 100k-200k
  BAN5 = 'BAN5'  // 500k-1m
}

export enum RoomStatus {
  WAITING = 'WAITING',
  PLAYING = 'PLAYING',
  FINISHED = 'FINISHED'
}

// Room & Player Types
export interface User {
  id: string;
  username: string;
  email: string;
  fullName?: string;
  avatar?: string;
  wallet: Wallet;
  rank: Rank;
}

export interface Wallet {
  id: string;
  userId: string;
  balance: number;
  createdAt: string;
  updatedAt: string;
}

export interface Rank {
  id: string;
  userId: string;
  points: number;
  createdAt: string;
  updatedAt: string;
}

export interface RoomSeat {
  position: number;
  playerId?: string;
  isReady?: boolean;
  joinedAt?: string;
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
  seats: RoomSeat[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateRoomRequest {
  name: string;
  gameType?: string;
  betLevel?: BetLevel;
}

export interface RoomResponse {
  id: string;
  name: string;
  status: RoomStatus;
  hostId: string;
  currentPlayers: number;
  maxPlayers: number;
  betLevel: BetLevel;
  seats: RoomSeat[];
}

// Game State Types
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
  INVALID = 'INVALID'
}

export interface GameState {
  roomId: string;
  hands: Record<string, Card[]>;
  turnOrder: string[];
  currentTurnIndex: number;
  currentPile?: Play;
  passedThisTurn: string[];
  gameLog: GameMove[];
}

export interface GameMove {
  playerId: string;
  cards: Card[];
  timestamp: number;
}

export interface GameResult {
  rankings: string[]; // [1st, 2nd, 3rd, 4th]
  settlement: Record<string, number>; // playerId -> delta
  log: GameMove[];
}

// Authentication Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  user: User;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  user: User;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}
