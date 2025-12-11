import type { Card } from '../types';
import { CardRank, CardSuit } from '../types';
import './CardView.css';

interface Props {
  card: Card;
  faceUp?: boolean;
  selected?: boolean;
  onClick?: () => void;
  className?: string;
  anim?: boolean;
}

export default function CardView({ card, faceUp = true, selected = false, onClick }: Props) {
  const mapRankToNumber = (rank: CardRank) => {
    switch(rank){
      case CardRank.THREE: return '3';
      case CardRank.FOUR: return '4';
      case CardRank.FIVE: return '5';
      case CardRank.SIX: return '6';
      case CardRank.SEVEN: return '7';
      case CardRank.EIGHT: return '8';
      case CardRank.NINE: return '9';
      case CardRank.TEN: return '10';
      case CardRank.JACK: return '11';
      case CardRank.QUEEN: return '12';
      case CardRank.KING: return '13';
      case CardRank.ACE: return '1';
      case CardRank.TWO: return '2';
      default: return '0';
    }
  };

  const mapSuitToSuffix = (suit: CardSuit) => {
    switch(suit){
      case CardSuit.SPADES: return 'bich';
      case CardSuit.CLUBS: return 'tep';
      case CardSuit.DIAMONDS: return 'ro';
      case CardSuit.HEARTS: return 'co';
      default: return '';
    }
  };

  const filename = faceUp ? `${mapRankToNumber(card.rank as CardRank)}${mapSuitToSuffix(card.suit as CardSuit)}.png` : 'saubai.png';
  const src = `${import.meta.env.VITE_API_URL?.replace('/api','') || ''}/images/bai/${filename}`;

  return (
    <div className={`card-view ${selected ? 'selected' : ''} ${className || ''} ${anim ? 'card-play-anim' : ''}`} onClick={onClick}>
      <img src={src} alt={card.rank + '_' + card.suit} />
    </div>
  );
}
