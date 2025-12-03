import React, { createContext, useState, useContext, useEffect } from 'react';
import * as authApi from '../api/auth';
import { setAuthToken } from '../api/client';
import api from '../api/client';
import { loadConfig } from '../api/configLoader';

const AppContext = createContext();

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
};

export const AppProvider = ({ children }) => {
  const [configReady, setConfigReady] = useState(false);
  const [baseURL, setBaseURL] = useState('');

  // 인증 정보
  const [auth, setAuth] = useState(() => {
    try {
      return {
        accessToken: localStorage.getItem('accessToken'),
        refreshToken: localStorage.getItem('refreshToken'),
        user: JSON.parse(localStorage.getItem('user') || 'null'),
      };
    } catch {
      return { accessToken: null, refreshToken: null, user: null };
    }
  });

  const isLoggedIn = !!auth?.accessToken;
  const [currentUser, setCurrentUser] = useState(auth?.user || null);
  const [cardList, setCardList] = useState([]);
  const [hashtags, setHashtags] = useState(['중요', '거래처', '개발', '마케팅', '영업', '인사']);

  // 설정
  const [settings, setSettings] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('appSettings')) || { 
        language: 'ko', 
        darkMode: false, 
        fontSize: 'medium' 
      };
    } catch {
      return { language: 'ko', darkMode: false, fontSize: 'medium' };
    }
  });

  const [meetingParticipants, setMeetingParticipants] = useState([]);
  const [currentMeeting, setCurrentMeeting] = useState(null);
  const [selectedNote, setSelectedNote] = useState(null);

  // Config 로드
  useEffect(() => {
    const initConfig = async () => {
      try {
        const config = await loadConfig();
        setBaseURL(config.baseURL);
      } catch (error) {
        console.error('Config load failed:', error);
      } finally {
        setConfigReady(true);
      }
    };
    initConfig();
  }, []);

  // 초기화: localStorage 토큰이 있으면 axios에 적용
  useEffect(() => {
    if (auth.accessToken) {
      setAuthToken(auth.accessToken);
    } else {
      setAuthToken(null);
    }
  }, [auth.accessToken]);

  // auth 변경 시 localStorage 동기화
  useEffect(() => {
    try {
      if (auth.accessToken) {
        localStorage.setItem('accessToken', auth.accessToken);
      } else {
        localStorage.removeItem('accessToken');
      }

      if (auth.refreshToken) {
        localStorage.setItem('refreshToken', auth.refreshToken);
      } else {
        localStorage.removeItem('refreshToken');
      }

      if (auth.user) {
        localStorage.setItem('user', JSON.stringify(auth.user));
      } else {
        localStorage.removeItem('user');
      }
    } catch (e) {
      console.error('localStorage error:', e);
    }
  }, [auth]);

  // settings 변경 처리
  useEffect(() => {
    if (settings.darkMode) {
      document.body.classList.add('dark');
    } else {
      document.body.classList.remove('dark');
    }

    const textSizeMap = { 
      small: '14px', 
      medium: '16px', 
      large: '18px' 
    };
    document.documentElement.style.setProperty(
      '--app-text-size', 
      textSizeMap[settings.fontSize] || textSizeMap.medium
    );

    if (settings.language) {
      document.documentElement.lang = settings.language;
    }

    try {
      localStorage.setItem('appSettings', JSON.stringify(settings));
    } catch (e) {
      console.error('localStorage error:', e);
    }
  }, [settings]);
  
  // 회원가입
  const register = async ({ userId, password, name, email }) => {
    const data = await authApi.register({ userId, password, name, email });

    // 서버 응답 구조에 맞춰 user/profile 추출
    const profile = data.user || data.profile || { name, email };
    setCurrentUser(profile);
    
    // 서버가 토큰을 반환하면 auth 상태로 반영 (옵션)
    if (data.accessToken || data.refreshToken) {
      setAuth({
        accessToken: data.accessToken || null,
        refreshToken: data.refreshToken || null,
        user: profile
      });
    }
    
    return data;
  };

  // 로그인
  ///////////////////////////
  const login = async (userId, password) => {
    // if (!baseURL) {
    //   console.warn("config.xml에 baseURL 없음. 기본값 사용");
    //   setBaseURL(process.env.REACT_APP_BASE_URL);
    // }

    if (!configReady) {
      console.warn("⚠️ config.xml이 아직 로드되지 않았습니다. 잠시 대기 후 재시도합니다.");
      await new Promise(resolve => setTimeout(resolve, 100));
    }
    // if (!configReady) {
    //   await new Promise(resolve => {
    //     const interval = setInterval(() => {
    //       if (configReady) {
    //         clearInterval(interval);
    //         resolve();
    //       }
    //     }, 50);
    //   });
    // }
  ////////////////////////////
    try {
      ////////////////////////////
      await api.ensureConfig?.();
      ////////////////////////////
      const { accessToken, refreshToken, user } = await authApi.login(userId, password);
      // 1) axios 즉시 업데이트 (중요)
      setAuthToken(accessToken);

      // 2) 상태 반영
      setAuth({ accessToken, refreshToken, user });

      // 3) 토큰이 axios에 확실히 반영된 후 호출
      await fetchMyCard();

    } catch (error) {
      console.error("Login error:", error);
      throw error;
    }
  }
    // setAuth({ accessToken, refreshToken, user });
    
    // try {
    //   await fetchMyCard();
    // } catch (err) {
    //   // 내 명함 불러오기 실패해도 로그인은 성공
    //   console.error('Failed to fetch user card:', err);
    // }
    
  //   return { accessToken, refreshToken, user };
  // };

  // 내 명함 불러오기
  const fetchMyCard = async () => {
    try {
      const res = await api.get('/api/users/me');
      const userData = res.data;

      if (!userData.success) {
        throw new Error(userData.message || '내 명함 조회 실패');
      }

      // 실제 사용자 정보는 userData.data 안에 있음
      const user = userData.data;

      setCurrentUser(user);
      setAuth(prev => ({ ...prev, user }));

      return userData;
    } catch (err) {
      console.error("내 명함 불러오기 실패:", err);
      throw err;
    }
  };

  // 내 명함 수정하기
  const updateMyCard = async (data) => {
    try {
      const res = await api.post('/api/users/me', data);
      const updatedUser = res.data.data;
      
      setCurrentUser(prev => ({ ...prev, ...updatedUser }));
      setAuth(prev => ({ ...prev, user: { ...prev.user, ...updatedUser } }));
      
      return updatedUser;
    } catch (err) {
      console.error("내 명함 수정 실패:", err);
      throw err;
    }
  };


  // 로그아웃
  const logout = async () => {
    try {
      await authApi.logout();
    } catch (err) {
      console.error('Logout error:', err);
    }
    
    setAuth({ accessToken: null, refreshToken: null, user: null });
    setCurrentUser(null);
    setAuthToken(null);
  };


  // 비밀번호 변경
  const changePassword = async (currentPassword, newPassword) => {
    try {
      const res = await api.post('/api/auth/me/change-password', {
        currentPassword,
        newPassword
      });
      return res.data.success || true;
    } catch (err) {
      console.error('비밀번호 변경 실패:', err);
      return false;
    }
  };

  // 이메일 변경
  const changeEmail = async (email) => {
    try {
      const res = await api.post('/api/users/change-email', { email });
      if (res.data.success) {
        setCurrentUser(prev => ({ ...prev, email }));
        setAuth(prev => ({ 
          ...prev, 
          user: { ...prev.user, email } 
        }));
      }
      return res.data.success || true;
    } catch (err) {
      console.error('이메일 변경 실패:', err);
      return false;
    }
  };

  // 회원 탈퇴
  const deleteAccount = async () => {
    try {
      await api.delete('/api/users/me');
      
      // 모든 데이터 초기화
      localStorage.removeItem('cardAppData');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      
      setAuth({ accessToken: null, refreshToken: null, user: null });
      setCurrentUser(null);
      setCardList([]);
      
      return true;
    } catch (err) {
      console.error('회원 탈퇴 실패:', err);
      return false;
    }
  };

  // 설정 업데이트
  const updateSettings = (patch) => {
    setSettings(prev => ({ ...prev, ...patch }));
  };

  const updateCurrentUser = (data) => {
    setCurrentUser(prev => ({ ...prev, ...data }));
    setAuth(prev => ({ 
      ...prev, 
      user: { ...prev.user, ...data } 
    }));
  };

  // 명함 관련 함수들
  const addCard = (newCard) => {
    const card = { 
      ...newCard, 
      id: Date.now(), 
      createdAt: new Date().toLocaleDateString('ko-KR') 
    };
    setCardList(prev => [card, ...prev]);
  };

  const deleteCard = (cardId) => {
    setCardList(prev => prev.filter(c => c.id !== cardId));
  };

  const updateCard = (cardId, updatedCard) => {
    setCardList(prev => prev.map(c => 
      c.id === cardId ? { ...c, ...updatedCard } : c
    ));
  };

  const addMeetingNote = (cardId, note) => {
    setCardList(prev => prev.map(card => 
      card.id === cardId 
        ? { 
            ...card, 
            meetingNotes: [
              ...(card.meetingNotes || []), 
              { 
                id: Date.now(), 
                date: new Date().toISOString(), 
                content: note, 
                language: settings.language 
              }
            ] 
          } 
        : card
    ));
  };

  const value = {
    // State
    baseURL,
    currentUser,
    isLoggedIn,
    cardList,
    hashtags,
    settings,
    meetingParticipants,
    currentMeeting,
    selectedNote,

    // Setters
    setMeetingParticipants,
    setCurrentMeeting,
    setSelectedNote,

    // Functions
    fetchMyCard,
    updateMyCard,
    updateCurrentUser,
    addCard,
    deleteCard,
    updateCard,
    addMeetingNote,
    updateSettings,
    
    // Auth
    login,
    logout,
    register,
    changePassword,
    changeEmail,
    deleteAccount
  };

  return (
    <AppContext.Provider value={value}>
      {!configReady ? (
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100vh' 
        }}>
          Loading...
        </div>
      ) : (
        children
      )}
    </AppContext.Provider>
  );
};