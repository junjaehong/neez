import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate, useParams } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './CardDetail.css';

const CardDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const baseURL = 'http://192.168.70.114:8083/api'; // 공통 prefix
  // const { cardList, updateCard, addHashtagToCard, hashtags, settings, addMeetingNote } = useApp();
  
  const [card, setCard] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({});

  // 메모
  const [memo, setMemo] = useState('');

  // 해시태그
  const [showHashtagInput, setShowHashtagInput] = useState(false);
  const [newHashtag, setNewHashtag] = useState('');
  const [hashtags, setHashtags] = useState([]);

  // 기업 정보 관련
  const [showCompanyInfo, setShowCompanyInfo] = useState(false);
  const [companyInfo, setCompanyInfo] = useState(null);

  // 회의록 관련
  const [showMeetingList, setShowMeetingList] = useState(false);
  const [showMeetingDetail, setShowMeetingDetail] = useState(false);
  const [selectedMeeting, setSelectedMeeting] = useState(null);
  const [currentMeetingIndex, setCurrentMeetingIndex] = useState(0);

  // const [showAddField, setShowAddField] = useState(false);
  // const [newFieldName, setNewFieldName] = useState('');
  // const [newFieldValue, setNewFieldValue] = useState('');
  // const [translatedContent, setTranslatedContent] = useState(null);
  // const [selectedLanguage, setSelectedLanguage] = useState(settings.language || 'ko');

  // 카드 데이터 로드
const reloadData = async () => {
  try {
    const cardRes = await axios.get(`${baseURL}/bizcards/${id}`);
    console.log("명함 상세 데이터:", cardRes.data.data);
    setCard(cardRes.data.data);
    setFormData(cardRes.data.data);

    // 메모
    const memoRes = await axios.get(`${baseURL}/bizcards/${id}/memo`);
    console.log('memo API response:', memoRes.data.data.memoContent);
    setMemo(memoRes.data.data.memoContent || '');

    // 해시태그
    const tagRes = await axios.get(`${baseURL}/bizcards/${id}/hashtags`);
    // console.log(id);
    console.log('Tag API response:', tagRes.data);
    
    const tagData = tagRes.data;
    setHashtags(Array.isArray(tagData) ? tagData : (tagData.hashTags || []));
    
    // 기업 정보
    const companyInfo = await axios.get(`${baseURL}/companies/${cardRes.data.data.companyIdx}`);
    console.log('companyInfo API response:', companyInfo.data);

  } catch (err) {
    console.error("데이터 불러오기 실패:", err);
  }
};

//   fetchData();
// }, [id]);


