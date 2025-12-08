import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import './FAB.css';

const FAB = () => {
  const [style, setStyle] = useState({ right: 90, top: 650 });
  const navigate = useNavigate();

  useEffect(() => {
    const updatePosition = () => {
      // App.css에 있는 공통 박스 선택자에 맞춤
      const box = document.querySelector('[class$="-box"]');
      if (!box) {
        setStyle({ right: 90, top: 650 });
        return;
      }
      const rect = box.getBoundingClientRect();
      const offsetRight = 90; // 박스 오른쪽에서 떨어질 픽셀 거리
      const offsetTop = 650;
      // 뷰포트 오른쪽에서 box 오른쪽까지의 거리 + offset
      const desiredRight = Math.max(12, window.innerWidth - rect.right + offsetRight);
      setStyle({ right: desiredRight, top: offsetTop });
    };

    updatePosition();
    window.addEventListener('resize', updatePosition);
    window.addEventListener('scroll', updatePosition);
    return () => {
      window.removeEventListener('resize', updatePosition);
      window.removeEventListener('scroll', updatePosition);
    };
  }, []);

  const handleClick = () => navigate('/CameraCapture');

  return (
    <div
      className="fab-container"
      style={{
        position: 'absolute',
        // zIndex: 900,
      }}
    >
      <button className="camera-btn" onClick={handleClick} title="명함 촬영">
        <img className="icon-camera" src=".\public\Neez-camera.png" alt="camera" />
      </button>
    </div>
  );
};

export default FAB;
