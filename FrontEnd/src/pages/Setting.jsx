import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './Setting.css';

const Setting = () => {
  const navigate = useNavigate();
  const { settings, updateSettings } = useApp();

  // 여러 boolean 대신 하나의 토글키를 사용
  // possible values: 'language' | 'theme' | 'font' | null
  const [openPanel, setOpenPanel] = useState(null);

  const handleBack = () => {
    navigate('/mypage');
  };

  const handleLanguageChange = (lang) => {
    updateSettings({ language: lang });
    setOpenPanel(null);
  };

  const handleThemeChange = (isDark) => {
    updateSettings({ darkMode: isDark });
    setOpenPanel(null);
  };

  const handleFontSizeChange = (size) => {
    updateSettings({ fontSize: size });
    setOpenPanel(null);
  };

  const getLanguageDisplay = () => {
    switch(settings.language) {
      case 'ko': return '한국어';
      case 'en': return 'English';
      case 'ja': return '日本語';
      default: return '한국어';
    }
  };

  const getFontSizeDisplay = () => {
    switch(settings.fontSize) {
      case 'small': return '작게';
      case 'medium': return '보통';
      case 'large': return '크게';
      default: return '보통';
    }
  };

  // 다국어 텍스트 테이블
  const langText = {
    ko: {
      title: "환경설정",
      language: "언어 설정",
      theme: "화면 모드",
      font: "글자 크기 설정",
      service: "서비스 이용 방법",
      about: "정보",
      light: "라이트",
      dark: "다크",
      small: "작게",
      medium: "보통",
      large: "크게",
    },
    en: {
      title: "Settings",
      language: "Language",
      theme: "Theme",
      font: "Font Size",
      service: "How to Use",
      about: "About",
      light: "Light",
      dark: "Dark",
      small: "Small",
      medium: "Medium",
      large: "Large",
    },
    ja: {
      title: "設定",
      language: "言語設定",
      theme: "テーマ",
      font: "文字サイズ",
      service: "サービスの使い方",
      about: "情報",
      light: "ライト",
      dark: "ダーク",
      small: "小",
      medium: "中",
      large: "大",
    },
  };

  // 폰트 크기 스타일
  const fontStyle = {
    small: { fontSize: "14px" },
    medium: { fontSize: "16px" },
    large: { fontSize: "18px" },
  };


  return (
    <div className="setting-container">
      <div className="setting-box">
        <div className="setting-header app-header">
          <button className="back-btn" onClick={handleBack}>←</button>
          <p>{langText[settings.language].title}</p>
          <div></div>
        </div>

        <div className="setting-content">
          {/* 언어 설정 */}
          <div className="setting-item">
            <div 
              className={`setting-row ${openPanel === 'language' ? 'active' : ''}`}
              onClick={() => setOpenPanel(openPanel === 'language' ? null : 'language')}
            >
              <span>{langText[settings.language].language}</span>
              <span className="setting-value">
                {getLanguageDisplay()}
                {/* {showFontToggle ? '▲' : '▼'} */}
              </span>
            </div>
            <div className={`toggle-menu ${openPanel === 'language' ? 'open' : ''}`}>
              <div 
                className={`toggle-option ${settings.language === 'ko' ? 'selected' : ''}`}
                onClick={() => handleLanguageChange('ko')}
              >
                <span>한국어</span>
                {settings.language === 'ko' && <span className="check">✓</span>}
              </div>
              <div 
                className={`toggle-option ${settings.language === 'en' ? 'selected' : ''}`}
                onClick={() => handleLanguageChange('en')}
              >
                <span>English</span>
                {settings.language === 'en' && <span className="check">✓</span>}
              </div>
              <div 
                className={`toggle-option ${settings.language === 'ja' ? 'selected' : ''}`}
                onClick={() => handleLanguageChange('ja')}
              >
                <span>日本語</span>
                {settings.language === 'ja' && <span className="check">✓</span>}
              </div>
            </div>
          </div>

          {/* 화면 모드 */}
          <div className="setting-item">
            <div 
              className={`setting-row ${openPanel === 'theme' ? 'active' : ''}`}
              onClick={() => setOpenPanel(openPanel === 'theme' ? null : 'theme')}
            >
              <span>{langText[settings.language].theme}</span>
              <span className="setting-value">
                {settings.darkMode ? '다크' : '라이트'}
                {/* {showThemeToggle ? '▲' : '▼'} */}
              </span>
            </div>
            <div className={`toggle-menu ${openPanel === 'theme' ? 'open' : ''}`}>
              <div 
                className={`toggle-option ${!settings.darkMode ? 'selected' : ''}`}
                onClick={() => handleThemeChange(false)}
              >
                <span>☀️ {langText[settings.language].light}</span>
                {!settings.darkMode && <span className="check">✓</span>}
              </div>
              <div 
                className={`toggle-option ${settings.darkMode ? 'selected' : ''}`}
                onClick={() => handleThemeChange(true)}
              >
                <span>🌙 {langText[settings.language].dark}</span>
                {settings.darkMode && <span className="check">✓</span>}
              </div>
            </div>
          </div>

          {/* 글자 크기 설정 */}
          <div className="setting-item">
            <div 
              className={`setting-row ${openPanel === 'font' ? 'active' : ''}`}
              onClick={() => setOpenPanel(openPanel === 'font' ? null : 'font')}
            >
              <span>{langText[settings.language].font}</span>
              <span className="setting-value">
                {getFontSizeDisplay()}
                {/* {showFontToggle ? '▲' : '▼'} */}
              </span>
            </div>
            <div className={`toggle-menu ${openPanel === 'font' ? 'open' : ''}`}>
              <div 
                className={`toggle-option ${settings.fontSize === 'small' ? 'selected' : ''}`}
                onClick={() => handleFontSizeChange('small')}
              >
                <span style={{fontSize: '14px'}}>
                  {langText[settings.language].small}
                </span>
                {settings.fontSize === 'small' && <span className="check">✓</span>}
              </div>
              <div 
                className={`toggle-option ${settings.fontSize === 'medium' ? 'selected' : ''}`}
                onClick={() => handleFontSizeChange('medium')}
              >
                <span style={{fontSize: '16px'}}>
                  {langText[settings.language].medium}
                </span>
                {settings.fontSize === 'medium' && <span className="check">✓</span>}
              </div>
              <div 
                className={`toggle-option ${settings.fontSize === 'large' ? 'selected' : ''}`}
                onClick={() => handleFontSizeChange('large')}
              >
                <span style={{fontSize: '18px'}}>
                  {langText[settings.language].large}
                </span>
                {settings.fontSize === 'large' && <span className="check">✓</span>}
              </div>
            </div>
          </div>

          {/* 서비스 이용 방법 */}
          <div className="setting-item">
            <div 
              className={`setting-row ${openPanel === 'service' ? 'active' : ''}`}
              onClick={() => setOpenPanel('service')}
            >
              <span>{langText[settings.language].service}</span>
            </div>
          </div>

          {/* 정보 */}
          <div className="setting-item">
            <div 
              className={`setting-row ${openPanel === 'about' ? 'active' : ''}`}
              onClick={() => setOpenPanel('about')}
            >
              <span>{langText[settings.language].about}</span>
            </div>
          </div>
        </div>
      </div>

      {/* 서비스 이용 방법 팝업 */}
      {openPanel === 'service' && (
        <div className="popup-overlay" onClick={() => setOpenPanel(null)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setOpenPanel(null)}>×</button>
            <h3>서비스 이용 방법</h3>
            <div className="info-content">
              <h4>1. 명함 등록</h4>
              <p>카메라 버튼을 눌러 명함을 촬영하거나 직접 입력할 수 있습니다.</p>
              
              <h4>2. 명함 관리</h4>
              <p>등록된 명함은 목록에서 확인하고 검색할 수 있습니다.</p>
              
              <h4>3. 해시태그</h4>
              <p>명함에 해시태그를 추가하여 분류할 수 있습니다.</p>
              
              <h4>4. 회의록 기능</h4>
              <p>STT 기능을 사용하여 회의 내용을 자동으로 기록하고 번역할 수 있습니다.</p>
              
              <h4>5. 내 명함 관리</h4>
              <p>마이페이지에서 내 명함 정보를 수정할 수 있습니다.</p>
            </div>
          </div>
        </div>
      )}

      {/* 정보 팝업 */}
      {openPanel === 'about' && (
        <div className="popup-overlay" onClick={() => setOpenPanel(null)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setOpenPanel(null)}>×</button>
            <h3>정보</h3>
            <div className="info-content">
              <div className="about-item">
                <strong>앱 이름</strong>
                <span>명함 관리 앱</span>
              </div>
              <div className="about-item">
                <strong>버전</strong>
                <span>1.0.0</span>
              </div>
              <div className="about-item">
                <strong>개발자</strong>
                <span>NaverCloud Team</span>
              </div>
              <div className="about-item">
                <strong>문의</strong>
                <span>support@navercloud.com</span>
              </div>
              <div className="about-item">
                <strong>라이센스</strong>
                <span>MIT License</span>
              </div>
              <div className="about-description">
                <p>이 앱은 명함 관리를 디지털화하여 효율적인 비즈니스 네트워킹을 지원합니다.</p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Setting;