import SockJS from 'sockjs-client';
import { Stomp, StompSubscription } from 'stompjs';
import type { Card, Play } from '../types';

class WebSocketService {
  private stompClient: any = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private url = import.meta.env.VITE_WS_URL || '/ws';
  private token: string | null = null;

  connect(token: string): Promise<void> {
    return new Promise((resolve, reject) => {
      this.token = token;
      const socket = new SockJS(this.url);
      this.stompClient = Stomp.over(socket);

      const headers: Record<string, string> = {};
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      this.stompClient.connect(
        headers,
        (frame: any) => {
          console.log('Connected: ' + frame);
          resolve();
        },
        (error: any) => {
          console.error('Connection error:', error);
          reject(error);
        }
      );
    });
  }

  disconnect(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect(() => {
        console.log('Disconnected');
      });
    }
    this.subscriptions.clear();
  }

  // Room operations
  joinRoom(roomId: string): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(`/app/room/${roomId}/join`, {});
    }
  }

  sendChatMessage(roomId: string, message: any): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(`/app/room/${roomId}/chat`, {}, JSON.stringify(message));
    }
  }

  startGame(roomId: string): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(`/app/room/${roomId}/start`, {});
    }
  }

  leaveRoom(roomId: string): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(`/app/room/${roomId}/leave`, {});
    }
  }

  markReady(roomId: string, ready: boolean): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(`/app/room/${roomId}/ready`, {}, JSON.stringify({ ready }));
    }
  }

  // Game operations
  playCards(roomId: string, cards: Card[]): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(
        `/app/game/${roomId}/play`,
        {},
        JSON.stringify({ cards })
      );
    }
  }

  pass(roomId: string): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(`/app/game/${roomId}/pass`, {});
    }
  }

  // Signaling for WebRTC
  sendSignaling(roomId: string, payload: any): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(`/app/room/${roomId}/webrtc`, {}, JSON.stringify(payload));
    }
  }

  sendChat(roomId: string, text: string): void {
    if (this.stompClient?.connected) {
      this.stompClient.send(
        `/app/game/${roomId}/chat`,
        {},
        JSON.stringify({ text })
      );
    }
  }

  // Subscription methods
  subscribeToRoomUpdates(roomId: string, callback: (data: any) => void): void {
    const topic = `/topic/room/${roomId}/updates`;
    const subscription = this.stompClient.subscribe(topic, (message: any) => {
      callback(JSON.parse(message.body));
    });
    this.subscriptions.set(topic, subscription);
  }

  subscribeToGameState(roomId: string, callback: (state: any) => void): void {
    const topic = `/topic/game/${roomId}/state`;
    const subscription = this.stompClient.subscribe(topic, (message: any) => {
      callback(JSON.parse(message.body));
    });
    this.subscriptions.set(topic, subscription);
  }

  subscribe(topic: string, callback: (data: any) => void): void {
    if (this.stompClient?.connected) {
      const subscription = this.stompClient.subscribe(topic, (message: any) => {
        try {
          callback(JSON.parse(message.body));
        } catch {
          callback(message.body);
        }
      });
      this.subscriptions.set(topic, subscription);
    }
  }

  subscribeToGameStarted(roomId: string, callback: (data: any) => void): void {
    const topic = `/topic/game/${roomId}/started`;
    const subscription = this.stompClient.subscribe(topic, (message: any) => {
      callback(JSON.parse(message.body));
    });
    this.subscriptions.set(topic, subscription);
  }

  subscribeToGameEnded(roomId: string, callback: (data: any) => void): void {
    const topic = `/topic/game/${roomId}/ended`;
    const subscription = this.stompClient.subscribe(topic, (message: any) => {
      callback(JSON.parse(message.body));
    });
    this.subscriptions.set(topic, subscription);
  }

  subscribeToNextTurn(roomId: string, callback: (data: any) => void): void {
    const topic = `/topic/game/${roomId}/next-turn`;
    const subscription = this.stompClient.subscribe(topic, (message: any) => {
      callback(JSON.parse(message.body));
    });
    this.subscriptions.set(topic, subscription);
  }

  subscribeToChat(roomId: string, callback: (msg: any) => void): void {
    const topic = `/topic/game/${roomId}/chat`;
    const subscription = this.stompClient.subscribe(topic, (message: any) => {
      callback(JSON.parse(message.body));
    });
    this.subscriptions.set(topic, subscription);
  }

  unsubscribeFromTopic(topic: string): void {
    const subscription = this.subscriptions.get(topic);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(topic);
    }
  }

  unsubscribeAll(): void {
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
  }
}

export default new WebSocketService();
