import React, { useState, useEffect  } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import Button from '../components/Button';
import InputField from '../components/InputField';
import './MyCard.css';

const MyCard = () => {
  const navigate = useNavigate();
  const { currentUser, updateCurrentUser, fetchMyCard, updateMyCard } = useApp();
  const [formData, setFormData] = useState({
    name: '',
    cardCompanyName: '',
    department: '',
    position: '',
    phone: '',
    email: '',
    address: '',
    fax: '',
    website: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // 컴포넌트 마운트 시 최신 데이터 가져오기
  useEffect(() => {
    loadUserData();
  }, []);

  const loadUserData = async () => {
    try {
      // 먼저 서버에서 최신 데이터 가져오기
      const response = await fetchMyCard();
      const userData = response.data;
      // console.log(userData);
      setFormData({
        name: userData.name || '',
        cardCompanyName: userData.cardCompanyName || '',
        department: userData.department || '',
        position: userData.position || '',
        phone: userData.phone || '',
        email: userData.email || '',
        address: userData.address || '',
        fax: userData.fax || '',
        website: userData.website || ''
      });
    } catch (err) {
      // 서버 데이터 가져오기 실패 시 로컬 데이터 사용
      if (currentUser) {
        setFormData({
          name: currentUser.name || '',
          cardCompanyName: currentUser.cardCompanyName || '',
          department: currentUser.department || '',
          position: currentUser.position || '',
          phone: currentUser.phone || '',
          email: currentUser.email || '',
          address: currentUser.address || '',
          fax: currentUser.fax || '',
          website: currentUser.website || ''
        });
      }
    }
  };

  // useEffect(() => {
  //   if (currentUser) setFormData(currentUser);
  // }, [currentUser]);

  const handleBack = () => {
    navigate('/mypage');
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (error) setError('');
  };

  const validateForm = () => {
    if (!formData.name) {
      setError('이름은 필수 입력 사항입니다.');
      return false;
    }
    
    if (formData.email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        setError('올바른 이메일 형식을 입력해주세요.');
        return false;
      }
    }
    
    if (formData.phone) {
      const phoneRegex = /^[0-9-]+$/;
      if (!phoneRegex.test(formData.phone)) {
        setError('전화번호는 숫자와 하이픈(-)만 입력 가능합니다.');
        return false;
      }
    }
    
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {

      const payload = {
        ...formData,
        cardCompanyName: formData.cardCompanyName
      };

      await updateMyCard(formData);
      alert('내 명함이 수정되었습니다!');
      navigate('/main');
    } catch (err) {
      console.error('명함 수정 실패:', err);
      setError('명함 수정 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

   const handleReset = () => {
    loadUserData();
    setError('');
  };


  return (
    <div className="mycard-container">
      <div className="mycard-box">
        <div className="mycard-header app-header">
          <button className="back-btn" onClick={handleBack}>←</button>
          <p>내 명함 관리</p>
        </div>

        <div className="mycard-content">
          <form onSubmit={handleSubmit}>
            {error && (
              <div className="error-banner">
                {error}
              </div>
            )}

              <div className="form-group">
                <label>이름 <span className="required">*</span></label>
                <input
                  name="name"
                  type="text"
                  value={formData.name || ''}
                  onChange={handleInputChange}
                  placeholder="이름 입력"
                  disabled={loading}
                />
              </div>
              
              <div className="form-group">
                <label>회사</label>
                <input
                  name="cardCompanyName"
                  type="text"
                  value={formData.cardCompanyName || ''}
                  onChange={handleInputChange}
                  placeholder="회사명 입력"
                  disabled={loading}
                />
              </div>
              
              <div className="form-group">
                <label>부서</label>
                <input
                  name="department"
                  type="text"
                  value={formData.department || ''}
                  onChange={handleInputChange}
                  placeholder="부서명 입력"
                  disabled={loading}
                />
              </div>
              
              <div className="form-group">
                <label>직급</label>
                <input
                  name="position"
                  type="text"
                  value={formData.position || ''}
                  onChange={handleInputChange}
                  placeholder="직급 입력"
                  disabled={loading}
                />
              </div>
              
              <div className="form-group">
                <label>휴대전화</label>
                <input
                  name="phone"
                  type="text"
                  value={formData.phone || ''}
                  onChange={handleInputChange}
                  placeholder="010-1234-5678"
                  disabled={loading}
                />
              </div>
              
              <div className="form-group">
                <label>이메일</label>
                <input
                  name="email"
                  type="email"
                  value={formData.email || ''}
                  onChange={handleInputChange}
                  placeholder="example@company.com"
                  disabled={loading}
                />
              </div>
              
              <div className="form-group">
                <label>팩스</label>
                <input
                  name="fax"
                  type="text"
                  value={formData.fax || ''}
                  onChange={handleInputChange}
                  placeholder="02-123-4567"
                  disabled={loading}
                />
              </div>
              
              <div className="form-group">
                <label>주소</label>
                <input
                  name="address"
                  type="text"
                  value={formData.address || ''}
                  onChange={handleInputChange}
                  placeholder="회사 주소 입력"
                  disabled={loading}
                />
              </div>
              
              <div className="form-group">
                <label>웹사이트</label>
                <input
                  name="website"
                  type="text"
                  value={formData.website || ''}
                  onChange={handleInputChange}
                  placeholder="www.company.com"
                  disabled={loading}
                />
              </div>
            </form>
          </div>

        <Button
          className="mycard-save-btn"
          variant="primary"
          fullWidth
          onClick={handleSubmit}
        >
          저장
        </Button>
      </div>
    </div>
  );
};

export default MyCard;

