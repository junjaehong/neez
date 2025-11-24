import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './SttCardSelect.css';

const DEFAULT_MEETING_ID = 1;

const SttCardSelect = () => {
  const navigate = useNavigate();
  const { cardList, setMeetingParticipants, setCurrentMeeting } = useApp();
  const [selectedCards, setSelectedCards] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [showConfirmPopup, setShowConfirmPopup] = useState(false);

  const handleBack = () => {
    navigate('/main');
  };

  const handleSelectCard = (cardId) => {
    if (selectedCards.includes(cardId)) {
      setSelectedCards(selectedCards.filter(id => id !== cardId));
    } else {
      setSelectedCards([...selectedCards, cardId]);
    }
  };

  const filteredCards = cardList.filter(card => 
    card.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    card.company.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleStartMeeting = () => {
    if (selectedCards.length === 0) {
      alert('최소 한 명의 참석자를 선택해주세요.');
      return;
    }
    setShowConfirmPopup(true);
  };

  const confirmStartMeeting = () => {
    const participants = cardList.filter(card => selectedCards.includes(card.id));
    setMeetingParticipants(participants);
    if (setCurrentMeeting) {
      setCurrentMeeting({
        id: DEFAULT_MEETING_ID,
        startedAt: new Date().toISOString(),
        participantIds: participants.map(card => card.id ?? card.idx).filter(Boolean),
        language: 'ko',
      });
    }
    navigate('/stting');
  };

  return (
    <div className="stt-select-container">
      <div className="stt-select-box">
        <div className="stt-select-header">
          <button className="back-button" onClick={handleBack}>←</button>
          <h2>참석자 선택</h2>
          <div></div>
        </div>

        {/* 검색바 */}
        <div className="search-container">
          <input
            type="text"
            placeholder="이름, 회사명으로 검색"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
        </div>

        {/* 명함 목록 */}
        <div className="card-select-list">
          {filteredCards.map(card => (
            <div 
              key={card.id}
              className={`card-select-item ${selectedCards.includes(card.id) ? 'selected' : ''}`}
              onClick={() => handleSelectCard(card.id)}
            >
              <div className="card-checkbox">
                {selectedCards.includes(card.id) && '✓'}
              </div>
              <div className="card-info">
                <div className="card-name">{card.name}</div>
                <div className="card-company">{card.position} | {card.company}</div>
              </div>
            </div>
          ))}
        </div>

        {/* 확인 버튼 */}
        <button className="confirm-button" onClick={handleStartMeeting}>
          확인
        </button>
      </div>

      {/* 참석자 확인 팝업 */}
      {showConfirmPopup && (
        <div className="popup-overlay" onClick={() => setShowConfirmPopup(false)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowConfirmPopup(false)}>×</button>
            <h3>참석자 확인</h3>
            <div className="participant-list">
              {cardList
                .filter(card => selectedCards.includes(card.id))
                .map(card => (
                  <div key={card.id} className="participant-item">
                    <span className="participant-name">{card.name}</span>
                    <span className="participant-company">{card.company}</span>
                  </div>
                ))}
            </div>
            <div className="popup-actions">
              <button 
                className="cancel-button"
                onClick={() => setShowConfirmPopup(false)}
              >
                취소
              </button>
              <button 
                className="start-button"
                onClick={confirmStartMeeting}
              >
                회의 시작
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SttCardSelect;
