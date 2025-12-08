import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import api from '../api/client';
import { getAuthHeader } from '../api/auth'; // 인증 토큰 함수
import { loadConfig } from '../api/configLoader';
import './SttIng.css';

// const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083/api';

const SttIng = () => {
  const navigate = useNavigate();
  const { meetingParticipants = [] } = useApp();

  const [isRecording, setIsRecording] = useState(false);
  const [transcriptText, setTranscriptText] = useState('');
  const [translatedText, setTranslatedText] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('en');
  const [recordingTime, setRecordingTime] = useState(0);
  const [isTranslating, setIsTranslating] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [lastChunkIsKorean, setLastChunkIsKorean] = useState(true);
  const [config, setConfig] = useState({ baseURL: '' });
  const [configLoaded, setConfigLoaded] = useState(false);

  const recognitionRef = useRef(null);
  const timerRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  ///////////////////////////////////////////////////////
  const chunkIndex = useRef(0);
  const [meetingId, setMeetingId] = useState(null);
  //////////////////////////////////////////////////////

  const handleBack = () => {
    navigate('/sttcardselect');
  };

  // config.xml 불러오기
  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const config = await loadConfig();
        setConfig({
          baseURL: config.baseURL || 'http://localhost:8083'
        });
        setConfigLoaded(true);
        console.log('Loaded config:', config);

      } catch (err) {
        console.error('config.xml 로드 실패', err);
      }
    };
    fetchConfig();
  }, []);

  // SttIng 진입 시 "회의 시작 API" 호출
  useEffect(() => {
    if (!configLoaded) return;

    const startMeeting = async () => {
      const participantBizCardIds = meetingParticipants
        .map(p => p.idx)
        .filter(id => id != null);

      if (participantBizCardIds.length === 0) {
        alert("참석자를 최소 한 명 선택해야 합니다.");
        navigate('/sttcardselect');
        return;
      }

      const body = {
        sourceLang: "ko-KR",
        targetLang: selectedLanguage || "en",
        participantBizCardIds
      };

      console.log("startMeeting body:", body);

      try {
        const response = await api.post("/meetings/me", body, { headers: getAuthHeader() });
        console.log("회의 시작 성공", response.data);
        setMeetingId(response.data.meetingId);
      } catch (err) {
        console.error("회의 시작 실패", err);
        alert("회의를 시작할 수 없습니다.");
        navigate('/sttcardselect');
      }
    };

    startMeeting();
  }, [configLoaded, meetingParticipants, selectedLanguage, navigate]);

  const isKorean = (text) => /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(text);

  /////////////////////////////////////////////////////
  // --- 수정: audio 기반 업로드만 사용 ---
  const uploadAudioChunk = async (audioBlob) => {
    if (!meetingId) return;

    chunkIndex.current += 1;
    const formData = new FormData();
    formData.append('file', audioBlob, `chunk_${chunkIndex.current}.webm`);

    setIsTranslating(true);

    try {
      const response = await api.post(
        `/meetings/me/${meetingId}/chunks?index=${chunkIndex.current}&targetLang=${selectedLanguage}&sourceLang=ko-KR`,
        formData,
        { headers: getAuthHeader() }
      );

      // 서버 STT 결과 반영
      if (response.data.text) {
        setTranscriptText((prev) => prev ? prev + ' ' + response.data.text : response.data.text);
      }
      if (response.data.translation) {
        setTranslatedText((prev) => prev ? prev + ' ' + response.data.translation : response.data.translation);
      }

      setLastChunkIsKorean(isKorean(response.data.text || ''));
    } catch (err) {
      console.error("오디오 업로드 실패:", err);
    } finally {
      setIsTranslating(false);
    }
  };
  /////////////////////////////////////////////////////

  ////////////////////////////////////
  // 청크 업로드
  const uploadChunk = async (text) => {
    if (!meetingId) {
      console.log("Meeting ID가 없어 청크 업로드 스킵");
      return;
    }
    
    chunkIndex.current += 1;
    const blob = new Blob([audio], { type: 'audio/wav' });
    const formData = new FormData();
    formData.append('file', blob, `chunk_${chunkIndex.current}.wav`);

    console.log("업로드 청크 번호:", chunkIndex.current);

    try {
      const response = await api.post(
        `/meetings/me/${meetingId}/chunks?index=${chunkIndex.current}&targetLang=${selectedLanguage}&sourceLang=ko-KR`,
        formData,
        { headers: getAuthHeader() } // Content-Type 제거!
      );
      console.log("청크 업로드 성공", response.data);
      
      // 서버 응답에서 번역 결과 가져오기
      const translated = response.data.translation || response.data.translatedText || '';
    
      if (translated) {
        setTranslatedText((prev) => prev ? prev + ' ' + translated : translated);
      }
      
    } catch (err) {
      console.error("청크 업로드 실패:", err.response?.status, err.message);
      // Clova Speech 오류 시 텍스트만 표시
      if (err.response?.status === 503) {
        // 번역 없이 원문만 표시
        setTranslatedText((prev) => prev ? prev + ' ' + text : text);
      }
    } finally {
      setIsTranslating(false); // 번역 완료
    }
  };
  /////////////////////////////////////

  // 음성 인식 설정
  useEffect(() => {
    if ('webkitSpeechRecognition' in window) {
      const recognition = new window.webkitSpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = true;
      recognition.lang = 'ko-KR';

      recognition.onresult = async (event) => {
        let finalTranscript = '';
        for (let i = event.resultIndex; i < event.results.length; i++) {
          const transcript = event.results[i][0].transcript;
          if (event.results[i].isFinal) {
            finalTranscript += `${transcript} `;
          }
        }
        if (!finalTranscript.trim()) return;

        console.log('음성 인식 결과:', finalTranscript);

        // ① 회의 진행 중 전체 텍스트 누적
        setTranscriptText((prev) => `${prev}${finalTranscript}`.trim() + ' ');
      };

      recognition.onerror = (event) => console.error('Speech recognition error:', event.error);
      recognitionRef.current = recognition;
    }

    return () => {
      if (recognitionRef.current) recognitionRef.current.stop();
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  // 녹음 시작
  const startRecording = () => {
  if (!recognitionRef.current || !configLoaded) return;
  
  // MediaRecorder로 실제 오디오 녹음
  navigator.mediaDevices.getUserMedia({ audio: true })
    .then(stream => {
      const mediaRecorder = new MediaRecorder(stream);
      const audioChunks = [];
      
      mediaRecorder.ondataavailable = (event) => {
        audioChunks.push(event.data);
      };
      
      mediaRecorder.onstop = async () => {
        const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
        // 이 audioBlob를 서버에 전송
        await uploadAudioChunk(audioBlob);
      };
      
      mediaRecorderRef.current = mediaRecorder;
      mediaRecorder.start();
    });
  
  recognitionRef.current.start();
  setIsRecording(true);
  timerRef.current = setInterval(() => {
    setRecordingTime((prev) => prev + 1);
  }, 1000);
};


  const stopRecording = () => {
    if (recognitionRef.current) recognitionRef.current.stop();
    if (mediaRecorderRef.current) mediaRecorderRef.current.stop();
    setIsRecording(false);
    if (timerRef.current) clearInterval(timerRef.current);
  };

  const handleLanguageSelect = (lang) => { if (!isRecording) setSelectedLanguage(lang); };

  const handleSave = async () => {
    if (!meetingId) return alert("회의 ID가 없습니다.");
    setIsSaving(true);
    try {
      // 각 참석자의 명함에 회의록 연결
      for (const participant of meetingParticipants) {
        if (participant.idx) {
          try {
            await api.post(
              `/meetings/me/${meetingId}/minutes?bizCardId=${participant.idx}`,
              {},
              { headers: getAuthHeader() }
            );
            console.log(`${participant.name} (ID: ${participant.idx}) 명함에 회의록 저장 완료`);
          } catch (err) {
            console.error(`${participant.name} 명함 저장 실패:`, err);
          }
        }
      }
      alert('회의록이 참석자 명함에 저장되었습니다.');
      navigate('/cardlist');
    } catch (err) {
      console.error('회의 종료 실패', err);
      alert('회의 종료 실패');
    } finally {
      setIsSaving(false);
    }
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
          <button className="back-btn" onClick={handleBack}>←</button>
          <p>회의 진행중</p>
          <select
            className="language-select"
            value={selectedLanguage}
            disabled={isRecording}
            onChange={(e) => handleLanguageSelect(e.target.value)}
            title={isRecording ? '회의 중에는 변경할 수 없습니다' : '한국어 입력 시 이 언어로 번역됩니다'}
          >
            <option value="en">English</option>
            <option value="ja">日本語</option>
            <option value="ko">한국어</option>
          </select>
        </div>

        <div className="stt-ing-content">
          <div className="participants-bar">
            <span className="participants-label">참석자</span>
            <div className="participants-chips">
              {meetingParticipants.length > 0 ? (
                meetingParticipants.map((participant, index) => (
                  <span key={participant.id || index} className="participant-chip">
                    {participant.name}
                  </span>
                ))
              ) : (
                <span className="participant-chip empty">참석자를 선택하지 않았어요</span>
              )}
            </div>
          </div>

          <div className="recording-control">
            <div className="recording-status">
              {isRecording && (
                <div className="recording-indicator">
                  <span className="recording-dot" />
                  <span>{formatTime(recordingTime)}</span>
                </div>
              )}
            </div>

            <button
              className={`record-button ${isRecording ? 'recording' : ''}`}
              onClick={isRecording ? stopRecording : startRecording}
            >
              <div className={`record-circle ${isRecording ? 'stop' : 'start'}`} />
            </button>
          </div>

          <div className="transcript-section">
            <h3>음성 인식</h3>
            <div className="transcript-box">
              {transcriptText || '녹음 버튼을 눌러 회의를 시작하세요'}
            </div>
          </div>

          { translatedText && (
            <div className="translation-section">
              <div className="translation-head">
                <h3>
                  {lastChunkIsKorean 
                    ? `한국어 → ${selectedLanguage === 'en' ? 'English' : selectedLanguage === 'ja' ? '日本語' : '한국어'}` 
                    : `입력 언어 → 한국어`}
                </h3>
              </div>
              <div className="translation-box">
                {translatedText ||'번역 중...'}
                {isTranslating && <span className="translation-loading">번역 중...</span>}
              </div>
            </div>
          )}
        </div>

        {transcriptText && (
          // <div className="meeting-end">
            <button className="save-meeting-button" onClick={handleSave} disabled={isSaving}>
              {isSaving ? '저장 중...' : '회의 종료'}
            </button>
          // </div>
        )}
      </div>
    </div>
  );
};
export default SttIng;
