// src/api/client.js
import axios from 'axios';
import { loadConfig } from './configLoader';

// Axios 인스턴스 생성
const api = axios.create({
//   headers: { 
//     'Content-Type': 'application/json' 
//   },
  baseURL: '', // AppContext에서 나중에 세팅
  timeout: 5000,
});

// BaseURL 설정
let isConfigLoaded = false;

const ensureConfig = async () => {
  if (!isConfigLoaded) {
    try {
      const { baseURL } = await loadConfig();
      ///////////////////
      console.log('Loaded baseURL:', baseURL);
      ///////////////////
      api.defaults.baseURL = baseURL;
      isConfigLoaded = true;
    } catch (error) {
      console.error('Failed to load config:', error);
    }
  }
};

// 토큰 설정 헬퍼
export const setAuthToken = (token) => {
  if (token) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common['Authorization'];
  }
};

// Request Interceptor - 설정 로드 및 토큰 추가
api.interceptors.request.use(
  async (config) => {
    // Config 확인
    await ensureConfig();
    
    // 토큰 추가
    const token = localStorage.getItem('accessToken');
    if (token && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor - 토큰 만료 처리
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // 401 에러이고 재시도하지 않은 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      const refreshToken = localStorage.getItem('refreshToken');
      
      if (refreshToken) {
        try {
          // Refresh Token으로 새 Access Token 받기
          const response = await axios.post(
            `${api.defaults.baseURL}/api/auth/refresh`, 
            { refreshToken }
          );

          const { data } = response;
          
          if (data.success && data.accessToken) {
            const newAccessToken = data.accessToken;
            
            // 새 토큰 저장
            localStorage.setItem('accessToken', newAccessToken);
            setAuthToken(newAccessToken);
            
            // 실패한 요청에 새 토큰 적용
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            
            // 요청 재시도
            return api(originalRequest);
          }
        } catch (refreshError) {
          console.error('Token refresh failed:', refreshError);
        }
      }
      
      // Refresh 실패 시 로그인 페이지로
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
      
      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);

export default api;