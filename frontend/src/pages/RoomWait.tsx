import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store';
import apiService from '../services/api';
import websocketService from '../services/websocket';
import webrtcService from '../services/webrtc';
import type { RoomResponse } from '../types';
import './RoomWait.css';

export default function RoomWait() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  
  const [room, setRoom] = useState<RoomResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [allReady, setAllReady] = useState(false);
  const [messages, setMessages] = useState<Array<{ sender: string; text: string }>>([]);
  const [newMessage, setNewMessage] = useState('');
  const [voiceEnabled, setVoiceEnabled] = useState(false);

  useEffect(() => {
    loadRoom();
    subscribeToRoomUpdates();

    // subscribe to incoming webrtc signals for this user
    if (roomId) {
      websocketService.subscribe(`/user/queue/room/${roomId}/webrtc`, async (msg:any) => {
        try {
          await webrtcService.handleSignal(msg, user?.id || '', (p)=> websocketService.sendSignaling(roomId, p));
        } catch (err) { console.error(err); }
      });
      websocketService.subscribe(`/topic/room/${roomId}/webrtc`, async (msg:any) => {
        try {
          await webrtcService.handleSignal(msg, user?.id || '', (p)=> websocketService.sendSignaling(roomId, p));
        } catch (err) { console.error(err); }
      });
    }

    return () => {
      websocketService.unsubscribeFromTopic(`/topic/room/${roomId}`);
      websocketService.unsubscribeFromTopic(`/topic/room/${roomId}/chat`);
      websocketService.unsubscribeFromTopic(`/user/queue/room/${roomId}/webrtc`);
      websocketService.unsubscribeFromTopic(`/topic/room/${roomId}/webrtc`);
    };
  }, [roomId]);

  const loadRoom = async () => {
    if (!roomId) return;
    try {
      const response = await apiService.getRoom(roomId);
      setRoom(response);
      setError('');
      
      // Check if all players ready
      const allPlayersReady = response.seats.every(
        seat => !seat.playerId || seat.isReady
      );
      setAllReady(allPlayersReady && response.seats.some(s => s.playerId));
    } catch (err) {
      setError('Failed to load room');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const subscribeToRoomUpdates = () => {
    if (!roomId) return;
    
    websocketService.subscribe(`/topic/room/${roomId}`, (data: any) => {
      setRoom(data);
    });

    websocketService.subscribe(`/topic/room/${roomId}/chat`, (message: any) => {
      setMessages(prev => [...prev, message]);
    });

    websocketService.subscribe(`/topic/game/${roomId}/started`, () => {
      navigate(`/game/${roomId}`);
    });
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim() || !roomId) return;

    websocketService.sendChatMessage(roomId, {
      sender: user?.username || 'Anonymous',
      text: newMessage,
      timestamp: Date.now(),
    });

    setNewMessage('');
  };

  const handleReady = async () => {
    if (!roomId || !user) return;

    try {
      await apiService.markReady(roomId, true);
      setAllReady(true);
    } catch (err) {
      alert('Failed to mark ready');
      console.error(err);
    }
  };

  const handleLeaveRoom = async () => {
    if (!roomId) return;

    try {
      await apiService.leaveRoom(roomId);
      navigate('/lobby');
    } catch (err) {
      alert('Failed to leave room');
      console.error(err);
    }
  };

  const handleStartGame = async () => {
    if (!roomId) return;

    try {
      websocketService.startGame(roomId);
    } catch (err) {
      alert('Failed to start game');
      console.error(err);
    }
  };

  // Voice control
  useEffect(()=>{
    if(voiceEnabled){
      // start local audio and create peer offers to other players
      const peers = room?.seats?.map(s=>s.playerId).filter(Boolean) as string[] || [];
      (async ()=>{
        try{
          await webrtcService.initLocalAudio();
          await webrtcService.startVoice(roomId || '', user?.id || '', peers, (p)=> websocketService.sendSignaling(roomId || '', p));
        }catch(err){ console.error('Voice start failed', err); setVoiceEnabled(false); }
      })();
    } else {
      webrtcService.stopAll();
    }
    // cleanup on unmount
    return ()=>{ if(voiceEnabled) webrtcService.stopAll(); }
  },[voiceEnabled]);

  const isHost = user?.id === room?.hostId;

  if (loading) {
    return <div className="room-wait-container">⏳ Loading...</div>;
  }

  if (error) {
    return <div className="room-wait-container">{error}</div>;
  }

  return (
    <div className="room-wait-container">
      <div className="room-wait-header">
        <div className="room-id">ID Phòng: <strong>{roomId?.substring(0, 8)}</strong></div>
        <div className="room-title">{room?.name}</div>
        <div className="room-bet-level">💰 {room?.betLevel}</div>
      </div>

      <div className="room-wait-content">
        {/* Players Section */}
        <div className="players-section">
          <h3>👥 Người Chơi ({room?.currentPlayers}/{room?.maxPlayers})</h3>
          <div className="players-list">
            {room?.seats?.map((seat, idx) => (
              <div key={idx} className={`player-slot ${seat.playerId ? 'occupied' : 'empty'}`}>
                <div className="player-avatar">
                  {seat.playerId ? '👤' : '❌'}
                </div>
                <div className="player-info">
                  <p className="player-name">{seat.playerId ? 'Player' : 'Trống'}</p>
                  {seat.isReady && <p className="player-ready">✅ Ready</p>}
                </div>
              </div>
            ))}
          </div>

          {/* Action Buttons */}
          <div className="action-buttons">
            {!allReady ? (
              <button className="btn-ready" onClick={handleReady}>
                ✅ Sẵn Sàng
              </button>
            ) : isHost ? (
              <button className="btn-start" onClick={handleStartGame}>
                🎮 Bắt Đầu
              </button>
            ) : (
              <div className="btn-waiting">⏳ Chờ Host Bắt Đầu</div>
            )}

            <button className="btn-leave" onClick={handleLeaveRoom}>
              ❌ Thoát Phòng
            </button>
          </div>
        </div>

        {/* Chat Section */}
        <div className="chat-section">
          <h3>💬 Chat</h3>
          <div className="chat-messages">
            {messages.length === 0 ? (
              <p className="empty-chat">Chưa có tin nhắn</p>
            ) : (
              messages.map((msg, idx) => (
                <div key={idx} className="message">
                  <strong>{msg.sender}:</strong> {msg.text}
                </div>
              ))
            )}
          </div>
          <form onSubmit={handleSendMessage} className="chat-form">
            <input
              type="text"
              placeholder="Nhập tin nhắn..."
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              className="chat-input"
            />
            <button type="submit" className="chat-send">Send</button>
          </form>

          {/* Voice Toggle */}
          <button
            className={`btn-voice ${voiceEnabled ? 'active' : ''}`}
            onClick={() => setVoiceEnabled(!voiceEnabled)}
          >
            🎤 {voiceEnabled ? 'Voice ON' : 'Voice OFF'}
          </button>
        </div>
      </div>
    </div>
  );
}
