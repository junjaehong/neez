import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import FAB from '../components/FAB';
import SearchBar from '../components/SearchBar';
import './CardList.css';

const CardList = () => {
  const navigate = useNavigate();
  const { cardList, deleteCard } = useApp();
  const [filteredCards, setFilteredCards] = useState(cardList);
  const [selectedCard, setSelectedCard] = useState(null);

  const handleBack = () => {
    navigate('/main');
  };

  const handleHashtagList = () => {
    navigate('/hashtaglist');
  };

  const handleCardDetail = () => {
    navigate('/carddetail');
  };

  const handleSearch = (keyword) => {
    if (!keyword.trim()) {
      setFilteredCards(cardList);
      return;
    }
    
    const filtered = cardList.filter(card => 
      card.name.toLowerCase().includes(keyword.toLowerCase()) ||
      card.company.toLowerCase().includes(keyword.toLowerCase()) ||
      (card.department && card.department.toLowerCase().includes(keyword.toLowerCase()))
    );
    setFilteredCards(filtered);
  };

  // const handleCardClick = (card) => {
  //   setSelectedCard(card);
  // };

  // const handleClosePopup = () => {
  //   setSelectedCard(null);
  // };

  // const handleDeleteCard = (e, cardId) => {
  //   e.stopPropagation();
  //   if (window.confirm('정말로 이 명함을 삭제하시겠습니까?')) {
  //     deleteCard(cardId);
  //     setFilteredCards(prev => prev.filter(card => card.id !== cardId));
  //   }
  // };

  React.useEffect(() => {
    setFilteredCards(cardList);
  }, [cardList]);

  return (
    <div className="cardlist-container">
      <div className="cardlist-box">

        {/* 명함 목록 헤더 */}
        <div className="cardlist-header">
          <button className="back-button" onClick={handleBack}>
            ←
          </button>
          <div className="hashtag-icon" onClick={handleHashtagList}>#</div>
        </div>

        {/* 명함 검색 */}
        <SearchBar onSearch={handleSearch} />

        {/* 명함 항목 */}
        <div className="cardlist">
          {filteredCards.length > 0 ? (
            filteredCards.map(card => (
              <div key={card.id} className="card-item" onClick={handleCardDetail}>
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
                  <div className="card-item-company">{card.company}</div>
                  <div className="card-item-position">
                    {card.position && `${card.position}`}
                    {card.position && card.department && ' | '}
                    {card.department && `${card.department}`}
                  </div>
                  <div className="card-item-contact">
                    {card.phone && `📞 ${card.phone}`}
                    {card.phone && card.email && ' | '}
                    {card.email && `✉️ ${card.email}`}
                  </div>
                </div>
                {card.tags && card.tags.length > 0 && (
                  <div className="card-tags">
                    {card.tags.map((tag, index) => (
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

      {/* 명함 상세 팝업
      {selectedCard && (
        <div className="popup-content">
          <button className="popup-close" onClick={handleClosePopup}>
            ×
          </button>
          <table>
            <tbody>
              <tr>
                <td>이름</td>
                <td>{selectedCard.name}</td>
              </tr>
              {selectedCard.position && (
                <tr>
                  <td>직급</td>
                  <td>{selectedCard.position}</td>
                </tr>
              )}
              {selectedCard.department && (
                <tr>
                  <td>부서</td>
                  <td>{selectedCard.department}</td>
                </tr>
              )}
              <tr>
                <td>회사</td>
                <td>{selectedCard.company}</td>
              </tr>
              {selectedCard.phone && (
                <tr>
                  <td>전화번호</td>
                  <td>{selectedCard.phone}</td>
                </tr>
              )}
              {selectedCard.email && (
                <tr>
                  <td>이메일</td>
                  <td>{selectedCard.email}</td>
                </tr>
              )}
              {selectedCard.address && (
                <tr>
                  <td>주소</td>
                  <td>{selectedCard.address}</td>
                </tr>
              )}
              {selectedCard.website && (
                <tr>
                  <td>웹사이트</td>
                  <td>{selectedCard.website}</td>
                </tr>
              )}
              {selectedCard.memo && (
                <tr>
                  <td>메모</td>
                  <td>{selectedCard.memo}</td>
                </tr>
              )}
              <tr>
                <td>등록일</td>
                <td>{selectedCard.createdAt}</td>
              </tr>
            </tbody>
          </table>
        </div>
      )} */}
    </div>
  );
};

export default CardList;
