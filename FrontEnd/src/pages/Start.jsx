import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import './Start.css';

const Start = () => {
  const navigate = useNavigate();

  useEffect(() => {
    // 3초 후 로그인 페이지로 자동 이동
    const timer = setTimeout(() => {
      navigate('/login');
    }, 3000);

    return () => clearTimeout(timer);
  }, [navigate]);

  return (
    <div className="start-container">
      <div className="start-content">
        <Logo size="large" />
      </div>
    </div>
  );
};

export default Start;
