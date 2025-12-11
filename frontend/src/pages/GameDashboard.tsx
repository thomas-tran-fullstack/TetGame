import { useParams, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import './GameDashboard.css';

export default function GameDashboard(){
  const { gameId } = useParams<{ gameId: string }>();
  const navigate = useNavigate();
  const [showTasks, setShowTasks] = useState(false);
  const [showWheel, setShowWheel] = useState(false);
  const [dailyClaimed, setDailyClaimed] = useState(false);

  const handlePlay = () => {
    navigate(`/rooms?game=${gameId}`);
  }

  const handleClaimDaily = () => {
    if(dailyClaimed){
      alert('Bạn đã nhận thưởng hằng ngày.');
      return;
    }
    // Placeholder: call backend to credit wallet
    setDailyClaimed(true);
    alert('Đã nhận thưởng đăng nhập hàng ngày!');
  }

  const handleSpin = () => {
    // Simple client-side random spin for UI demo
    const prize = [5000,10000,20000,0,50000][Math.floor(Math.random()*5)];
    setShowWheel(true);
    setTimeout(()=>{
      alert(`Bạn nhận được ${prize} xu! (demo)`);
      setShowWheel(false);
    }, 1200);
  }

  return (
    <div className="dashboard-container">
      <h1 className="dashboard-title">{gameId === 'TIEN_LEN' ? 'Tiến Lên' : gameId}</h1>

      <div className="dashboard-grid">
        <div className="dash-card">
          <h3>Chơi</h3>
          <p>Bắt đầu tìm phòng hoặc tạo phòng riêng</p>
          <button className="dash-btn" onClick={handlePlay}>Vào sảnh trò chơi</button>
        </div>

        <div className="dash-card">
          <h3>Nhiệm vụ</h3>
          <p>Hoàn thành nhiệm vụ hằng ngày để nhận xu</p>
          <button className="dash-btn" onClick={()=>setShowTasks(true)}>Mở nhiệm vụ</button>
        </div>

        <div className="dash-card">
          <h3>Đăng nhập hằng ngày</h3>
          <p>Nhận quà khi đăng nhập</p>
          <button className="dash-btn" onClick={handleClaimDaily}>{dailyClaimed ? 'Đã nhận' : 'Nhận thưởng'}</button>
        </div>

        <div className="dash-card">
          <h3>Vòng Xoay</h3>
          <p>Mỗi ngày 1 lượt quay (demo UI)</p>
          <button className="dash-btn" onClick={handleSpin}>Quay Ngay</button>
        </div>
      </div>

      {showTasks && (
        <div className="modal-overlay">
          <div className="modal-dialog">
            <h3>Nhiệm vụ hàng ngày (demo)</h3>
            <ul>
              <li>Chơi 1 trận (Nhận 5k)</li>
              <li>Chơi 5 trận (Nhận 10k)</li>
              <li>Thắng 2 trận (Nhận 15k)</li>
              <li>Đăng nhập 7 ngày (Nhận 50k)</li>
              <li>Chơi 10 trận (Nhận 25k)</li>
            </ul>
            <div style={{display:'flex', gap:12, marginTop:16}}>
              <button onClick={()=>setShowTasks(false)} className="btn-cancel">Đóng</button>
            </div>
          </div>
        </div>
      )}

    </div>
  )
}
