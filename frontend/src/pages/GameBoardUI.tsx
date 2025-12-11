import { useEffect, useState } from 'react';
import './GameBoardUI.css';
import CardView from '../components/CardView';
import websocketService from '../services/websocket';
import { useAuthStore } from '../store';
import type { Card } from '../types';
import { CardRank, CardSuit } from '../types';
import { useParams } from 'react-router-dom';

export default function GameBoardUI(){
  const { roomId } = useParams<{roomId:string}>();
  const { user } = useAuthStore();
  const [hand, setHand] = useState<Card[]>([]);
  const [selected, setSelected] = useState<Record<number,boolean>>({});
  const [playingIndices, setPlayingIndices] = useState<number[]>([]);
  const [log, setLog] = useState<string[]>([]);
  const [players, setPlayers] = useState<Array<{id?:string,name?:string,balance?:number,avatar?:string}>>([]);
  const [betLevel] = useState('BAN1');

  useEffect(()=>{
    // sample demo hand (placeholder) - in real will come from WebSocket game:start
    const demo: Card[] = [
      { suit: CardSuit.HEARTS, rank: CardRank.THREE },
      { suit: CardSuit.CLUBS, rank: CardRank.FIVE },
      { suit: CardSuit.SPADES, rank: CardRank.TWO },
      { suit: CardSuit.DIAMONDS, rank: CardRank.ACE },
      { suit: CardSuit.HEARTS, rank: CardRank.KING },
      { suit: CardSuit.SPADES, rank: CardRank.TEN },
      { suit: CardSuit.HEARTS, rank: CardRank.SEVEN },
      { suit: CardSuit.CLUBS, rank: CardRank.EIGHT },
      { suit: CardSuit.SPADES, rank: CardRank.NINE },
      { suit: CardSuit.HEARTS, rank: CardRank.FOUR },
      { suit: CardSuit.CLUBS, rank: CardRank.JACK },
      { suit: CardSuit.DIAMONDS, rank: CardRank.QUEEN },
      { suit: CardSuit.SPADES, rank: CardRank.SIX }
    ];
    setHand(demo);

    // subscribe to game events
    if(roomId){
      websocketService.subscribe(`/topic/game/${roomId}/state`, (data)=>{
        // update state
        const uid = user?.id;
        if(uid && data.hands && data.hands[uid]){
          setHand(data.hands[uid] || []);
        }
        if(data.players) setPlayers(data.players);
      });

      websocketService.subscribe(`/topic/game/${roomId}/log`, (m:any)=> setLog((l)=>[...l, m.message]));
    }

    return ()=>{
      websocketService.unsubscribeAll();
    }
  },[roomId]);

  const toggleSelect = (idx:number)=>{
    setSelected(s => ({...s, [idx]: !s[idx]}));
  }

  const playSelected = ()=>{
    const cards = Object.keys(selected).filter(k=>selected[+k]).map(k=>hand[+k]);
    if(cards.length===0) return alert('Chọn lá để đánh');
    if(!roomId || !user) return;
    // mark playing indices to animate
    const idxs = Object.keys(selected).filter(k=>selected[+k]).map(k=>+k);
    setPlayingIndices(idxs);
    // send via websocket service
    websocketService.playCards(roomId, cards);
    setLog(l=>[...l, `Bạn đánh ${cards.length} lá`]);
    // after animation, remove from hand
    setTimeout(()=>{
      const remaining = hand.filter((_,i)=>!selected[i]);
      setHand(remaining);
      setSelected({});
      setPlayingIndices([]);
    }, 700);
  }

  const pass = ()=>{
    if(!roomId || !user) return;
    websocketService.pass(roomId);
    setLog(l=>[...l, 'Bạn đã bỏ lượt']);
  }

  const enterFullscreen = async ()=>{
    const el = document.documentElement as any;
    if(el.requestFullscreen) await el.requestFullscreen();
  }

  return (
    <div className="gb-root">
      <div className="gb-topbar">
        <div className="gb-left">Bàn: {roomId?.substring(0,8)} • {betLevel}</div>
        <div className="gb-center">🖤 Tiến Lên • Black-Red Theme</div>
        <div className="gb-right">{user?.username} • <button onClick={enterFullscreen} className="btn-small">FS</button></div>
      </div>

      <div className="gb-table">
        <img src={`${import.meta.env.VITE_API_URL?.replace('/api','') || ''}/images/ban/ban3.png`} alt="table" className="gb-table-bg"/>

        <div className="gb-player-top">{players[1]?.name || 'Player 2'}</div>
        <div className="gb-player-left">{players[2]?.name || 'Player 3'}</div>
        <div className="gb-player-right">{players[3]?.name || 'Player 4'}</div>

        <div className="gb-hand-area">
          <div className="gb-hand-scroll">
            {hand.map((c, idx)=> (
              <CardView key={idx} card={c} selected={!!selected[idx]} onClick={()=>toggleSelect(idx)} anim={playingIndices.includes(idx)} />
            ))}
          </div>

          <div className="gb-actions">
            <button className="btn-play-main" onClick={playSelected}>Đánh</button>
            <button className="btn-pass" onClick={pass}>Bỏ lượt</button>
          </div>
        </div>

        <div className="gb-log">
          <h4>Log</h4>
          <div className="gb-log-list">{log.map((l,i)=>(<div key={i}>{l}</div>))}</div>
        </div>
      </div>
    </div>
  )
}
