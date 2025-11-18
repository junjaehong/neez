import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import FAB from '../components/FAB';
import SearchBar from '../components/SearchBar';
import './CardList.css';

const CardList = () => {
  const navigate = useNavigate();
  const [cards, setCards] = useState([]); // 전체 명함 목록
  const [filteredCards, setFilteredCards] = useState([]); // 검색용
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const userIdx = 1; // ✅ 로그인된 사용자 ID (임시)
  const page = 0;
  const size = 10;
  
  // 명함 목록 API 호출
  useEffect(() => {
    const fetchCards = async () => {
      setLoading(true);
      try {
        const response = await fetch(`http://192.168.70.114:8083/api/bizcards/user/${userIdx}/page?page=${page}&size=${size}`);
        if (!response.ok) {
          throw new Error('명함 목록 불러오기 실패');
        }
        const result = await response.json();
        console.log('API 응답:', result);

        if (result.success && result.data && result.data.content) {
          setCards(result.data.content);
          setFilteredCards(result.data.content);
      } else {
        console.error("API 구조가 예상과 다릅니다:", result);
        setError("데이터 형식이 올바르지 않습니다.");
      }
      
    } catch (error) {
      console.error("명함 목록 불러오기 실패:", error);
      setError("명함 데이터를 불러오는 중 오류가 발생했습니다.");
    } finally {
      // ✅ 무조건 실행 — 로딩 종료
      setLoading(false);
    }
  };

    //     setCards(list);
    //     setFilteredCards(list);
    //   } catch (err) {
    //     console.error(err);
    //     setError('명함 데이터를 불러오는 중 오류가 발생했습니다.');
    //   } finally {
    //     setLoading(false);
    //   }
    // };

    fetchCards();
  }, []);

  const handleBack = () => {
    navigate('/main');
  };

  // const handleHashtagList = () => {
  //   navigate('/hashtaglist');
  // };

  const handleCardDetail = (cardId) => {
  navigate(`/carddetail/${cardId}`);
  };

  const handleSearch = (keyword) => {
    if (!keyword.trim()) {
      setFilteredCards(cards);
      return;
    }
    
    const filtered = cards.filter(card =>
      card.name?.toLowerCase().includes(keyword.toLowerCase()) ||
      card.companyName?.toLowerCase().includes(keyword.toLowerCase()) ||
      card.department?.toLowerCase().includes(keyword.toLowerCase())
    );
    setFilteredCards(filtered);
  };

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
        <div className="cardlist-header">
          <button className="back-button" onClick={handleBack}>
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
                <div className="card-item-header">
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
