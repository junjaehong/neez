import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './Email.css';

const Email = () => {
  const navigate = useNavigate();
  const { changeEmail, currentUser } = useApp();
  const [formData, setFormData] = useState({
    newEmail: '',
    confirmEmail: '',
    verificationCode: ''
  });
  const [verificationSent, setVerificationSent] = useState(false);
  const [errors, setErrors] = useState({});

  const handleBack = () => {
    navigate('/mypage');
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error for this field
    setErrors(prev => ({
      ...prev,
      [name]: ''
    }));
  };

  const validateEmail = (email) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  const handleSendVerification = () => {
    const newErrors = {};
    
    if (!formData.newEmail) {
      newErrors.newEmail = '새 이메일을 입력해주세요.';
    } else if (!validateEmail(formData.newEmail)) {
      newErrors.newEmail = '유효한 이메일 주소를 입력해주세요.';
    }
    
    if (!formData.confirmEmail) {
      newErrors.confirmEmail = '이메일 확인을 입력해주세요.';
    } else if (formData.newEmail !== formData.confirmEmail) {
      newErrors.confirmEmail = '이메일이 일치하지 않습니다.';
    }
    
    if (Object.keys(newErrors).length === 0) {
      // 실제로는 API 호출
      alert('인증번호가 발송되었습니다. (테스트: 1234 입력)');
      setVerificationSent(true);
    } else {
      setErrors(newErrors);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!verificationSent) {
      alert('먼저 인증번호를 발송해주세요.');
      return;
    }
    
    if (formData.verificationCode !== '1234') {
      setErrors({ verificationCode: '인증번호가 올바르지 않습니다. (1234 입력)' });
      return;
    }
    
    if (changeEmail(formData.newEmail)) {
      alert('이메일이 변경되었습니다.');
      navigate('/mypage');
    }
  };

  return (
    <div className="email-container">
      <div className="email-box">
        <div className="email-header">
          <button className="back-button" onClick={handleBack}>←</button>
          <h2>이메일 변경</h2>
          <div></div>
        </div>

        <form onSubmit={handleSubmit} className="email-form">
          <div className="form-group">
            <label>현재 이메일</label>
            <input
              type="email"
              value={currentUser?.email || 'user@example.com'}
              disabled
              className="disabled-input"
            />
          </div>

          <div className="form-group">
            <label>새 이메일</label>
            <input
              type="email"
              name="newEmail"
              value={formData.newEmail}
              onChange={handleInputChange}
              placeholder="새 이메일 입력"
            />
            {errors.newEmail && (
              <span className="error-message">{errors.newEmail}</span>
            )}
          </div>

          <div className="form-group">
            <label>이메일 확인</label>
            <input
              type="email"
              name="confirmEmail"
              value={formData.confirmEmail}
              onChange={handleInputChange}
              placeholder="새 이메일 재입력"
            />
            {errors.confirmEmail && (
              <span className="error-message">{errors.confirmEmail}</span>
            )}
          </div>

          <button 
            type="button" 
            className="verification-button"
            onClick={handleSendVerification}
          >
            인증번호 발송
          </button>

          {verificationSent && (
            <div className="form-group">
              <label>인증번호</label>
              <input
                type="text"
                name="verificationCode"
                value={formData.verificationCode}
                onChange={handleInputChange}
                placeholder="인증번호 입력"
                maxLength="6"
              />
              {errors.verificationCode && (
                <span className="error-message">{errors.verificationCode}</span>
              )}
            </div>
          )}

          <button type="submit" className="submit-button">
            이메일 변경
          </button>
        </form>
      </div>
    </div>
  );
};

export default Email;
