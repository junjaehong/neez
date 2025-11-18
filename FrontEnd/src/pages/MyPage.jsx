import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import Logo from '../components/Logo';
import FAB from '../components/FAB';
import './Mypage.css';

const Mypage = () => {
  const navigate = useNavigate();
  const { myCard, logout, deleteAccount } = useApp();
  const [showAccountToggle, setShowAccountToggle] = useState(false);
  const [showDeletePopup, setShowDeletePopup] = useState(false);
  
  const handleBack = () => {
    navigate('/main');
  };
  
  const handleMyCard = () => {
    navigate('/mycard');
  };

  const handleSetting = () => {
    navigate('/setting');
  };

  const handlePasswordChange = () => {
    navigate('/password');
  };

  const handleEmailChange = () => {
    navigate('/email');
  };

  const handleDeleteAccount = () => {
    setShowDeletePopup(true);
  };

  const confirmDelete = () => {
    if (deleteAccount()) {
      alert('계정이 탈퇴되었습니다.');
      navigate('/login');
    }
  };
  
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="mypage-container">
      <div className="mypage-box">

        {/* 마이페이지 헤더 */}
        <div className="mypage-header">
          <button className="back-button" onClick={handleBack}>
            ←
          </button>
          <Logo size="small" />
        </div>

        {/* 반갑습니다 */}
        <div className="welcome-text">
          <h2>반갑습니다. <strong>{myCard.name}</strong>님</h2>
        </div>

        {/* 마이페이지 메뉴 */}
        <div className="mypage-menu">
          <ul>
            <li onClick={handleMyCard}>내 명함 관리</li>
            <li onClick={() => setShowAccountToggle(!showAccountToggle)}
                className={showAccountToggle ? 'active' : ''}
            >
              계정관리 {showAccountToggle ? '▲' : '▼'}
            </li>
            </ul>
            {/* 계정관리 토글 메뉴 */}
            <div className={`account-toggle ${showAccountToggle ? 'open' : ''}`}>
              <div className="toggle-item" onClick={handlePasswordChange}>
                비밀번호 변경
              </div>
              <div className="toggle-item" onClick={handleEmailChange}>
                이메일 변경
              </div>
              <div className="toggle-item delete" onClick={handleDeleteAccount}>
                회원탈퇴
              </div>
            </div>
            <ul>
            <li onClick={handleSetting}>환경설정</li>
          </ul>
        </div>

        {/* 로그아웃 */}
        <div className="logout" onClick={handleLogout}>
          <h3>LOGOUT</h3>
        </div>

        {/* 카메라 버튼 */}
        <FAB />
        
      </div>

      {/* 계정 탈퇴 확인 팝업 */}
      {showDeletePopup && (
        <div className="popup-overlay" onClick={() => setShowDeletePopup(false)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowDeletePopup(false)}>×</button>
            <h3>계정 탈퇴</h3>
            <div className="popup-message">
              <p>정말 탈퇴하시겠습니까?</p>
              <p className="warning">탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.</p>
            </div>
            <div className="popup-actions">
              <button 
                className="cancel-button"
                onClick={() => setShowDeletePopup(false)}
              >
                취소
              </button>
              <button 
                className="delete-button"
                onClick={confirmDelete}
              >
                탈퇴
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Mypage;
