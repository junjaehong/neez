import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import axios from 'axios';
import api from '../api/client';
import { getAuthHeader } from '../api/auth'; // 인증 토큰 함수
import { loadConfig } from '../api/configLoader';
import './SttCardSelect.css';

const DEFAULT_MEETING_ID = 1;

const SttCardSelect = () => {
  const navigate = useNavigate();
  const {setMeetingParticipants, setCurrentMeeting, currentUser } = useApp();
  const [cardList, setCardList] = useState([]);
  const [selectedCards, setSelectedCards] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [showConfirmPopup, setShowConfirmPopup] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
////////////////////////////////////
  useEffect(() => {
    const fetchCards = async () => {
      if (!currentUser?.idx && !currentUser?.id) {
        console.log('사용자 정보가 없습니다.');
        return;
      }

      setLoading(true);
      setError('');
      
      try {
        const response = await api.get(`/api/bizcards/me`, {
          params: {
            page: 0,
            size: 10,
            sort: 'createdAt,DESC'
          },
          headers: getAuthHeader()
        });

        const result = response.data;
        console.log('API 응답:', result);

        if (result.success && result.data?.content) {
          setCardList(result.data.content);
          
          // if (page === 0) {
          //   setCards(newCards);
          //   setFilteredCards(newCards);
          // } else {
          //   setCards(prev => [...prev, ...newCards]);
          //   setFilteredCards(prev => [...prev, ...newCards]);
          // }
          
          // // 더 불러올 데이터가 있는지 확인
          // setHasMore(!result.data.last);
        } else {
          throw new Error('데이터 형식이 올바르지 않습니다.');
        }
      } catch (err) {
        console.error("명함 목록 불러오기 실패:", err);
        setError('명함 목록을 불러오는 중 오류가 발생했습니다.');
      } finally {
        setLoading(false);
      }
    };
    console.log("currentUser:", currentUser);
    fetchCards(); // 여기서 바로 호출
}, [currentUser]);

/////////////////////////////////////
  const handleBack = () => {
    navigate('/main');
  };

  const handleSelectCard = (cardIdx) => {
    if (selectedCards.includes(cardIdx)) {
      setSelectedCards(selectedCards.filter(id => id !== cardIdx));
    } else {
      setSelectedCards([...selectedCards, cardIdx]);
    }
  };

  const filteredCards = cardList.filter(card => 
    (card.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    card.cardCompanyName?.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  const handleStartMeeting = () => {
    if (selectedCards.length === 0) {
      alert('최소 한 명의 참석자를 선택해주세요.');
      return;
    }
    setShowConfirmPopup(true);
  };

  const confirmStartMeeting = () => {
    const participants = cardList.filter(card => selectedCards.includes(card.idx));
    setMeetingParticipants(participants);
    if (setCurrentMeeting) {
      setCurrentMeeting({
        id: DEFAULT_MEETING_ID,
        startedAt: new Date().toISOString(),
        participantIds: participants.map(card => card.idx).filter(Boolean),
        language: 'ko',
      });
    }
    navigate('/stting');
  };

  return (
    <div className="stt-select-container">
      <div className="stt-select-box">
        <div className="stt-select-header">
          <button className="back-btn" onClick={handleBack}>←</button>
          <p>참석자 선택</p>
          <div></div>
        </div>

        {/* 검색바 */}
        <div className="search-area">
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
          {filteredCards.map((card, index) => (
            <div 
              key={card.idx ?? index}
              className={`card-select-item ${selectedCards.includes(card.idx) ? 'selected' : ''}`}
              onClick={() => handleSelectCard(card.idx)}
            >
              <div className="card-checkbox">
                {selectedCards.includes(card.idx) && '✓'}
              </div>
              <div className="card-info">
                <div className="card-name">{card.name}</div>
                <div className="card-company">{card.position} | {card.cardCompanyName}</div>
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
                .filter(card => selectedCards.includes(card.idx))
                .map(card => (
                  <div key={card.idx} className="participant-item">
                    <span className="participant-name">{card.name}</span>
                    <span className="participant-company">{card.cardCompanyName}</span>
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
