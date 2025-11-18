import React, { createContext, useState, useContext, useEffect } from 'react';

const AppContext = createContext();

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
};

export const AppProvider = ({ children }) => {
  // 사용자 정보
  const [currentUser, setCurrentUser] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  
  // 내 명함 정보
  const [myCard, setMyCard] = useState({
    name: '홍길동',
    position: '팀장',
    department: '총무팀',
    company: 'NaverCloud',
    phone: '010-1234-5678',
    email: 'asdf@navercloud.com',
    address: '서울시 강남구',
    fax: '02-123-4567',
    website: 'www.navercloud.com',
    // customFields: [] // 추가 필드
  });
  
  // 수집한 명함 목록
  const [cardList, setCardList] = useState([
    {
      id: 1,
      name: '홍길동',
      position: '대표이사',
      department: '경영지원',
      company: 'NaverCloud',
      phone: '010-1234-5678',
      email: 'ceo@navercloud.com',
      createdAt: '2025.10.24',
      hashtags: ['#중요', '#거래처'],
      memo: '',
      companyInfo: '',
      meetingNotes: []
    },
    {
      id: 2,
      name: '김길태',
      position: '팀장',
      department: '인사과',
      company: 'Coupang',
      phone: '010-8765-4321',
      email: 'tae@coupang.com',
      createdAt: '2025.10.29',
      hashtags: ['#마케팅', '#영업'],
      memo: '',
      companyInfo: '',
      meetingNotes: []
    }
  ]);

  // 해시태그 목록
  const [hashtags, setHashtags] = useState(['중요', '거래처', '개발', '마케팅', '영업', '인사']);
  
  // 설정
  const [settings, setSettings] = useState({
    language: 'ko', // ko, en, ja
    darkMode: false,
    fontSize: 'medium' // small, medium, large
  });

  // STT 관련
  const [meetingParticipants, setMeetingParticipants] = useState([]);
  const [currentMeeting, setCurrentMeeting] = useState(null);

  // 회의록 리스트
  const [meetingNotes, setMeetingNotes] = useState([
    {
    date: "25.10.24",
    company: "NaverCloud",
    content: "신규 프로젝트 일정 조율 및 역할 분담 논의...",
  }
  ]);
  // 팝업용
  const [selectedNote, setSelectedNote] = useState(null); 

  // localStorage에 데이터 저장
  useEffect(() => {
    const savedData = localStorage.getItem('cardAppData');
    if (savedData) {
      const parsedData = JSON.parse(savedData);
      if (parsedData.myCard) setMyCard(parsedData.myCard);
      if (parsedData.cardList) setCardList(parsedData.cardList);
      if (parsedData.currentUser) {
        setCurrentUser(parsedData.currentUser);
        setIsLoggedIn(true);
      }
      if (parsedData.settings) setSettings(parsedData.settings);
      if (parsedData.hashtags) setHashtags(parsedData.hashtags);
    }
  }, []);

  useEffect(() => {
    const dataToSave = {
      myCard,
      cardList,
      currentUser,
      settings,
      hashtags
    };
    localStorage.setItem('cardAppData', JSON.stringify(dataToSave));
    
    // 다크모드 적용
    if (settings.darkMode) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
    
    // 폰트 크기 적용
    document.body.className = document.body.className.replace(/font-size-\w+/, '');
    document.body.classList.add(`font-size-${settings.fontSize}`);
  }, [myCard, cardList, currentUser, settings, hashtags]);

  // 내 명함 수정
  const updateMyCard = (updatedCard) => {
    setMyCard(updatedCard);
  };

  // 명함 추가
  const addCard = (newCard) => {
    const card = {
      ...newCard,
      id: Date.now(),
      createdAt: new Date().toLocaleDateString('ko-KR').replace(/\. /g, '.').replace('.', ''),
      hashtags: [],
      memo: '',
      companyInfo: '',
      meetingNotes: []
    };
    setCardList([card, ...cardList]);
  };

  // 명함 삭제
  const deleteCard = (cardId) => {
    setCardList(cardList.filter(card => card.id !== cardId));
  };

  // 명함 수정
  const updateCard = (cardId, updatedCard) => {
    setCardList(cardList.map(card => 
      card.id === cardId ? { ...card, ...updatedCard } : card
    ));
  };

  // 해시태그 추가
  const addHashtag = (tag) => {
    if (!hashtags.includes(tag)) {
      setHashtags([...hashtags, tag]);
    }
  };

  // 해시태그 삭제
  const deleteHashtag = (tag) => {
    setHashtags(hashtags.filter(t => t !== tag));
    // 명함에서도 해당 해시태그 제거
    setCardList(cardList.map(card => ({
      ...card,
      hashtags: card.hashtags.filter(t => t !== `#${tag}`)
    })));
  };

  // 명함에 해시태그 추가
  const addHashtagToCard = (cardId, tag) => {
    setCardList(cardList.map(card => {
      if (card.id === cardId) {
        const tagWithHash = tag.startsWith('#') ? tag : `#${tag}`;
        if (!card.hashtags.includes(tagWithHash)) {
          return { ...card, hashtags: [...card.hashtags, tagWithHash] };
        }
      }
      return card;
    }));
  };

  // 회의록 추가
  const addMeetingNote = (cardId, note) => {
    setCardList(cardList.map(card => {
      if (card.id === cardId) {
        return { 
          ...card, 
          meetingNotes: [...(card.meetingNotes || []), {
            id: Date.now(),
            date: new Date().toISOString(),
            content: note,
            language: settings.language
          }]
        };
      }
      return card;
    }));
  };

  // 설정 업데이트
  const updateSettings = (newSettings) => {
    setSettings({ ...settings, ...newSettings });
  };

  // 로그인
  const login = (userId, password) => {
    setCurrentUser({ id: userId, name: myCard.name });
    setIsLoggedIn(true);
    return true;
  };

  // 로그아웃
  const logout = () => {
    setCurrentUser(null);
    setIsLoggedIn(false);
  };

  // 회원가입
  const register = (userData) => {
    console.log('회원가입:', userData);
    return true;
  };

  // 비밀번호 변경
  const changePassword = (oldPassword, newPassword) => {
    // 실제로는 API 호출
    console.log('비밀번호 변경');
    return true;
  };

  // 이메일 변경
  const changeEmail = (newEmail) => {
    // 실제로는 API 호출
    console.log('이메일 변경:', newEmail);
    return true;
  };

  // 회원 탈퇴
  const deleteAccount = () => {
    // 실제로는 API 호출
    localStorage.removeItem('cardAppData');
    setCurrentUser(null);
    setIsLoggedIn(false);
    return true;
  };

  const value = {
    currentUser,
    isLoggedIn,
    myCard,
    cardList,
    hashtags,
    settings,
    meetingParticipants,
    currentMeeting,
    setMeetingParticipants,
    setCurrentMeeting,
    updateMyCard,
    addCard,
    deleteCard,
    updateCard,
    addHashtag,
    deleteHashtag,
    addHashtagToCard,
    addMeetingNote,
    updateSettings,
    login,
    logout,
    register,
    changePassword,
    changeEmail,
    deleteAccount
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};