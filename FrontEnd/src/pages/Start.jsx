import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
        <div className="logo">
          <img src=".\public\Neez-Logo-L.png" alt="logo" />
        </div>
      </div>
    </div>
  );
};

export default Start;
