import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './SttIng.css';

const SttIng = () => {
  const navigate = useNavigate();
  const { meetingParticipants, currentMeeting, settings, addMeetingNote } = useApp();
  const [isRecording, setIsRecording] = useState(false);
  const [transcriptText, setTranscriptText] = useState('');
  const [translatedText, setTranslatedText] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('en');
  const [showLanguagePopup, setShowLanguagePopup] = useState(false);
  const [recordingTime, setRecordingTime] = useState(0);
  const recognitionRef = useRef(null);
  const timerRef = useRef(null);

  useEffect(() => {
    // Web Speech API 초기화
    if ('webkitSpeechRecognition' in window) {
      const recognition = new window.webkitSpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = true;
      recognition.lang = 'ko-KR';

      recognition.onresult = (event) => {
        let finalTranscript = '';
        for (let i = event.resultIndex; i < event.results.length; i++) {
          const transcript = event.results[i][0].transcript;
          if (event.results[i].isFinal) {
            finalTranscript += transcript + ' ';
          }
        }
        setTranscriptText(prev => prev + finalTranscript);
      };

      recognition.onerror = (event) => {
        console.error('Speech recognition error:', event.error);
        if (event.error === 'no-speech') {
          // 음성이 감지되지 않음
        }
      };

      recognitionRef.current = recognition;
    }

    return () => {
      if (recognitionRef.current) {
        recognitionRef.current.stop();
      }
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    };
  }, []);

  const startRecording = () => {
    if (recognitionRef.current) {
      recognitionRef.current.start();
      setIsRecording(true);
      
      // 녹음 시간 타이머 시작
      timerRef.current = setInterval(() => {
        setRecordingTime(prev => prev + 1);
      }, 1000);
    }
  };

  const stopRecording = () => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
      setIsRecording(false);
      
      // 타이머 정지
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
      
      // 자동 번역 시뮬레이션
      simulateTranslation();
    }
  };

  const simulateTranslation = () => {
    // 실제로는 번역 API 호출
    setTimeout(() => {
      const mockTranslations = {
        en: "Alright, let's start with the new project schedule and role assignments. As I mentioned before, we've split this week and share it next week. Sounds good. How about the budget? The budget has been reviewed, and we're planning to start the approval process early next week.",
        ja: "それでは、新しいプロジェクトのスケジュールと役割分担から始めましょう。前に述べたように、今週を分割して来週に共有します。いいですね。予算はどうですか？予算は検討済みで、来週初めに承認プロセスを開始する予定です。",
        ko: transcriptText || "좋습니다. 새로운 프로젝트의 일정과 역할 분담부터 시작하겠습니다. 앞서 말씀드린 대로 이번 주를 나누어서 다음 주에 공유하겠습니다. 좋습니다. 예산은 어떻게 되나요? 예산은 검토했고, 다음 주 초에 승인 절차를 시작할 예정입니다."
      };
      setTranslatedText(mockTranslations[selectedLanguage]);
    }, 1000);
  };

  const handleLanguageSelect = (lang) => {
    setSelectedLanguage(lang);
    setShowLanguagePopup(false);
    simulateTranslation();
  };

  const handleSave = () => {
    // 회의록 요약본 생성
    const summary = transcriptText.substring(0, 200) + '...';
    
    // 참석자들의 카드에 회의록 추가
    meetingParticipants.forEach(participant => {
      addMeetingNote(participant.id, summary);
    });
    
    alert('회의록이 저장되었습니다.');
    navigate('/cardlist');
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="stt-ing-container">
      <div className="stt-ing-box">
        <div className="stt-ing-header">
          <button className="back-button" onClick={() => navigate('/stt-select')}>←</button>
          <h2>회의 진행중</h2>
          <button className="language-button" onClick={() => setShowLanguagePopup(true)}>
            {selectedLanguage === 'ko' ? '한국어' : selectedLanguage === 'en' ? 'English' : '日本語'}
          </button>
        </div>

        {/* 참석자 표시 */}
        <div className="participants-bar">
          <span className="participants-label">참석자:</span>
          <div className="participants-chips">
            {meetingParticipants?.map(participant => (
              <span key={participant.id} className="participant-chip">
                {participant.name}
              </span>
            ))}
          </div>
        </div>

        {/* 녹음 컨트롤 */}
        <div className="recording-control">
          <div className="recording-status">
            {isRecording && (
              <div className="recording-indicator">
                <span className="recording-dot"></span>
                <span>녹음중 {formatTime(recordingTime)}</span>
              </div>
            )}
          </div>
          
          <button 
            className={`record-button ${isRecording ? 'recording' : ''}`}
            onClick={isRecording ? stopRecording : startRecording}
          >
            {isRecording ? '⬜' : '🔴'}
          </button>
        </div>

        {/* 음성 인식 결과 */}
        <div className="transcript-section">
          <h3>음성 인식</h3>
          <div className="transcript-box">
            {transcriptText || '녹음 버튼을 눌러 회의를 시작하세요'}
          </div>
        </div>

        {/* 번역 결과 */}
        {translatedText && (
          <div className="translation-section">
            <h3>번역 ({selectedLanguage === 'en' ? 'English' : selectedLanguage === 'ja' ? '日本語' : '한국어'})</h3>
            <div className="translation-box">
              {translatedText}
            </div>
          </div>
        )}

        {/* 저장 버튼 */}
        {transcriptText && (
          <button className="save-meeting-button" onClick={handleSave}>
            회의록 저장
          </button>
        )}
      </div>

      {/* 언어 선택 팝업 */}
      {showLanguagePopup && (
        <div className="popup-overlay" onClick={() => setShowLanguagePopup(false)}>
          <div className="popup-content" onClick={e => e.stopPropagation()}>
            <button className="popup-close" onClick={() => setShowLanguagePopup(false)}>×</button>
            <h3>번역 언어 선택</h3>
            <div className="language-options">
              <button 
                className={`language-option ${selectedLanguage === 'ko' ? 'selected' : ''}`}
                onClick={() => handleLanguageSelect('ko')}
              >
                <span className="language-flag">🇰🇷</span>
                <span>한국어</span>
              </button>
              <button 
                className={`language-option ${selectedLanguage === 'en' ? 'selected' : ''}`}
                onClick={() => handleLanguageSelect('en')}
              >
                <span className="language-flag">🇺🇸</span>
                <span>English</span>
              </button>
              <button 
                className={`language-option ${selectedLanguage === 'ja' ? 'selected' : ''}`}
                onClick={() => handleLanguageSelect('ja')}
              >
                <span className="language-flag">🇯🇵</span>
                <span>日本語</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SttIng;