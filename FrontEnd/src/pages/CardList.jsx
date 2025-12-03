import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import api from '../api/client';
import { getAuthHeader } from '../api/auth';
import FAB from '../components/FAB';
import SearchBar from '../components/SearchBar';
import './CardList.css';

const CardList = () => {
  const navigate = useNavigate();
  const { currentUser } = useApp();
  
  const [cards, setCards] = useState([]);
  const [filteredCards, setFilteredCards] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  // const navigate = useNavigate();
  // const [cards, setCards] = useState([]); // 전체 명함 목록
  // const [filteredCards, setFilteredCards] = useState([]); // 검색용
  // const [loading, setLoading] = useState(false);
  // const [error, setError] = useState('');

  // const userIdx = 1; // ✅ 로그인된 사용자 ID (임시)
  // const page = 0;
  // const size = 10;
  const fetchCards = async () => {
    if (!currentUser?.idx && !currentUser?.id) {
      console.log('사용자 정보가 없습니다.');
      return;
    }

    setLoading(true);
    setError('');
    
    try {
      // const userIdx = currentUser.idx || currentUser.id || 1; // 임시 fallback
      const response = await api.get(`/api/bizcards/me`, {
        params: {
          page: page,
          size: 10,
          sort: 'createdAt,DESC'
        },
        headers: getAuthHeader()
      });

      const result = response.data;
      console.log('API 응답:', result);

      if (result.success && result.data?.content) {
        const newCards = result.data.content;
        
        if (page === 0) {
          setCards(newCards);
          setFilteredCards(newCards);
        } else {
          setCards(prev => [...prev, ...newCards]);
          setFilteredCards(prev => [...prev, ...newCards]);
        }
        
        // 더 불러올 데이터가 있는지 확인
        setHasMore(!result.data.last);
      } else {
        throw new Error('데이터 형식이 올바르지 않습니다.');
      }
    } catch (err) {
      console.error("명함 목록 불러오기 실패:", err);
      
      if (err.response?.status === 404) {
        setError('명함이 없습니다.');
      } else if (err.response?.status === 401) {
        setError('로그인이 필요합니다.');
        navigate('/login');
      } else {
        setError('명함 데이터를 불러오는 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  // 명함 목록 API 호출
  useEffect(() => {
    fetchCards();
  }, [page]);

  const handleBack = () => {
    navigate('/main');
  };

  const handleCardDetail = (cardId) => {
  navigate(`/carddetail/${cardId}`);
  };

  const handleSearch = (keyword) => {
    if (!keyword.trim()) {
      setFilteredCards(cards);
      return;
    }
    
    const lowerKeyword = keyword.toLowerCase();
    const filtered = cards.filter(card =>
      card.name?.toLowerCase().includes(lowerKeyword) ||
      card.companyName?.toLowerCase().includes(lowerKeyword) ||
      card.department?.toLowerCase().includes(lowerKeyword) ||
      card.position?.toLowerCase().includes(lowerKeyword) ||
      card.email?.toLowerCase().includes(lowerKeyword) ||
      card.phoneNumber?.includes(keyword)
    );
    setFilteredCards(filtered);
  };

  const handleLoadMore = () => {
    if (hasMore && !loading) {
      setPage(prev => prev + 1);
    }
  };

  // 명함 삭제
  const handleDeleteCard = async (e, cardId) => {
    e.stopPropagation();
    
    if (!window.confirm('이 명함을 삭제하시겠습니까?')) {
      return;
    }
    
    try {
      await api.delete(`/api/bizcards/${cardId}`);
      
      // 로컬 상태에서 삭제
      setCards(prev => prev.filter(c => c.idx !== cardId));
      setFilteredCards(prev => prev.filter(c => c.idx !== cardId));
      
      alert('명함이 삭제되었습니다.');
    } catch (err) {
      console.error('명함 삭제 실패:', err);
      alert('명함 삭제 중 오류가 발생했습니다.');
    }
  };

  // 날짜 포맷팅
  const formatDate = (dateString) => {
    if (!dateString) return '';
    
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    } catch {
      return dateString;
    }
  };

  if (loading && page === 0) {
    return (
      <div className="cardlist-container">
        <div className="loading-state">불러오는 중...</div>
      </div>
    );
  }

  // const handleCardClick = (card) => {
  //   setSelectedCard(card);
  // };

  // const handleClosePopup = () => {
  //   setSelectedCard(null);
  // };

  

  if (loading) return <div className="cardlist-container">불러오는 중...</div>;
  if (error) return <div className="cardlist-container">{error}</div>;


  return (
    <div className="cardlist-container">
      <div className="cardlist-box">

        {/* 명함 목록 헤더 */}
        <div className="cardlist-header app-header">
          <button className="back-btn" onClick={handleBack}>
            ←
          </button>
          {/* <div className="hashtag-icon" onClick={handleHashtagList}>#</div> */}
        </div>

        {/* 명함 검색 */}
        <SearchBar onSearch={handleSearch} />

        {/* 명함 항목 */}
        <div className="cardlist">
          {filteredCards.length > 0 ? (
            filteredCards.map(card => (
              <div key={card.idx}
                   className="card-item"
                   onClick={() => handleCardDetail(card.idx)}>
                {/* <button 
                  className="delete-btn"
                  onClick={(e) => handleDeleteCard(e, card.id)}
                  title="삭제"
                >
                  ×
                </button> */}
                <div className="card-item-head">
                  <div className="card-item-name">{card.name}</div>
                  <div className="card-item-date">{card.createdAt}</div>
                </div>
                <div className="card-item-info">
                  <div className="card-item-company">{card.companyName}</div>
                  <div className="card-item-position">
                    {card.position && `${card.position}`}
                    {card.position && card.department && ' | '}
                    {card.department && `${card.department}`}
                  </div>
                  <div className="card-item-contact">
                    {card.phoneNumber && `📞 ${card.phoneNumber}`}
                    {card.phoneNumber && card.email && ' | '}
                    {card.email && `✉️ ${card.email}`}
                  </div>
                </div>
                {card.hashTags && card.hashTags.length > 0 && (
                  <div className="card-tags">
                    {card.hashTags.map((tag, index) => (
                      <span key={index} className="card-tag">#{tag}</span>
                    ))}
                  </div>
                )}
              </div>
            ))
          ) : (
            <div className="empty-state">
              <p>명함이 없습니다.</p>
              <p>카메라 버튼을 눌러 명함을 추가해주세요.</p>
            </div>
          )}
        </div>

        {/* 카메라 버튼 */}
        <FAB />
        
      </div>
    </div>
  );
};

export default CardList;
