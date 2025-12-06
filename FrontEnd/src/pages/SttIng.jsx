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
  // 번역 API 호출 중복 방지 플래그
  const translatingRef = useRef(false);

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

      // 서버 누적본을 그대로 반영해 순서/중복 문제 방지
      const serverTranscript = response.data.transcript || response.data.text || '';
      const serverTranslated = response.data.translatedTranscript || response.data.translation || '';

      if (serverTranscript) {
        setTranscriptText(serverTranscript);
      }
      if (serverTranslated) {
        setTranslatedText(serverTranslated);
      }

      setLastChunkIsKorean(isKorean(response.data.text || ''));
    } catch (err) {
      console.error("오디오 업로드 실패:", err);
    } finally {
      setIsTranslating(false);
    }
  };
  /////////////////////////////////////////////////////

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

        // ② 서버 STT가 비거나 느린 경우를 대비해 Web Speech 결과를 바로 번역 요청
        if (selectedLanguage && selectedLanguage !== 'ko' && !translatingRef.current) {
          translatingRef.current = true;
          try {
            const res = await api.post(
              '/api/translate',
              {
                text: finalTranscript.trim(),
                sourceLang: 'ko',
                targetLang: selectedLanguage
              },
              { headers: getAuthHeader() }
            );
            const translated = res.data?.translatedText || '';
            if (translated) {
              setTranslatedText((prev) => (prev ? `${prev} ${translated}` : translated));
            }
          } catch (err) {
            console.error('로컬 번역 호출 실패', err);
          } finally {
            translatingRef.current = false;
          }
        }
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
      
      mediaRecorder.ondataavailable = (event) => {
        // 매 청크를 실시간으로 서버에 전송
        if (event.data && event.data.size > 0) {
          uploadAudioChunk(event.data);
        }
      };
      
      mediaRecorderRef.current = mediaRecorder;
      // 1초 단위로 dataavailable 발생시켜 스트리밍 업로드
      mediaRecorder.start(1000);
    });
  
  recognitionRef.current.start();
  setIsRecording(true);
  timerRef.current = setInterval(() => {
    setRecordingTime((prev) => prev + 1);
  }, 1000);
};


  const stopRecording = () => {
    if (recognitionRef.current) recognitionRef.current.stop();
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
      // 마이크 스트림 정리
      mediaRecorderRef.current.stream?.getTracks()?.forEach(t => t.stop());
    }
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

        <div className="translation-section">
          <div className="translation-head">
            <h3>
              {lastChunkIsKorean 
                ? `한국어 → ${selectedLanguage === 'en' ? 'English' : selectedLanguage === 'ja' ? '日本語' : '한국어'}` 
                : `입력 언어 → 한국어`}
            </h3>
          </div>
          <div className="translation-box">
            {translatedText || '번역 대기 중...'}
            {isTranslating && <span className="translation-loading">번역 중...</span>}
          </div>
        </div>
      </div>

      {transcriptText && (
        <div className="meeting-end">
          <button className="save-meeting-button" onClick={handleSave} disabled={isSaving}>
            {isSaving ? '저장 중...' : '회의 종료'}
          </button>
        </div>
      )}
    </div>
  );
};
export default SttIng;
