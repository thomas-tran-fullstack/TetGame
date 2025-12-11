import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import websocketService from '../services/websocket';
import apiService from '../services/api';
import { useGameStore, useRoomStore, useChatStore } from '../store';
import type { RoomResponse } from '../types';
import './GameBoard.css';

export default function GameBoard() {
  const { roomId } = useParams<{ roomId: string }>();
  const { currentRoom, setCurrentRoom } = useRoomStore();
  const { gameState, setGameState, selectedCards, toggleCardSelection, clearSelectedCards, isMyTurn, setIsMyTurn } = useGameStore();
  const { messages, addMessage } = useChatStore();
  const [loading, setLoading] = useState(true);
  const [chatInput, setChatInput] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadRoom();
    subscribeToGameUpdates();
    return () => {
      websocketService.unsubscribeAll();
    };
  }, [roomId]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadRoom = async () => {
    if (!roomId) return;
    try {
      setLoading(true);
      const room = await apiService.getRoom(roomId);
      setCurrentRoom(room);
    } catch (error) {
      console.error('Failed to load room:', error);
    } finally {
      setLoading(false);
    }
  };

  const subscribeToGameUpdates = () => {
    if (!roomId) return;

    websocketService.subscribeToGameState(roomId, (state) => {
      setGameState(state);
      // Determine current player from turnOrder + currentTurnIndex
      const currentUserId = localStorage.getItem('userId');
      const currentPlayerId = state.turnOrder ? state.turnOrder[state.currentTurnIndex] : undefined;
      setIsMyTurn(currentPlayerId === currentUserId);
    });

    websocketService.subscribeToGameStarted(roomId, (data) => {
      console.log('Game started:', data);
    });

    websocketService.subscribeToGameEnded(roomId, (data) => {
      console.log('Game ended:', data);
      alert(`🎉 Kết quả: ${data.rankings[0]} thắng!`);
    });

    websocketService.subscribeToChat(roomId, (message) => {
      addMessage(message);
    });

    websocketService.subscribeToNextTurn(roomId, (data) => {
      console.log('Next turn:', data.playerId);
    });
  };

  const handlePlayCards = () => {
    if (selectedCards.length === 0) {
      alert('Chọn bài để đánh');
      return;
    }
    if (!roomId) return;

    websocketService.playCards(roomId, selectedCards);
    clearSelectedCards();
  };

  const handlePass = () => {
    if (!roomId) return;
    websocketService.pass(roomId);
  };

  const handleSendChat = () => {
    if (!chatInput.trim() || !roomId) return;
    websocketService.sendChat(roomId, chatInput);
    setChatInput('');
  };

  if (loading) {
    return <div className="game-loading">Đang tải trò chơi...</div>;
  }

  if (!currentRoom || !gameState) {
    return <div className="game-loading">Chờ bắt đầu trò chơi...</div>;
  }

  return (
    <div className="game-board">
      {/* Header */}
      <div className="game-header">
        <h1>{currentRoom.name}</h1>
        <div className="room-info">
          <span>💰 {currentRoom.betLevel}</span>
          <span>👥 {currentRoom.currentPlayers}/{currentRoom.maxPlayers}</span>
        </div>
      </div>

      <div className="game-main">
        {/* Left: Game Board */}
        <div className="game-table">
          {/* Table Area */}
          <div className="table-area">
            <div className="current-pile">
              <h3>Bài trên bàn</h3>
              {gameState.currentPile ? (
                <div className="pile-display">
                  {gameState.currentPile.cards.map((card, idx) => (
                    <div key={idx} className="card-display">
                      {card.rank} {card.suit}
                    </div>
                  ))}
                </div>
              ) : (
                <p className="empty-pile">Trống</p>
              )}
            </div>

            {/* Other Players */}
            <div className="other-players">
              {(() => {
                const cp = gameState.turnOrder ? gameState.turnOrder[gameState.currentTurnIndex] : undefined;
                return cp ? (
                  <div className="current-player-indicator">🎯 Lượt của: {cp}</div>
                ) : null;
              })()}
              {Object.entries(gameState.hands).map(([playerId, hand]) => (
                <div key={playerId} className="player-status">
                  {playerId}: {hand.length} lá
                </div>
              ))}
            </div>
          </div>

          {/* My Hand & Controls */}
          <div className="my-hand-section">
            <h3>Bài của tôi</h3>
            <div className="hand-display">
              {gameState.hands[localStorage.getItem('userId') || '']?.map((card, idx) => (
                <div
                  key={idx}
                  className={`card ${
                    selectedCards.some((c) => c.suit === card.suit && c.rank === card.rank)
                      ? 'selected'
                      : ''
                  }`}
                  onClick={() => toggleCardSelection(card)}
                >
                  <span className="rank">{card.rank}</span>
                  <span className="suit">{card.suit}</span>
                </div>
              ))}
            </div>

            {/* Controls */}
            <div className="game-controls">
              <button
                className="btn-play"
                onClick={handlePlayCards}
                disabled={!isMyTurn || selectedCards.length === 0}
              >
                Đánh ({selectedCards.length})
              </button>
              <button className="btn-pass" onClick={handlePass} disabled={!isMyTurn}>
                Bỏ
              </button>
            </div>
          </div>
        </div>

        {/* Right: Chat */}
        <div className="chat-panel">
          <h3>💬 Chat</h3>
          <div className="chat-messages">
            {messages.map((msg, idx) => (
              <div key={idx} className="chat-message">
                <span className="chat-sender">{msg.sender}:</span>
                <span className="chat-text">{msg.text}</span>
              </div>
            ))}
            <div ref={chatEndRef} />
          </div>
          <div className="chat-input-area">
            <input
              type="text"
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSendChat()}
              placeholder="Nhập tin nhắn..."
              className="chat-input"
            />
            <button onClick={handleSendChat} className="btn-send">
              Gửi
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