useEffect(() => {
  reloadData();
}, [id]);


  const handleBack = () => {
    navigate('/cardlist');
  };

  // const handleEdit = () => {
  //   if (editMode) {
  //     handleSave();
  //   }
  //   setEditMode(!editMode);
  // };

  // 입력 변경
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // const reloadData = () => fetchData();
  
  // 메모 저장
  const handleSaveMemo = async () => {
    try {
      await axios.patch(`${baseURL}/${id}/memo`,
        { memo: memo },
        { headers: { 'Content-Type': 'application/json' }
      });
      console.log("메모 저장 성공");
    } catch (err) {
      console.error("메모 저장 실패:", err);
      alert("메모 저장 실패");
    }
  };

  // 명함 수정
  const handleSave = async () => {
    try {
      // 명함 정보 저장
      await axios.put(`${baseURL}/${id}`, {
        ...formData,
        // memo: memo   // 메모 함께 전송
      });
      await handleSaveMemo(); // 메모는 별도로 PATCH
      
      alert("수정이 모두 저장되었습니다.");
      
      setEditMode(false);
      reloadData(); // 최신 상태 재조회  
      
    } catch (err) {
      console.error(err);
      alert("저장 중 오류 발생");
    }
  };

  
  // 명함 삭제
  const handleDelete = async () => {
    if (window.confirm('정말 이 명함을 삭제하시겠습니까?')) {
      try {
        await axios.delete(`${baseURL}/${id}`);
        alert('명함이 삭제되었습니다.');
        navigate('/cardlist');
      } catch (err) {
        console.error('명함 삭제 실패:', err);
        alert('삭제 중 오류 발생');
      }
      
    }
  };
  
  // 해시태그 삭제
  const handleDeleteHashtag = async (tag) => {
    try {
      await axios.delete(`${baseURL}/${id}/hashtags/${encodeURIComponent(tag)}`);

      // 화면에서도 즉시 삭제
      setHashtags(prev => prev.filter(t => t !== tag));
      setCard(prevCard => ({
      ...prevCard,
      hashTags: prevCard.hashTags.filter(t => t !== tag)
      }));

    } catch (err) {
      console.error("해시태그 삭제 실패:", err);
      alert("해시태그 삭제에 실패했습니다.");
    }
  };

  
  // 해시태그 추가
  const handleAddHashtag = async () => {
    if (!newHashtag.trim()) return;
    // const tagRes = await axios.get(`${baseURL}/${id}/hashtags`);
    //   console.log("해시태그 조회:", tagRes.data);
    
    try {
      const res = await axios.post(`${baseURL}/${id}/hashtags`,
        JSON.stringify([newHashtag]),
       { headers: { "Content-Type": "application/json" } }
    );

      // 성공한 경우 화면에도 즉시 추가
      setHashtags(prev => [...prev, newHashtag]);
      setNewHashtag('');

    } catch (err) {
      console.error("해시태그 추가 실패:", err);
      alert("해시태그 추가에 실패했습니다.");
    }
  };

  
  // 기업 정보 조회 (OpenAI 시뮬레이션)
  const handleCompanyInfoClick = async () => {
    // 실제로는 OpenAI API 호출
    const mockCompanyInfo = {
      name: formData.company || card.company,
      industry: 'Cloud Computing',
      employees: '5000+',
      founded: '2010',
      headquarters: '서울특별시 강남구',
      revenue: '연매출 1조원',
      description: `${formData.company || card.company}는 클라우드 컴퓨팅 서비스를 제공하는 선도적인 기업입니다. 
      AI, 빅데이터, IoT 등 다양한 분야에서 혁신적인 솔루션을 제공하고 있습니다.`,
      services: ['Cloud Infrastructure', 'AI Platform', 'Data Analytics', 'Security Solutions']
    };
    setCompanyInfo(mockCompanyInfo);
    setShowCompanyInfo(true);
  };


  // 회의록 삭제
  const handleDeleteMeeting = (meetingId) => {
    const updatedMeetings = card.meetingNotes.filter(note => note.id !== meetingId);
    updateCard(card.id, { ...card, meetingNotes: updatedMeetings });
    setCard({ ...card, meetingNotes: updatedMeetings });
    if (updatedMeetings.length === 0) {
      setShowMeetingList(false);
    }
  };

  // 회의록 상세 보기
  const handleViewMeeting = (meeting, index) => {
    setSelectedMeeting(meeting);
    setCurrentMeetingIndex(index);
    setShowMeetingDetail(true);
    setShowMeetingList(false);
  };

  // 회의록 네비게이션
  const navigateMeeting = (direction) => {
    const newIndex = currentMeetingIndex + direction;
    if (newIndex >= 0 && newIndex < card.meetingNotes.length) {
      setCurrentMeetingIndex(newIndex);
      setSelectedMeeting(card.meetingNotes[newIndex]);
    }
  };

  // // 번역 기능
  // const handleTranslate = async (targetLang) => {
  //   // 실제로는 번역 API 호출
  //   const mockTranslations = {
  //     ko: {
  //       position: formData.position || card.position,
  //       department: formData.department || card.department,
  //       memo: memo
  //     },
  //     en: {
  //       position: 'Manager',
  //       department: 'Planning Team',
  //       memo: 'Important client for the project'
  //     },
  //     ja: {
  //       position: 'マネージャー',
  //       department: '企画チーム',
  //       memo: 'プロジェクトの重要なクライアント'
  //     }
  //   };
  //   setTranslatedContent(mockTranslations[targetLang]);
  //   setSelectedLanguage(targetLang);
  // };

  if (!card) return <div>Loading...</div>;

  return (
    <div className="card-detail-container">
      <div className="card-detail-box">
        <div className="card-detail-header">
          <button className="back-button" onClick={handleBack}>←</button>
          <h2>명함 상세보기</h2>
          
        </div>

        {/* 번역 버튼
        <div className="translation-buttons">
          <button 
            className={selectedLanguage === 'ko' ? 'active' : ''}
            onClick={() => handleTranslate('ko')}
          >
            한국어
          </button>
          <button 
            className={selectedLanguage === 'en' ? 'active' : ''}
            onClick={() => handleTranslate('en')}
          >
            English
          </button>
          <button 
            className={selectedLanguage === 'ja' ? 'active' : ''}
            onClick={() => handleTranslate('ja')}
          >
            日本語
          </button>
        </div> */}

        <div className="card-detail-content">
          <table className="card-info-table">
            <tbody>
              <tr>
                <td>이름</td>
                <td>
                  {editMode ? (
                    <input name="name" value={formData.name || ''} onChange={handleInputChange} />
                  ) : (
                    card.name
                  )}
                </td>
              </tr>
              <tr>
                <td>회사</td>
                <td>
                  <div className='company-group'>
                    {editMode ? (
                      <input name="cardCompanyName" value={formData.cardCompanyName || ''} onChange={handleInputChange} />
                    ) : (
                      card.cardCompanyName
                    )}
                    <button className='companyinfo-btn' onClick={handleCompanyInfoClick}>기업정보</button>
                  </div>
                </td>
              </tr>
              <tr>
                <td>직급</td>
                <td>
                  {editMode ? (
                    <input name="position" value={formData.position || ''} onChange={handleInputChange} />
                  ) : (
                    card.position
                  )}
                </td>
              </tr>
              <tr>
                <td>부서</td>
                <td>
                  {editMode ? (
                    <input name="department" value={formData.department || ''} onChange={handleInputChange} />
                  ) : (
                    card.department
                  )}
                </td>
              </tr>
              <tr>
                <td>번호</td>
                <td>
                  {editMode ? (
                    <input name="phoneNumber" value={formData.phoneNumber || ''} onChange={handleInputChange} />
                  ) : (
                    card.phoneNumber
                  )}
                </td>
              </tr>
              <tr>
                <td>이메일</td>
                <td>
                  {editMode ? (
                    <input name="email" value={formData.email || ''} onChange={handleInputChange} />
                  ) : (
                    card.email
                  )}
                </td>
              </tr>
              <tr>
                <td>주소</td>
                <td>
                  {editMode ? (
                    <input name="address" value={formData.address || ''} onChange={handleInputChange} />
                  ) : (
                    card.address
                  )}
                </td>
              </tr>
              <tr>
                <td>유선번호</td>
                <td>
                  {editMode ? (
                    <input name="lineNumber" value={formData.lineNumber || ''} onChange={handleInputChange} />
                  ) : (
                    card.lineNumber
                  )}
                </td>
              </tr>
              <tr>
                <td>팩스번호</td>
                <td>
                  {editMode ? (
                    <input name="faxNumber" value={formData.faxNumber || ''} onChange={handleInputChange} />
                  ) : (
                    card.faxNumber
                  )}
                </td>
              </tr>
            </tbody>
          </table>

          {/* 해시태그 섹션 */}
          <div className="hashtag-section">
            <div className="section-header">
              <h3>해시태그</h3>
              {editMode && (
                <button 
                  className="add-button"
                  onClick={() => setShowHashtagInput(!showHashtagInput)}
                >
                  +
                </button>
              )}
            </div>

            {card.hashTags && card.hashTags.length > 0 && (
            <div className="card-tags">
              {card.hashTags.map((tag, index) => (
                <span key={index} className="card-tag">#{tag}
                {editMode && (
                  <button 
                    className="hashtag-delete"
                    onClick={() => handleDeleteHashtag(tag)}
                  >
                    ×
                  </button>
                )}
                </span>
              ))}
            </div>
            )}
            
            <div className="hashtag-list">
              {hashtags.map((tag, i) => (
                <span key={i} className="card-tag">
                  #{tag}
                  {editMode && (
                    <button 
                      className="hashtag-delete"
                      onClick={() => handleDeleteHashtag(tag)}
                    >
                      ×
                    </button>
                  )}
                </span>
              ))}
            </div>

            {editMode && showHashtagInput && (
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
            {editMode ? (
              <textarea
                value={memo}
                onChange={(e) => setMemo(e.target.value)}
                placeholder="메모를 입력하세요"
                rows="4"
              />
            ) : (
              <p>{memo || '메모 없음'}</p>
            )}
          </div>

          {/* 회의록 섹션 */}
          {card.meetingNotes && card.meetingNotes.length > 0 && (
          <div className="meeting-section">
            <div className="section-header">
              <h3>회의록</h3>
              <button 
                className="view-button"
                onClick={() => setShowMeetingList(!showMeetingList)}
              >
                {showMeetingList ? '닫기' : '보기'}
              </button>
            </div>
            
            {showMeetingList && (
              <div className="meeting-list">
                {card.meetingNotes.map((meeting, index) => (
                  <div key={meeting.id} className="meeting-item">
                    <span 
                      className="meeting-date"
                      onClick={() => handleViewMeeting(meeting, index)}
                    >
                      {new Date(meeting.date).toLocaleDateString()}
                    </span>
                    <button 
                      className="delete-meeting-btn"
                      onClick={() => handleDeleteMeeting(meeting.id)}
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
          )}
            {/* <div className="meeting-section">
              <div className="section-header">
                <h3>회의록</h3>
                <button 
                  className="view-button"
                 >
                </button>
              </div>
              
                <div className="meeting-list">
                    <div  className="meeting-item">
                      <span className="meeting-date"></span>
                      <button className="delete-meeting-btn" >
                        ×
                      </button>
                    </div>
                </div>
            </div> */}
        </div>

        {/* 수정, 저장, 삭제 btn */}
          <div className="bottom-btn-group">
            {editMode ? (
              <button className="edit-btn" onClick={handleSave}>저장</button>
            ) : (
              <button className="edit-btn" onClick={() => setEditMode(true)}>수정</button>
            )}
              <button className="delete-btn" onClick={handleDelete}>삭제</button>
          </div>

      </div>

      {/* 회사 정보 팝업 */}
      {showCompanyInfo && companyInfo && (
        <div className="popup-overlay" onClick={() => setShowCompanyInfo(false)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowCompanyInfo(false)}>×</button>
            <h3>{companyInfo.name}</h3>
            <div className="company-info">
              <p><strong>회사이름:</strong> {companyInfo.industry}</p>
              <p><strong>대표이사:</strong> {companyInfo.employees}</p>
              <p><strong>주소:</strong> {companyInfo.revenue}</p>
              <p><strong>사이트:</strong> {companyInfo.description}</p>
              <p><strong>사업자번호:</strong> {companyInfo.founded}</p>
              <p><strong>법인번호:</strong> {companyInfo.headquarters}</p>
              <div className="services">
                <strong>주요 서비스:</strong>
                <ul>
                  {companyInfo.services.map((service, index) => (
                    <li key={index}>{service}</li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 회의록 상세 팝업 */}
      {showMeetingDetail && selectedMeeting && (
        <div className="popup-overlay" onClick={() => setShowMeetingDetail(false)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowMeetingDetail(false)}>×</button>
            
            <div className="meeting-navigation">
              <button 
                onClick={() => navigateMeeting(-1)}
                disabled={currentMeetingIndex === 0}
              >
                ←
              </button>
              <span className="meeting-date">
                {new Date(selectedMeeting.date).toLocaleDateString()}
              </span>
              <button 
                onClick={() => navigateMeeting(1)}
                disabled={currentMeetingIndex === card.meetingNotes.length - 1}
              >
                →
              </button>
            </div>
            
            <div className="meeting-content">
              <h3>{card.company}</h3>
              <p className="meeting-text">{selectedMeeting.content}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CardDetail;