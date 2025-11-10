import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './CardDetail.css';

const CardDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { cardList, updateCard, addHashtagToCard, hashtags, settings } = useApp();
  const [card, setCard] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({});
  const [memo, setMemo] = useState('');
  const [showHashtagInput, setShowHashtagInput] = useState(false);
  const [newHashtag, setNewHashtag] = useState('');
  const [showCompanyInfo, setShowCompanyInfo] = useState(false);
  const [showMeetingNotes, setShowMeetingNotes] = useState(false);
  const [currentNoteIndex, setCurrentNoteIndex] = useState(0);
  const [showTranslatePopup, setShowTranslatePopup] = useState(false);

  useEffect(() => {

    // if (!cardList || cardList.length === 0) return;
    // 타입 일치 비교 (문자열과 숫자 모두 대응)
    // const foundCard = cardList.find(c => String(c.id) === String(id));

    const foundCard = cardList.find(c => c.id === parseInt(id));
    if (foundCard) {
      setCard(foundCard);
      setFormData(foundCard);
      setMemo(foundCard.memo || '');
    }
  }, [id, cardList]);

  const handleBack = () => {
    navigate('/cardlist');
  };

  const handleEdit = () => {
    setEditMode(!editMode);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSave = () => {
    updateCard(card.id, formData);
    setCard({ ...card, ...formData });
    setEditMode(false);
  };

  const handleMemoSave = () => {
    updateCard(card.id, { ...card, memo });
  };

  const handleAddHashtag = () => {
    if (newHashtag.trim()) {
      addHashtagToCard(card.id, newHashtag);
      setNewHashtag('');
      setShowHashtagInput(false);
      // 카드 정보 새로고침
      const updatedCard = cardList.find(c => c.id === card.id);
      setCard(updatedCard);
    }
  };

  const handleCompanyInfoClick = () => {
    // 실제로는 chatGPT API 호출
    const mockCompanyInfo = {
      industry: 'Cloud Computing',
      employees: '5000+',
      founded: '2010',
      headquarters: '서울특별시 강남구',
      description: 'NaverCloud는 클라우드 컴퓨팅 서비스를 제공하는 선도적인 기업입니다.'
    };
    updateCard(card.id, { ...card, companyInfo: mockCompanyInfo });
    setShowCompanyInfo(true);
  };

  const handleDeleteMeetingNote = (noteId) => {
    const updatedNotes = card.meetingNotes.filter(note => note.id !== noteId);
    updateCard(card.id, { ...card, meetingNotes: updatedNotes });
    setCard({ ...card, meetingNotes: updatedNotes });
  };

  const translations = {
    ko: { title: '명함 상세보기', edit: '수정', save: '저장' },
    en: { title: 'Card Detail', edit: 'Edit', save: 'Save' },
    ja: { title: '名刺詳細', edit: '編集', save: '保存' }
  };

  const t = translations[settings.language] || translations.ko;

  if (!card) return <div>Loading...</div>;

  return (
    <div className="card-detail-container">
      <div className="card-detail-box">
        <div className="card-detail-header">
          <button className="back-button" onClick={handleBack}>←</button>
          <h2>{t.title}</h2>
          <button className="edit-button" onClick={handleEdit}>
            {editMode ? t.save : t.edit}
          </button>
        </div>

        <div className="card-detail-content">
          <table className="card-info-table">
            <tbody>
              <tr>
                <td>이름</td>
                <td>
                  {editMode ? (
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                    />
                  ) : (
                    card.name
                  )}
                </td>
              </tr>
              <tr>
                <td>직급</td>
                <td>
                  {editMode ? (
                    <input
                      type="text"
                      name="position"
                      value={formData.position}
                      onChange={handleInputChange}
                    />
                  ) : (
                    card.position
                  )}
                </td>
              </tr>
              <tr>
                <td>부서</td>
                <td>
                  {editMode ? (
                    <input
                      type="text"
                      name="department"
                      value={formData.department}
                      onChange={handleInputChange}
                    />
                  ) : (
                    card.department
                  )}
                </td>
              </tr>
              <tr>
                <td>회사 이름</td>
                <td>
                  <span 
                    className="company-link"
                    onClick={handleCompanyInfoClick}
                  >
                    {card.company}
                  </span>
                </td>
              </tr>
              <tr>
                <td>전화번호</td>
                <td>
                  {editMode ? (
                    <input
                      type="text"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                    />
                  ) : (
                    card.phone
                  )}
                </td>
              </tr>
              <tr>
                <td>이메일</td>
                <td>
                  {editMode ? (
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                    />
                  ) : (
                    card.email
                  )}
                </td>
              </tr>
            </tbody>
          </table>

          {editMode && (
            <button className="save-button" onClick={handleSave}>
              저장
            </button>
          )}

          {/* 해시태그 섹션 */}
          <div className="hashtag-section">
            <div className="section-header">
              <h3>해시태그</h3>
              <button 
                className="add-button"
                onClick={() => setShowHashtagInput(!showHashtagInput)}
              >
                +
              </button>
            </div>
            <div className="hashtag-list">
              {card.hashtags?.map((tag, index) => (
                <span key={index} className="hashtag">{tag}</span>
              ))}
            </div>
            {showHashtagInput && (
              <div className="hashtag-input">
                <input
                  type="text"
                  value={newHashtag}
                  onChange={(e) => setNewHashtag(e.target.value)}
                  placeholder="새 해시태그"
                />
                <button onClick={handleAddHashtag}>추가</button>
              </div>
            )}
          </div>

          {/* 메모 섹션 */}
          <div className="memo-section">
            <h3>메모</h3>
            <textarea
              value={memo}
              onChange={(e) => setMemo(e.target.value)}
              placeholder="메모를 입력하세요"
              rows="4"
            />
            <button onClick={handleMemoSave}>저장</button>
          </div>

          {/* 회의록 요약본 */}
          {card.meetingNotes && card.meetingNotes.length > 0 && (
            <div className="meeting-notes-section">
              <h3>회의록 요약본</h3>
              <button 
                className="view-notes-button"
                onClick={() => setShowMeetingNotes(true)}
              >
                보기
              </button>
            </div>
          )}
        </div>
      </div>

      {/* 회사 정보 팝업 */}
      {showCompanyInfo && card.companyInfo && (
        <div className="popup-overlay" onClick={() => setShowCompanyInfo(false)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowCompanyInfo(false)}>×</button>
            <h3>{card.company}</h3>
            <div className="company-info">
              <p><strong>업종:</strong> {card.companyInfo.industry}</p>
              <p><strong>직원수:</strong> {card.companyInfo.employees}</p>
              <p><strong>설립:</strong> {card.companyInfo.founded}</p>
              <p><strong>본사:</strong> {card.companyInfo.headquarters}</p>
              <p><strong>소개:</strong> {card.companyInfo.description}</p>
            </div>
          </div>
        </div>
      )}

      {/* 회의록 팝업 */}
      {showMeetingNotes && card.meetingNotes && card.meetingNotes.length > 0 && (
        <div className="popup-overlay" onClick={() => setShowMeetingNotes(false)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowMeetingNotes(false)}>×</button>
            <h3>회의록 요약본</h3>
            <div className="meeting-note-navigation">
              <button 
                onClick={() => setCurrentNoteIndex(Math.max(0, currentNoteIndex - 1))}
                disabled={currentNoteIndex === 0}
              >
                ←
              </button>
              <span>{new Date(card.meetingNotes[currentNoteIndex].date).toLocaleDateString()}</span>
              <button 
                onClick={() => setCurrentNoteIndex(Math.min(card.meetingNotes.length - 1, currentNoteIndex + 1))}
                disabled={currentNoteIndex === card.meetingNotes.length - 1}
              >
                →
              </button>
            </div>
            <div className="meeting-note-content">
              <p>{card.meetingNotes[currentNoteIndex].content}</p>
            </div>
            <button 
              className="delete-note-button"
              onClick={() => handleDeleteMeetingNote(card.meetingNotes[currentNoteIndex].id)}
            >
              삭제
            </button>
          </div>
        </div>
      )}

      {/* 번역 언어 선택 팝업 */}
      {showTranslatePopup && (
        <div className="popup-overlay" onClick={() => setShowTranslatePopup(false)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowTranslatePopup(false)}>×</button>
            <h3>번역 언어 선택</h3>
            <div className="language-options">
              <label>
                <input type="radio" name="language" value="ko" />
                한국어
              </label>
              <label>
                <input type="radio" name="language" value="en" />
                English
              </label>
              <label>
                <input type="radio" name="language" value="ja" />
                日本語
              </label>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CardDetail;