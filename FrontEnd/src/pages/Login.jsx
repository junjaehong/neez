import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import Logo from '../components/Logo';
import InputField from '../components/InputField';
import Button from '../components/Button';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const { login } = useApp();
  const [formData, setFormData] = useState({
    id: '',
    password: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.id || !formData.password) {
      alert('아이디와 비밀번호를 입력해주세요.');
      return;
    }
    
    // 간단한 로그인 시뮬레이션
    if (login(formData.id, formData.password)) {
      alert('로그인 성공!');
      navigate('/main');
    } else {
      alert('로그인 실패. 다시 시도해주세요.');
    }
  };

  return (
    <div className="login-container">
      <div className="login-content">
        <Logo size="medium" />
        <form onSubmit={handleSubmit} className="login-form">
          <InputField
            name="id"
            type="text"
            placeholder="ID"
            value={formData.id}
            onChange={handleChange}
          />
          <InputField
            name="password"
            type="password"
            placeholder="PW"
            value={formData.password}
            onChange={handleChange}
          />
          <Button type="submit" variant="primary" fullWidth>
            로그인
          </Button>
        </form>
        <div className="login-footer">
          <Link to="/join" className="join-link">회원가입</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;

// import React, { useState } from 'react';
// import { useNavigate, Link } from 'react-router-dom';
// import Logo from '../components/Logo';
// import InputField from '../components/InputField';
// import Button from '../components/Button';
// import './Login.css';

// const Login = () => {
//   const navigate = useNavigate();
//   const [formData, setFormData] = useState({
//     id: '',
//     password: ''
//   });

//   const handleChange = (e) => {
//     const { name, value } = e.target;
//     setFormData(prev => ({
//       ...prev,
//       [name]: value
//     }));
//   };

//   const handleSubmit = (e) => {
//     e.preventDefault();
//     // 로그인 로직 구현
//     console.log('로그인 시도:', formData);
//     // 실제로는 API 호출 후 성공시 메인 페이지로 이동
//     alert('로그인 기능은 백엔드 연동이 필요합니다.');
//   };

//   return (
//     <div className="login-container">
//       <div className="login-content">
//         <Logo size="medium" />
//         <form onSubmit={handleSubmit} className="login-form">
//           <InputField
//             name="id"
//             type="text"
//             placeholder="ID"
//             value={formData.id}
//             onChange={handleChange}
//           />
//           <InputField
//             name="password"
//             type="password"
//             placeholder="PW"
//             value={formData.password}
//             onChange={handleChange}
//           />
//           <Button type="submit" variant="primary" fullWidth>
//             로그인
//           </Button>
//         </form>
//         <div className="login-footer">
//           <Link to="/join" className="join-link">회원가입</Link>
//         </div>
//       </div>
//     </div>
//   );
// };

// export default Login;
