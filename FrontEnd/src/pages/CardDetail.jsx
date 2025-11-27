import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate, useParams } from 'react-router-dom';
import { loadConfig } from '../api/configLoader';
import { useApp } from '../context/AppContext';
import api from '../api/client';
import { getAuthHeader } from '../api/auth';
import './CardDetail.css';

const CardDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();

  /////////////////////////////////////
  const [baseURL, setBaseURL] = useState('');

  // config.xml에서 baseURL 가져오기
  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const config = await loadConfig();
        setBaseURL(config.baseURL);
      } catch (err) {
        console.error('config 로드 실패:', err);
      }
    };
    fetchConfig();
  }, []);

  useEffect(() => {
    if (baseURL) reloadData();
  }, [id, baseURL]);
  /////////////////////////////////////
  
  // const { addMeetingNote } = useApp();
  
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

  // 뒤로가기
  const handleBack = () => {
    navigate('/cardlist');
  };

  // 카드 데이터 로드
  const reloadData = async () => {
    if (!baseURL) return; // config가 아직 로드되지 않았다면 호출하지 않음
 
    try {
      const [cardRes, memoRes, tagRes] = await Promise.all([
        axios.get(`${baseURL}/api/bizcards/${id}`, { headers: { ...getAuthHeader() } }),
        axios.get(`${baseURL}/api/bizcards/${id}/memo`, { headers: { ...getAuthHeader() } }),
        axios.get(`${baseURL}/api/bizcards/${id}/hashtags`, { headers: { ...getAuthHeader() } })
      ]);

      /////////////////////////////////////
      const cardData = cardRes.data.data;

      if (!cardData) {
        alert("명함을 찾을 수 없습니다.");
        return;
      }

      setCard(cardData);
      setFormData(cardData);
      setMemo(cardData.memoContent || '');
      setHashtags(Array.isArray(tagRes.data) ? tagRes.data : []);
      /////////////////////////////////////
      console.log("명함 상세 데이터:", cardRes.data.data.content);
      // setCard(cardRes.data.data);
      // setFormData(cardRes.data.data);

      // // 메모
      // const memoRes = await axios.get(`${baseURL}/api/bizcards/${id}/memo`, {
      //   headers: { ...getAuthHeader() }
      // });
      // console.log('memo API response:', memoRes.data.data.memoContent);
      // setMemo(memoRes.data.data?.memoContent || '');

      // // 해시태그
      // const tagRes = await axios.get(`${baseURL}/api/bizcards/${id}/hashtags`, {
      //   headers: { ...getAuthHeader() }
      // });
      // console.log('Tag API response:', tagRes.data);
      
      // const tagData = tagRes.data;
      // setHashtags(Array.isArray(tagData) ? tagData : (tagData.hashTags || []));
      
      // 기업 정보
      // const companyInfo = await axios.get(`${baseURL}/companies/${cardRes.data.data.companyIdx}`);
      // console.log('companyInfo API response:', companyInfo.data);

      // 회의록 리스트 API 입력 후 삭제 ▼
        const defaultMeetings = [
          {
            date: "2025-11-04",
            company: "NaverCloud",
            content: "신규 프로젝트 일정 조율 및 역할 분담 논의...",
          },
          {
            date: "2025-10-25",
            company: "Naver",
            content: "구 프로젝트 일정 조율 및 역할 분담 논의...",
          }
        ];

        setCard({
          ...cardRes.data.data,            // API에서 받은 카드 데이터
          meetingNotes: cardRes.data.data.meetingNotes || defaultMeetings  // 기존 회의록 유지
        });
        // 회의록 리스트 API 입력 후 삭제 ▲

    } catch (err) {
      console.error("데이터 불러오기 실패:", err);
    }
  };


  // 입력 변경
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  // const handleEdit = () => {
  //   if (editMode) {
  //     handleSave();
  //   }
  //   setEditMode(!editMode);
  // };
  // const reloadData = () => fetchData();
  
  // 메모 저장
  const handleSaveMemo = async () => {
    const memoToSend = (memo || '').trim();
    if (!memoToSend) {
      // alert("메모가 비어 있습니다.");
      return;
    }
    try {
      const payload = { memoContent: memoToSend };
      console.log("PATCH 요청 body:", payload);

      const res = await axios.patch(`${baseURL}/api/bizcards/${id}/memo`,
        { memo: memoToSend},
        { headers: { 
          'Content-Type': 'application/json',
          ...getAuthHeader() }
      });
      console.log("메모 저장 성공", res.data);
      setMemo(res.data.data?.memoContent || memoToSend); // 서버 반환값 반영
    } catch (err) {
      console.error("메모 저장 실패:", err.response?.data || err);
      alert("메모 저장 실패");
    }
  };

  // 명함 수정
  const handleSave = async () => {
    try {
      // 1. 명함 기본 정보 저장 (memo, hashtags 제외)
      await axios.put(`${baseURL}/api/bizcards/${id}`, formData, {
        headers: { ...getAuthHeader() }
      });

      // 2. 메모 저장
      await handleSaveMemo(); // memoContent는 PATCH로 별도 저장

      // 3. 해시태그는 개별 추가/삭제로 관리 (UI에서 이미 반영되므로 필요시 서버 동기화)

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
    if (!window.confirm('이 명함을 삭제하시겠습니까?')) return;
      try {
        await axios.delete(`${baseURL}/api/bizcards/${id}`, {
          headers: { ...getAuthHeader() }
        });
        alert('명함이 삭제되었습니다.');
        navigate('/cardlist');
      } catch (err) {
        console.error('명함 삭제 실패:', err);
        alert('삭제 중 오류 발생');
      }
  };
  
  // 해시태그 삭제
  const handleDeleteHashtag = async (tag) => {
    try {
      await axios.delete(`${baseURL}/api/bizcards/${id}/hashtags/${encodeURIComponent(tag)}`,
      { headers: { ...getAuthHeader() } }
    );
      
      // 화면에서도 즉시 삭제
      setHashtags(prev => prev.filter(t => t !== tag));
      setCard(prev => ({
      ...prev,
      hashTags: prev?.hashTags?.filter(t => t !== tag) ?? []
      }));

      // // 서버 반영 후 재조회
      // setHashtags(prev => prev.filter(t => t !== tag)); // 화면 반영
      // reloadData();

    } catch (err) {
      console.error("해시태그 삭제 실패:", err);
      alert("해시태그 삭제에 실패했습니다.");
    }
  };

  // 해시태그 추가
  const handleAddHashtag = async () => {
    if (!newHashtag.trim()) return;
    
    try {
      const res = await axios.post(`${baseURL}/api/bizcards/${id}/hashtags`,
        // JSON.stringify([newHashtag]),
        [newHashtag],   // JSON 배열
        { headers: { ...getAuthHeader() } }
      );    

      // 성공한 경우 화면에도 즉시 추가
      setHashtags(prev => [...prev, newHashtag]);
      setNewHashtag('');

    } catch (err) {
      console.error("해시태그 추가 실패:", err);
      alert("해시태그 추가에 실패했습니다.");
    }
  };

  // 기업 정보 조회
  const handleCompanyInfoClick = async () => {
    if (!card?.companyIdx) {
      return alert("기업 정보가 존재하지 않습니다.");
    }
    try {
      const companyInfo = await axios.get(`${baseURL}/api/companies/${card.companyIdx}`,
        { headers: { ...getAuthHeader() } }
      );
      setCompanyInfo(companyInfo.data);
      setShowCompanyInfo(true);
      console.log('companyInfo API response:', companyInfo.data);
    } catch (err) {
      console.error("기업 정보 조회 실패:", err);
      alert("기업 정보 조회 실패");
    }
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
    // setShowMeetingList(false);
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
        <div className="card-detail-header app-header">
          <button className="back-btn" onClick={handleBack}>←</button>
          <p>명함 상세보기</p>
          
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
            <div className="hashtag-head">
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
                <span key={index} className="card-tag hashtag-font">#{tag}
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
                <button className="hashtag-plus" onClick={handleAddHashtag}>추가</button>
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
                placeholder="예) 12월 중으로 미팅 예정"
                rows="4"
              />
            ) : (
              <p>{memo || '메모 없음'}</p>
            )}
          </div>

          {/* 회의록 섹션 */}
          {card.meetingNotes && (
          <div className="meeting-section">
            <div className="meeting-head">
              <h3>회의록</h3>
              {/* <button 
                className="view-button"
                onClick={() => setShowMeetingList(!showMeetingList)}
              >
                {showMeetingList ? '닫기' : '보기'}
              </button> */}
            </div>
            
            {/* {showMeetingList && ( */}
              <div className="meeting-list">
                {card.meetingNotes.map((meeting, index) => (
                  <div key={meeting.id || index} className="meeting-item">
                    <span className="meeting-date">
                      {new Date(meeting.date).toLocaleDateString()}
                    </span>
                    <span 
                      className="meeting-title"
                      onClick={() => handleViewMeeting(meeting, index)}
                    >
                      {meeting.company}
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
            {/* }) */}
          </div>
          )}
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
          <div className="popup-content companyinfo-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowCompanyInfo(false)}>×</button>
            <h3>기업정보</h3>
            <div className="company-info">
              <p><strong>회사이름 :</strong> {companyInfo.data.name}</p>
              <p><strong>대표이사 :</strong> {companyInfo.data.repName}</p>
              <p><strong>주소 :</strong> {companyInfo.data.address}</p>
              <p><strong>사이트 :</strong> {companyInfo.data.homepage}</p>
              <p><strong>사업자번호 :</strong> {companyInfo.data.bizNo}</p>
              <p><strong>법인번호 :</strong> {companyInfo.data.corpNo}</p>
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
              {/* 클릭한 회의록 회사명 출력 */}
              <h3>{selectedMeeting.company}</h3>
              <p className="meeting-text">{selectedMeeting.content}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CardDetail;