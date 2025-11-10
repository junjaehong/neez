import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import Logo from '../components/Logo';
import FAB from '../components/FAB';
import './MyPage.css';

const MyPage = () => {
  const navigate = useNavigate();
  const { myCard, logout } = useApp();
  
  const handleBack = () => {
    navigate('/main');
  };
  
  const handleMyCard = () => {
    navigate('/mycard');
  };
  
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="mypage-container">
      <div className="mypage-box">

        {/* 마이페이지 헤더 */}
        <div className="mypage-header">
          <button className="back-button" onClick={handleBack}>
            ←
          </button>
          <Logo size="small" />
        </div>

        {/* 반갑습니다 */}
        <div className="welcome-text">
          <h2>반갑습니다. <strong>{myCard.name}</strong>님</h2>
        </div>

        {/* 마이페이지 메뉴 */}
        <div className="mypage-menu">
          <ul>
            <li onClick={handleMyCard}>내 명함 관리</li>
            <li>계정관리</li>
            <li>환경설정</li>
          </ul>
        </div>

        {/* 로그아웃 */}
        <div className="logout" onClick={handleLogout}>
          <h3>LOGOUT</h3>
        </div>

        {/* 카메라 버튼 */}
        <FAB />
        
      </div>
    </div>
  );
};

export default MyPage;


// import React, { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import Logo from '../components/Logo';
// import FAB from '../components/FAB';
// import '../components/InputField';
// import './Mypage.css';

// const Mypage = ({name}) => {

//   const navigate = useNavigate();
//   const handleBack = () => {
//     navigate('/main');
//   };
//   const handleMyCard = () => {
//     navigate('/mycard');
//   };

//   const [isOn, setinOn] = useState(false);
//   const toggleMyId = () => {
//     setinOn(!isOn);
//   };


//   return (
//     <div className="mypage-container">
//       <div className="mypage-box">

//         {/* 마이페이지 헤더 */}
//         <div className="mypage-header">
//           <button className="back-button" onClick={handleBack}>
//             ←
//           </button>
//           <Logo size="small" />
//         </div>

//         {/* 반갑습니다 */}
//         <div className="welcome-text">
//           <h2>반갑습니다. <strong>{name}</strong>님</h2>
//         </div>

//         {/* 마이페이지 메뉴 */}
//         <div className="mypage-menu">
//           <ul>
//             <li onClick={handleMyCard}>내 명함 관리</li>
//             <li onClick={toggleMyId}>계정관리
//             <div className={`toggle-myid ${isOn ? "toggle-content" : null}`}>
//               <p>비밀번호 변경</p>
//               <p>이메일 주소 변경</p>
//               <p>회원탈퇴</p>
//             </div> 
//             </li>
//             <li>환경설정</li>
//           </ul>
//         </div>

//         {/* 로그아웃 */}
//         <div className="logout">
//           <h3>LOGOUT</h3>
//         </div>

//         {/* 카메라 버튼 */}
//         <FAB />
        
//       </div>
//     </div>
//   );
// };

// export default Mypage;
