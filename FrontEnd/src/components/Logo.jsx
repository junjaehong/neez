import React from 'react';
import './Logo.css';

const Logo = ({ size = 'large' }) => {
  return (
    <div className={`logo ${size}`}>
      <img src=".\public\임의로고.png" alt="임의로고" />
    </div>
  );
};

export default Logo;
