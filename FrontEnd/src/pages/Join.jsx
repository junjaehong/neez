import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
// import { useApp } from '../context/AppContext';
import Logo from '../components/Logo';
import InputField from '../components/InputField';
import Button from '../components/Button';
import './Join.css';

const Join = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    userId: '',
    password: '',
    passwordCheck: '',
    name: '',
    email: '',
    verificationCode: ''
  });

  const [verificationSent, setVerificationSent] = useState(false);
  const [idChecked, setIdChecked] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // ID가 변경되면 중복체크 상태 초기화
    if (name === 'userId') {
      setIdChecked(false);
    }
  };

  const handleBack = () => {
    navigate('/login');
  };

  const handleIdCheck = () => {
    if (!formData.userId) {
      alert('아이디를 입력해주세요.');
      return;
    }
    try {
      alert('사용 가능한 아이디입니다.'); // 시뮬레이션
      setIdChecked(true);
    } catch (error) {
      alert('아이디 중복확인 중 오류가 발생했습니다.');
    }
  };
    // 실제로는 API 호출하여 중복 체크
    // 시뮬레이션: 모든 ID 사용 가능
  //   alert('사용 가능한 아이디입니다.');
  //   setIdChecked(true);
  // };

  const handleSendVerification = async() => {
    if (!formData.email) {
      alert('이메일을 입력해주세요.');
      return;
    }
    try {
      alert('인증번호가 발송되었습니다. (시뮬레이션: 1234 입력)');
      setVerificationSent(true);
    } catch (error) {
      alert('이메일 전송 중 오류가 발생했습니다.');
    }
  };
    // 실제로는 API 호출하여 인증번호 발송
  //   alert('인증번호가 발송되었습니다. (시뮬레이션: 1234 입력)');
  //   setVerificationSent(true);
  // };

  const handleVerificationCheck = () => {
    if (!formData.verificationCode) {
      alert('인증번호를 입력해주세요.');
      return;
    }
    if (formData.verificationCode === '1234') {
      alert('인증되었습니다.');
    } else {
      alert('잘못된 인증번호입니다. 1234를 입력해주세요.');
    }
  };
    // 시뮬레이션: 1234만 허용
  //   if (formData.verificationCode === '1234') {
  //     alert('인증되었습니다.');
  //   } else {
  //     alert('잘못된 인증번호입니다. 1234를 입력해주세요.');
  //   }
  // };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 유효성 검사
    if (!idChecked) {
      alert('아이디 중복체크를 해주세요.');
      return;
    }
    
    if (formData.password !== formData.passwordCheck) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }
    
    if (!verificationSent) {
      alert('이메일 인증을 완료해주세요.');
      return;
    }
    
    // 회원가입 처리
    // if (register(formData)) {
    //   alert('회원가입이 완료되었습니다.');
    //   navigate('/login');
    // }

    setLoading(true);
    try {
      const response = await fetch('http://localhost:8083/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: formData.userId,
          password: formData.password,
          name: formData.name,
          email: formData.email
        }),
      });

      if (!response.ok) {
        throw new Error('서버 응답 오류');
      }

      const data = await response.json();

      if (data.success) {
        alert('회원가입이 완료되었습니다!');
        navigate('/login');
      } else {
        alert(data.message || '회원가입에 실패했습니다.');
      }
    } catch (error) {
      console.error('회원가입 에러:', error);
      alert('회원가입 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="join-container">
      <div className="join-box">
        <div className="join-header">
          <button className="back-button" onClick={handleBack}>
            ←
          </button>
          <Logo size="small" />
        </div>
        
        <form onSubmit={handleSubmit} className="join-form">
          <InputField
            label="아이디"
            name="userId"
            type="text"
            placeholder="ID"
            value={formData.id}
            onChange={handleChange}
            rightButton="중복체크"
            onRightButtonClick={handleIdCheck}
          />
          
          <InputField
            label="비밀번호"
            name="password"
            type="password"
            placeholder="PW"
            value={formData.password}
            onChange={handleChange}
          />
          
          <InputField
            label="비밀번호 재확인"
            name="passwordCheck"
            type="password"
            placeholder="PW(check)"
            value={formData.passwordCheck}
            onChange={handleChange}
          />
          
          <InputField
            label="이름"
            name="name"
            type="text"
            placeholder="NAME"
            value={formData.name}
            onChange={handleChange}
          />
          
          <InputField
            label="이메일"
            name="email"
            type="email"
            placeholder="E-MAIL"
            value={formData.email}
            onChange={handleChange}
            rightButton="인증번호 보내기"
            onRightButtonClick={handleSendVerification}
          />
          
          <InputField
            label="인증번호"
            name="verificationCode"
            type="text"
            placeholder="1234"
            value={formData.verificationCode}
            onChange={handleChange}
            rightButton="확인"
            onRightButtonClick={handleVerificationCheck}
          />
          
          <Button type="submit" variant="primary" fullWidth>
            {loading ? '가입 중...' : '회원가입'}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default Join;
