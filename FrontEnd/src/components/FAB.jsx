import React from 'react';
import { useNavigate } from 'react-router-dom';
import './FAB.css';

const FAB = () => {
  const navigate = useNavigate();

  const handleClick = () => {
    // 바로 카메라 페이지로 이동
    navigate('/camera');
  };

  return (
    <button
      className="camera-btn"
      onClick={handleClick}
      title="명함 촬영"
    >
      📷
    </button>
  );
};

export default FAB;

