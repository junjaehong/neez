import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import Logo from '../components/Logo';
import './Main.css';

const Main = () => {
  const navigate = useNavigate();
  const { isLoggedIn, currentUser, updateCurrentUser, updateMyCard  } = useApp();
  const [isPopupOpen, setIsPopupOpen] = useState(false);

  const handleSubmit = async (e) => {
  e.preventDefault();
  const updatedData = { name, company, department, position, phone, email };
  
  try {
    await updateMyCard(updatedData); // 서버 반영
    updateCurrentUser(updatedData);   // context 반영 → Main에서 바로 보임
  } catch (err) {
    console.error(err);
  }
};

  // 로그인 체크
  React.useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login');
    }
  }, [isLoggedIn, navigate]);

  const handleMypage = () => {
    navigate('/mypage');
  };

  const handleCardlist = () => {
    navigate('/cardlist');
  };

  const handleMic = () => {
    navigate('/SttCardSelect');
  };

  const handleCardClick = () => setIsPopupOpen(true);
  const handleClosePopup = () => setIsPopupOpen(false);

  return (
    <div className="main-container">
      <div className={`main-box${isPopupOpen ? 'dimmed' : ''}`}>
        
        {/* 메인 헤더 */}
        <div className="main-header app-header">
          <div className="left-logo">
            <img src=".\public\Neez-Logo-S.png" alt="logo" />
          </div>      
          <div className="header-right">
            <div className="icon" onClick={handleMic} title="녹음">
              <img src=".\public\Neez-Mic.png" alt="mic" />
            </div>
            <div className="icon" onClick={handleCardlist} title="명함 목록">
              <img src=".\public\Neez-List.png" alt="list" />
            </div>
            <div className="icon" onClick={handleMypage} title="마이페이지">
              <img src=".\public\Neez-My.png" alt="mypage" />
            </div>
          </div>
        </div>

        {/* 명함 박스 - Context에서 데이터 가져오기 */}
        {/* <div className="main-mycard">My Card</div> */}
        <div className="main-card-content" onClick={handleCardClick}>
          {currentUser && currentUser.name ? (
          <div className="main-card-text">    
            <h3 className="main-card-company">{currentUser.company || ''}</h3>
            <ul>
              <li className="main-card-rank">
                <span className="main-card-name">{currentUser.name}</span> &nbsp;&nbsp;{currentUser.position || ''} | {currentUser.department || ''}
              </li>
              <li className="main-card-number">{currentUser.phone || '전화번호 없음'}</li>
              <li className="main-card-email">{currentUser.email || '이메일 없음'}</li>
            </ul>
          </div>
        ) : (
          <div className="no-card-placeholder">
            <p>등록된 명함 정보가 없습니다</p>
            <button className="btn-primary" onClick={() => navigate('/MyCard')}>'내 명함 관리'에서 등록/수정</button>
          </div>
        )}
      </div>

      </div>
      {/* 팝업창 - Context 데이터 사용 */}
      {isPopupOpen && (
        <div className="popup-content">
          <button className="popup-close" onClick={handleClosePopup}>
            ×
          </button>
          <table>
            <tbody>
              <tr>
                <td>이름</td>
                <td>{currentUser.name}</td>
              </tr>
              <tr>
                <td>회사</td>
                <td>{currentUser.cardCompanyName}</td>
              </tr>
              <tr>
                <td>부서</td>
                <td>{currentUser.department}</td>
              </tr>
              <tr>
                <td>직급</td>
                <td>{currentUser.position}</td>
              </tr>
              <tr>
                <td>휴대전화</td>
                <td>{currentUser.phone}</td>
              </tr>
              <tr>
                <td>이메일</td>
                <td>{currentUser.email}</td>
              </tr>
              {currentUser.address && (
                <tr>
                  <td>주소</td>
                  <td>{currentUser.address}</td>
                </tr>
              )}
              {currentUser.website && (
                <tr>
                  <td>사이트</td>
                  <td>{currentUser.website}</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

    </div>
  );
};

export default Main;
