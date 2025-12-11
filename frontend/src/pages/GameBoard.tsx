import { useParams } from 'react-router-dom';
import './GameBoard.css';

export default function GameBoard() {
  const { roomId } = useParams<{ roomId: string }>();

  return (
    <div className="gameboard-container">
      <div className="gameboard-content">
        <div className="game-table">
          <h2>🎮 Bàn Chơi: {roomId?.substring(0, 8)}</h2>
          <p style={{ color: '#999', marginTop: '2rem', fontSize: '1.2rem' }}>
            ⏳ Giao diện trò chơi sẽ được hoàn thiện trong Giai Đoạn 9
          </p>
          <p style={{ color: '#ccc', marginTop: '1rem' }}>
            [Bàn chơi sẽ hiển thị bài, lượt chơi, chat, voice, và kết quả ở đây]
          </p>
        </div>
      </div>
    </div>
  );
}
