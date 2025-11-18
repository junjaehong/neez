import React from 'react';
import './Logo.css';

const Logo = ({ size = 'large' }) => {
  return (
    <div className={`logo ${size}`}>
      LOGO
    </div>
  );
};

export default Logo;
