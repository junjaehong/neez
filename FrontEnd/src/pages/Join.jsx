import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import api from '../api/client';
import Logo from '../components/Logo';
import InputField from '../components/InputField';
import Button from '../components/Button';
import './Join.css';

const Join = () => {
  const navigate = useNavigate();

  const { register } = useApp();
  
  const [formData, setFormData] = useState({
    userId: '',
    password: '',
    passwordCheck: '',
    name: '',
    email: '',
    verificationCode: ''
  });

  const [idChecked, setIdChecked] = useState(false);
  const [emailChecked, setEmailChecked] = useState(false);
  const [checkingId, setCheckingId] = useState(false);
  const [checkingEmail, setCheckingEmail] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  // const [verificationSent, setVerificationSent] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value}));
      if (name === 'userId') setIdChecked(false);
      if (name === 'email') { setEmailChecked(false); }
      if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
    };
    
   // ID 중복 체크
  const handleIdCheck = async () => {
    if (!formData.userId) {
      alert('아이디를 입력해주세요.');
      return;
    }
    if (formData.userId.length < 4) {
      alert('아이디는 4자 이상이어야 합니다.');
      return;
    }
    setCheckingId(true);

    try {
      const response = await api.get('/api/auth/check-id', {
        params: { userId: formData.userId }
      });
      console.log('API 응답 전체:', response.data);
      
      const result = response.data?.data;
      console.log('ID 중복체크 응답:', result);
      
      // API 응답에 따라 처리
      if (result.exists === true) {
        alert('이미 사용 중인 아이디입니다.');
        setIdChecked(false);
      } else if (result.exists === false) {
        alert('사용 가능한 아이디입니다.');
        setIdChecked(true);
      } else {
        // 예상치 못한 응답 구조
        alert('중복 확인에 실패했습니다.');
        setIdChecked(false);
      }
    } catch (err) {
      console.error('ID 중복확인 에러:', err);
      
      if (err.response?.status === 409) {
        alert('이미 사용 중인 아이디입니다.');
        setIdChecked(false);
      } else {
        alert('아이디 중복확인 중 오류가 발생했습니다.');
        setIdChecked(false);
      }
    } finally {
      setCheckingId(false);
    }
  };


  // 이메일 중복 체크
  const handleEmailCheck = async () => {
    if (!formData.email) {
      alert('이메일을 입력해주세요.');
      return;
    }
    
    // 이메일 형식 검증
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      alert('올바른 이메일 형식을 입력해주세요.');
      return;
    }

    setCheckingEmail(true);
    
    try {
      const response = await api.get('/api/auth/check-email', {
        params: { email: formData.email }
      });
      
      
      const result = response.data?.data;
      console.log('이메일 중복체크 응답:', result);
      
      // API 응답에 따라 처리
      if (result.exists === true) {
        alert('이미 사용 중인 이메일입니다.');
        setEmailChecked(false);
      } else if (result.exists === false) {
        alert('사용 가능한 이메일입니다.');
        setEmailChecked(true);
      } else {
        // 예상치 못한 응답 구조
        alert('중복 확인에 실패했습니다.');
        setEmailChecked(false);
      }
    } catch (err) {
      console.error('이메일 중복확인 에러:', err);
      
      if (err.response?.status === 409) {
        alert('이미 사용 중인 이메일입니다.');
        setEmailChecked(false);
      } else {
        alert('이메일 중복확인 중 오류가 발생했습니다.');
        setEmailChecked(false);
      }
    } finally {
      setCheckingEmail(false);
    }
  };



 // 폼 검증
  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.userId) {
      newErrors.userId = '아이디를 입력해주세요.';
    } else if (formData.userId.length < 4) {
      newErrors.userId = '아이디는 4자 이상이어야 합니다.';
    } else if (!idChecked) {
      newErrors.userId = '아이디 중복체크를 해주세요.';
    }
    
    if (!formData.password) {
      newErrors.password = '비밀번호를 입력해주세요.';
    } else if (formData.password.length < 4) {
      newErrors.password = '비밀번호는 4자 이상이어야 합니다.';
    }
    
    if (!formData.passwordCheck) {
      newErrors.passwordCheck = '비밀번호 확인을 입력해주세요.';
    } else if (formData.password !== formData.passwordCheck) {
      newErrors.passwordCheck = '비밀번호가 일치하지 않습니다.';
    }
    
    if (!formData.name) {
      newErrors.name = '이름을 입력해주세요.';
    }
    
    if (!formData.email) {
      newErrors.email = '이메일을 입력해주세요.';
    } else {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        newErrors.email = '올바른 이메일 형식을 입력해주세요.';
      } else if (!emailChecked) {
        newErrors.email = '이메일 중복체크를 해주세요.';
      }
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 회원가입 제출
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    
    try {
      await register({
        userId: formData.userId,
        password: formData.password,
        name: formData.name,
        email: formData.email
      });

      alert('회원가입이 완료되었습니다.');
      navigate('/login');
    } catch (error) {
      console.error('회원가입 에러:', error);
      
      // 서버 에러 메시지 처리
      if (error.response?.data?.message) {
        alert(error.response.data.message);
      } else if (error.message) {
        alert(error.message);
      } else {
        alert('회원가입 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleBack = () => {
    navigate('/login');
  };

  return (
    <div className="join-container">
      <div className="join-box">
        <div className="join-header app-header">
          <span className="back-btn" onClick={handleBack}>
            ←
          </span>
          <div className="logo">
            <img src=".\public\Neez-Logo-S.png" alt="logo" />
          </div>
        </div>
        
        <form onSubmit={handleSubmit} className="join-form">
          <InputField
            label="아이디"
            name="userId"
            type="text"
            placeholder="ID (4자 이상)"
            value={formData.userId}
            onChange={handleChange}
            rightButton="중복체크"
            onRightButtonClick={handleIdCheck}
            disabled={checkingId || loading}
            error={errors.userId}
          />
          {idChecked && (
            <div className="success-message">✓ 사용 가능한 아이디입니다</div>
          )}
          
          <InputField
            label="비밀번호"
            name="password"
            type="password"
            placeholder="PW (4자 이상)"
            value={formData.password}
            onChange={handleChange}
            disabled={loading}
            error={errors.password}
          />
          
          <InputField
            label="비밀번호 재확인"
            name="passwordCheck"
            type="password"
            placeholder="PW(check)"
            value={formData.passwordCheck}
            onChange={handleChange}
            disabled={loading}
            error={errors.passwordCheck}
          />
          
          <InputField
            label="이름"
            name="name"
            type="text"
            placeholder="NAME"
            value={formData.name}
            onChange={handleChange}
            disabled={loading}
            error={errors.name}
          />
          
          <InputField
            label="이메일"
            name="email"
            type="email"
            placeholder="E-MAIL"
            value={formData.email}
            onChange={handleChange}
            rightButton={"중복체크"}
            onRightButtonClick={handleEmailCheck}
            disabled={checkingEmail || loading}
            error={errors.email}
          />
          {emailChecked && (
            <div className="success-message">✓ 사용 가능한 이메일입니다</div>
          )}
          
          <Button type="submit" variant="primary" className="join-btn" fullWidth disabled={loading}>
            회원가입
            {/* {loading ? '가입 중...' : '회원가입'} */}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default Join;
