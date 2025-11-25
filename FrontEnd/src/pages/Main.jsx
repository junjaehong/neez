import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import Logo from '../components/Logo';
import FAB from '../components/FAB';
import './Main.css';

const Main = () => {
  const navigate = useNavigate();
  const { myCard, isLoggedIn, currentUser } = useApp();
  const [isPopupOpen, setIsPopupOpen] = useState(false);

  // 로그인 체크
  React.useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login');
    }
  }, [isLoggedIn, navigate]);

  const handleMypage = () => {
    navigate('/mypage');
  };

  const handleCardlist = () => {
    navigate('/cardlist');
  };

  const handleMic = () => {
    navigate('/SttCardSelect');
  };

  const handleCardClick = () => setIsPopupOpen(true);
  const handleClosePopup = () => setIsPopupOpen(false);

  return (
    <div className="main-container">
      <div className={`main-box ${isPopupOpen ? 'dimmed' : ''}`}>
        
        {/* 메인 헤더 */}
        <div className="main-header">
          <Logo size="medium_left" />
          <div className="header-right">
            <div className="icon" onClick={handleMic} title="녹음">Mic</div>
            <div className="icon" onClick={handleCardlist} title="명함 목록">List</div>
            <div className="icon" onClick={handleMypage} title="마이페이지">My</div>
          </div>
        </div>

        {/* 명함 박스 - Context에서 데이터 가져오기 */}
        <div className="main-card-content" onClick={handleCardClick}>
          <div className="main-card-text">    
            <h1 className="card-company">{myCard.company}</h1>
            <ul>
              <li className="card-rank">
                <span className="card-name">{myCard.name}</span> {myCard.position} | {myCard.department}
              </li>
              <li className="card-number">{myCard.phone}</li>
              <li className="card-email">{myCard.email}</li>
            </ul>
          </div>
        </div>

        {/* 카메라 버튼 */}
        <FAB />
        
      </div>
      {/* 팝업창 - Context 데이터 사용 */}
      {isPopupOpen && (
        <div className="popup-content">
          <button className="popup-close" onClick={handleClosePopup}>
            ×
          </button>
          <table>
            <tbody>
              <tr>
                <td>이름</td>
                <td>{myCard.name}</td>
              </tr>
              <tr>
                <td>직급</td>
                <td>{myCard.position}</td>
              </tr>
              <tr>
                <td>부서</td>
                <td>{myCard.department}</td>
              </tr>
              <tr>
                <td>회사 이름</td>
                <td>{myCard.company}</td>
              </tr>
              <tr>
                <td>휴대전화</td>
                <td>{myCard.phone}</td>
              </tr>
              <tr>
                <td>이메일</td>
                <td>{myCard.email}</td>
              </tr>
              {myCard.address && (
                <tr>
                  <td>주소</td>
                  <td>{myCard.address}</td>
                </tr>
              )}
              {myCard.website && (
                <tr>
                  <td>웹사이트</td>
                  <td>{myCard.website}</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

    </div>
  );
};

export default Main;


// import React, { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import Logo from '../components/Logo';
// import FAB from '../components/FAB';
// import './Main.css';

// const Main = () => {
  
//   const navigate = useNavigate();
//   const handleMypage = () => {
//     navigate('/mypage');
//   };

//   const handleCardlist = () => {
//     navigate('/cardlist');
//   };

//   const [isPopupOpen, setIsPopupOpen] = useState(false);

//   const handleCardClick = () => setIsPopupOpen(true);
//   const handleClosePopup = () => setIsPopupOpen(false);

//   return (
//     <div className="main-container">
//       <div className={`main-box ${isPopupOpen ? 'dimmed' : ''}`}>

//         {/* 메인 헤더 */}
//         <div className="main-header">
//           <Logo size="medium_left" />
//           <div className="header-right">
//             <div className="icon">icon1</div>
//             <div className="icon" onClick={handleCardlist}>icon2</div>
//             <div className="icon" onClick={handleMypage}>icon3</div>
//           </div>
//         </div>

//         {/* 명함 박스 */}
//         <div className="main-card-box" onClick={handleCardClick}>
//             <div className="main-card-text">    
//                 <h1 className="card-company">NaverCloud</h1>
//                 <ul>
//                     <li className="card-rank"><span className="card-name">홍길동</span> 팀장|총무팀</li>
//                     <li className="card-number">010-1234-5678</li>
//                     <li className="card-email">asdf@naver.com</li>
//                 </ul>
//             </div>
//         </div>

//         {/* 카메라 버튼 */}
//         <FAB />
        
//       </div>

//       {/* 팝업창 */}
//       {isPopupOpen && (
//         // <div className="popup-overlay">
//           <div className="popup-content">
//             <button className="popup-close" onClick={handleClosePopup}>
//               ×
//             </button>
//             <table>
//               <tbody>
//                 <tr>
//                   <td>이름</td>
//                   <td>홍길동</td>
//                 </tr>
//                 <tr>
//                   <td>직급</td>
//                   <td>팀장</td>
//                 </tr>
//                 <tr>
//                   <td>부서</td>
//                   <td>총무팀</td>
//                 </tr>
//                 <tr>
//                   <td>회사 이름</td>
//                   <td>NaverCloud</td>
//                 </tr>
//                 <tr>
//                   <td>휴대전화</td>
//                   <td>010-1234-5678</td>
//                 </tr>
//                 <tr>
//                   <td>이메일</td>
//                   <td>asdf@naver.com</td>
//                 </tr>
//               </tbody>
//             </table>
//           </div>
//         // </div>
//       )}

    
//     </div>
  
      
//   );
// };

// export default Main;
