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
  const { meetingParticipants = [], addMeetingNote } = useApp();

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

  const handleBack = () => {
    navigate('/sttcardselect');
  };

  ///////////////////////////////////////////////////////
  // config.xml 불러오기
  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const config = await loadConfig();
        setConfig({
          baseURL: config.baseURL || 'http://localhost:8083/api'
        });
        setConfigLoaded(true);
        console.log('Loaded config:', config);

      } catch (err) {
        console.error('config.xml 로드 실패', err);
      }
    };
    fetchConfig();
  }, []);
  ///////////////////////////////////////////////////////

  const isKorean = (text) => /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(text);

  const translateText = (text) => {
    if (!text) return '';
    const trimmed = text.trim();
    // Fallback: show original text when translation API fails
    return trimmed;
  };

  const translateAndAppend = async (text, targetLang) => {
    if (!text.trim()) return;
    if (!configLoaded) return;
    // if (!config.baseURL) {
    //   console.warn('API URL이 아직 준비되지 않았습니다.');
    //   return;
    // }

    setIsTranslating(true);
    try {
      const res = await fetch(`${config.baseURL}/translate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
        body: JSON.stringify({
          text,
          sourceLang: 'auto',
          targetLang,
        }),
      });
      if (!res.ok) {
        throw new Error('translation failed');
      }
      const data = await res.json();
      const translated = data.translatedText || translateText(text);
      setTranslatedText((prev) => `${prev}${prev ? ' ' : ''}${translated}`);
    } catch (err) {
      console.error('번역 실패', err);
      setTranslatedText((prev) => `${prev}${prev ? ' ' : ''}${translateText(text)}`);
    } finally {
      setIsTranslating(false);
    }
  };

  const translateToKorean = async (text) => {
    if (!text.trim()) return '';
    if (!configLoaded) return;
    try {
      const res = await fetch(`${config.baseURL}/translate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
        body: JSON.stringify({
          text,
          sourceLang: 'auto',
          targetLang: 'ko',
        }),
      });
      if (!res.ok) throw new Error('translation failed');
      const data = await res.json();
      return data.translatedText || translateText(text);
    } catch (err) {
      console.error('요약용 번역 실패', err);
      return translateText(text);
    }
  };

  useEffect(() => {
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
            finalTranscript += `${transcript} `;
          }
        }
        if (!finalTranscript.trim()) return;
        // Keep accumulating all recognized text so it can be summarized later
        setTranscriptText((prev) => `${prev}${finalTranscript}`.trim() + ' ');
        const chunkIsKorean = isKorean(finalTranscript);
        setLastChunkIsKorean(chunkIsKorean);
        translateAndAppend(finalTranscript, chunkIsKorean ? selectedLanguage : 'ko');
      };

      recognition.onerror = (event) => {
        console.error('Speech recognition error:', event.error);
      };

      recognitionRef.current = recognition;
    }

    return () => {
      if (recognitionRef.current) recognitionRef.current.stop();
      if (timerRef.current) clearInterval(timerRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const startRecording = () => {
    if (!recognitionRef.current) return;
    if (!configLoaded) {
      alert('API 설정이 로드될 때까지 잠시 기다려주세요.');
      return;
    }
    recognitionRef.current.start();
    setIsRecording(true);
    timerRef.current = setInterval(() => {
      setRecordingTime((prev) => prev + 1);
    }, 1000);
  };

  const stopRecording = () => {
    if (!recognitionRef.current) return;
    recognitionRef.current.stop();
    setIsRecording(false);
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
    if (!translatedText) {
      setTranslatedText(translateText(transcriptText));
    }
  };

  const handleLanguageSelect = (lang) => {
    if (isRecording) return;
    setSelectedLanguage(lang);
  };

  const handleSave = async () => {
    if (isSaving) return;
    setIsSaving(true);
    try {
      const koreanText = await translateToKorean(transcriptText);
      const summarySource = koreanText || transcriptText;
      const summary = `${summarySource.substring(0, 200)}...`;
      meetingParticipants.forEach((p) => addMeetingNote(p.id, summary));
      alert('회의록이 저장되었습니다.');
      navigate('/cardlist');
    } catch (err) {
      console.error('회의록 저장 실패', err);
      alert('회의록 저장에 실패했습니다.');
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
              meetingParticipants.map((participant) => (
                <span key={participant.id} className="participant-chip">
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
                <span>녹음중 {formatTime(recordingTime)}</span>
              </div>
            )}
          </div>

          <button
            className={`record-button ${isRecording ? 'recording' : ''}`}
            onClick={isRecording ? stopRecording : startRecording}
          >
            {isRecording ? '중지' : '녹음'}
          </button>
        </div>

        <div className="transcript-section">
          <h3>음성 인식</h3>
          <div className="transcript-box">
            {transcriptText || '녹음 버튼을 눌러 회의를 시작하세요'}
          </div>
        </div>

        {translatedText && (
          <div className="translation-section">
            <div className="translation-header">
              <h3>{lastChunkIsKorean ? `한국어 → ${selectedLanguage.toUpperCase()}` : `${selectedLanguage.toUpperCase()} → 한국어`}</h3>
            </div>
            <div className="translation-box">
              {translatedText}
              {isTranslating && <span className="translation-loading">번역 중...</span>}
            </div>
          </div>
        )}
      </div>

      {transcriptText && (
        <div className="meeting-end-container">
          <button className="save-meeting-button" onClick={handleSave} disabled={isSaving}>
            {isSaving ? '저장 중...' : '회의 종료'}
          </button>
        </div>
      )}
    </div>
  );
};

export default SttIng;
