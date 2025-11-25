import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import axios from 'axios';
import Logo from '../components/Logo';
import InputField from '../components/InputField';
import Button from '../components/Button';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const { login } = useApp();
  
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);


  const handleSubmit = async (e) => {
    e.preventDefault();
    // console.log("로그인 에러", error.response?.data)
    // 입력값 검증
    if (!userId || !password) {
      setError('아이디와 비밀번호를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await login(userId, password);
      navigate('/main'); // 로그인 성공하면 메인으로 이동
    } catch (err) {
      console.error('login error', err);
      console.log("서버 응답:", err?.response?.data);   // ← 서버 에러 제대로 찍힘
      setError(err?.response?.data?.message || err?.message || '로그인 실패');
    } finally {
      setLoading(false);
    }
  };



  return (
    <div className="login-container">
      <div className="login-content">
        <Logo size="medium" />

        <form onSubmit={handleSubmit} className="login-form">
          <InputField
            name="userId"
            type="text"
            placeholder="ID"
            value={userId}
            onChange={e => {
              setUserId(e.target.value);
              if (error) setError('');
            }}
            disabled={loading}
          />
          <InputField
            name="password"
            type="password"
            placeholder="PW"
            value={password}
            onChange={e => {
              setPassword(e.target.value);
              if (error) setError('');
            }}
            disabled={loading}
          />
          <Button type="submit" variant="primary" fullWidth>
            {/* {loading ? '로그인 중...' : '로그인'} */}
            로그인
          </Button>
        </form>
        <div className="login-footer">
          <Link to="/join" className="join-link">회원가입</Link>
          {error && <div className="login-error">{error}</div>}
        </div>
      </div>
    </div>
  );
};

export default Login;
