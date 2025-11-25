import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './CardInput.css';

const CardInput = () => {
  const navigate = useNavigate();
  const { addCard } = useApp();
  const [formData, setFormData] = useState({
    name: '',
    position: '',
    department: '',
    company: '',
    phone: '',
    email: '',
    address: '',
    fax: '',
    website: '',
    memo: ''
  });

  const handleBack = () => {
    navigate(-1);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!formData.name) {
      alert('이름은 필수 입력 사항입니다.');
      return;
    }
    
    if (!formData.company) {
      alert('회사명은 필수 입력 사항입니다.');
      return;
    }
    
    addCard(formData);
    alert('명함이 저장되었습니다!');
    navigate('/cardlist');
  };

  const handleReset = () => {
    setFormData({
      name: '',
      position: '',
      department: '',
      company: '',
      phone: '',
      email: '',
      address: '',
      fax: '',
      website: '',
      memo: ''
    });
  };

  return (
    <div className="card-input-container">
      <div className="card-input-box">
        <div className="card-input-header">
          <button className="back-button" onClick={handleBack}>
            ←
          </button>
          <h2>명함 정보 입력</h2>
          <div></div>
        </div>

        <form onSubmit={handleSubmit} className="card-input-form">
          <div className="form-section">
            <h3>기본 정보</h3>
            
            <div className="form-field">
              <label>이름 <span className="required">*</span></label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="홍길동"
                required
              />
            </div>

            <div className="form-row">
              <div className="form-field">
                <label>직급</label>
                <input
                  type="text"
                  name="position"
                  value={formData.position}
                  onChange={handleInputChange}
                  placeholder="대리"
                />
              </div>
              
              <div className="form-field">
                <label>부서</label>
                <input
                  type="text"
                  name="department"
                  value={formData.department}
                  onChange={handleInputChange}
                  placeholder="개발팀"
                />
              </div>
            </div>

            <div className="form-field">
              <label>회사 <span className="required">*</span></label>
              <input
                type="text"
                name="company"
                value={formData.company}
                onChange={handleInputChange}
                placeholder="NaverCloud"
                required
              />
            </div>
          </div>

          <div className="form-section">
            <h3>연락처 정보</h3>
            
            <div className="form-field">
              <label>휴대폰</label>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                placeholder="010-1234-5678"
                pattern="[0-9]{3}-[0-9]{3,4}-[0-9]{4}"
              />
            </div>

            <div className="form-field">
              <label>이메일</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                placeholder="example@company.com"
              />
            </div>

            <div className="form-field">
              <label>팩스</label>
              <input
                type="tel"
                name="fax"
                value={formData.fax}
                onChange={handleInputChange}
                placeholder="02-123-4567"
              />
            </div>
          </div>

          <div className="form-section">
            <h3>추가 정보</h3>
            
            <div className="form-field">
              <label>주소</label>
              <input
                type="text"
                name="address"
                value={formData.address}
                onChange={handleInputChange}
                placeholder="서울시 강남구"
              />
            </div>

            <div className="form-field">
              <label>웹사이트</label>
              <input
                type="url"
                name="website"
                value={formData.website}
                onChange={handleInputChange}
                placeholder="www.company.com"
              />
            </div>

            <div className="form-field">
              <label>메모</label>
              <textarea
                name="memo"
                value={formData.memo}
                onChange={handleInputChange}
                placeholder="추가 메모사항"
                rows="3"
              />
            </div>
          </div>

          <div className="form-actions">
            <button type="button" onClick={handleReset} className="reset-button">
              초기화
            </button>
            <button type="submit" className="submit-button">
              저장
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CardInput;
