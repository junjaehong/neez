// src/api/auth.js
import axios from 'axios';
import api, { setAuthToken } from './client';

// 회원가입 API
export async function register({ userId, password, name, email }) {
  try {
    const res = await api.post('/api/auth/register', {
      userId, 
      password, 
      name, 
      email
    });

    const { data } = res;
    
    if (!data.success) {
      throw new Error(data.message || '회원가입 실패');
    }
    
    return data;
  } catch (error) {
    console.error('Register error:', error);
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    throw error;
  }
}

// 로그인 API
export async function login(userId, password) {
  try {
    const res = await api.post('/api/auth/login', {
      userId,
      password,
    });

    const { data } = res;

    if (!data.success) {
      throw new Error(data.message || '로그인 실패');
    }

    // 백엔드 응답 구조에 따라 조정
    const accessToken = data.accessToken || data.data?.accessToken;
    const refreshToken = data.refreshToken || data.data?.refreshToken;
    const user = data.user || data.data?.user;

    if (!accessToken || !refreshToken) {
      throw new Error('토큰 정보를 받을 수 없습니다.');
    }

    // 토큰 저장
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    }
    
    setAuthToken(accessToken);
    
    return { accessToken, refreshToken, user };
  } catch (error) {
    console.error('Login error:', error);
    
    if (error.response) {
      const status = error.response.status;
      const message = error.response.data?.message;
      
      if (status === 401) {
        throw new Error('아이디 또는 비밀번호가 일치하지 않습니다.');
      } else if (status === 400) {
        throw new Error(message || '잘못된 요청입니다.');
      } else {
        throw new Error(message || '서버 오류가 발생했습니다.');
      }
    }
    
    throw error;
  }
}

// 로그아웃 API
export async function logout() {
  try {
    // 서버에 로그아웃 요청 (옵션)
    await api.post('/api/auth/logout').catch(() => {
      // 로그아웃 요청 실패는 무시
    });
  } finally {
    // 로컬 스토리지 클리어
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setAuthToken(null);
  }
}

// 토큰 갱신 API
export async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  if (!refreshToken) {
    throw new Error('Refresh token not found');
  }

  try {
    const res = await api.post('/api/auth/refresh', {
      refreshToken,
    });

    const { data } = res;

    if (data.success) {
      const newAccessToken = data.accessToken || data.data?.accessToken;
      
      if (newAccessToken) {
        localStorage.setItem('accessToken', newAccessToken);
        setAuthToken(newAccessToken);
        return newAccessToken;
      }
    }
    
    throw new Error(data.message || 'Token refresh failed');
  } catch (error) {
    console.error('Token refresh error:', error);
    
    // Refresh 실패 시 로그인 페이지로
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    
    throw error;
  }
}

// 중복 체크 API
export async function checkDuplicate(userId) {
  try {
    const res = await api.get('/api/auth/check-duplicate', {
      params: { userId }
    });
    
    const { data } = res;
    
    // available: true 또는 exists: false면 사용 가능
    return data.available === true || data.exists === false;
  } catch (error) {
    console.error('Check duplicate error:', error);
    throw error;
  }
}

// 현재 로그인한 사용자 정보 가져오기
export function getCurrentUser() {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }
  return null;
}

// 로그인 여부 확인
export function isAuthenticated() {
  return !!localStorage.getItem('accessToken');
}

// 헤더에 토큰 가져오기
export function getAuthHeader() {
  const token = localStorage.getItem('accessToken');
  if (token) {
    return { Authorization: `Bearer ${token}` };
  }
  return {};
}