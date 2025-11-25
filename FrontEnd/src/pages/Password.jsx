import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './Password.css';

const Password = () => {
  const navigate = useNavigate();
  const { changePassword } = useApp();
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
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

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.currentPassword) {
      newErrors.currentPassword = '현재 비밀번호를 입력해주세요.';
    }
    
    if (!formData.newPassword) {
      newErrors.newPassword = '새 비밀번호를 입력해주세요.';
    } else if (formData.newPassword.length < 8) {
      newErrors.newPassword = '비밀번호는 8자 이상이어야 합니다.';
    }
    
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '비밀번호 확인을 입력해주세요.';
    } else if (formData.newPassword !== formData.confirmPassword) {
      newErrors.confirmPassword = '비밀번호가 일치하지 않습니다.';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (validateForm()) {
      if (changePassword(formData.currentPassword, formData.newPassword)) {
        alert('비밀번호가 변경되었습니다.');
        navigate('/mypage');
      } else {
        setErrors({ currentPassword: '현재 비밀번호가 올바르지 않습니다.' });
      }
    }
  };

  return (
    <div className="password-container">
      <div className="password-box">
        <div className="password-header">
          <button className="back-button" onClick={handleBack}>←</button>
          <h2>비밀번호 변경</h2>
          <div></div>
        </div>

        <form onSubmit={handleSubmit} className="password-form">
          <div className="form-group">
            <label>현재 비밀번호</label>
            <input
              type="password"
              name="currentPassword"
              value={formData.currentPassword}
              onChange={handleInputChange}
              placeholder="현재 비밀번호 입력"
            />
            {errors.currentPassword && (
              <span className="error-message">{errors.currentPassword}</span>
            )}
          </div>

          <div className="form-group">
            <label>새 비밀번호</label>
            <input
              type="password"
              name="newPassword"
              value={formData.newPassword}
              onChange={handleInputChange}
              placeholder="새 비밀번호 입력 (8자 이상)"
            />
            {errors.newPassword && (
              <span className="error-message">{errors.newPassword}</span>
            )}
          </div>

          <div className="form-group">
            <label>비밀번호 확인</label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              placeholder="새 비밀번호 재입력"
            />
            {errors.confirmPassword && (
              <span className="error-message">{errors.confirmPassword}</span>
            )}
          </div>

          <div className="password-tips">
            <h4>안전한 비밀번호 만들기</h4>
            <ul>
              <li>최소 8자 이상</li>
              <li>대소문자 혼용</li>
              <li>숫자 포함</li>
              <li>특수문자 포함 권장</li>
            </ul>
          </div>

          <button type="submit" className="submit-button">
            비밀번호 변경
          </button>
        </form>
      </div>
    </div>
  );
};

export default Password;
