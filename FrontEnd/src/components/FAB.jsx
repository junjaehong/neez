import React from 'react';
import { useNavigate } from 'react-router-dom';
import './FAB.css';

const FAB = () => {
  const navigate = useNavigate();

  const handleClick = () => {
    // 바로 카메라 페이지로 이동
    navigate('/camera');
  };

  return (
    <button
      className="camera-btn"
      onClick={handleClick}
      title="명함 촬영"
    >
      📷
    </button>
  );
};

export default FAB;


////////////////////////////////////////////////////////////



// import React, { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import './FAB.css';

// const FAB = () => {
//   const [showMenu, setShowMenu] = useState(false);
//   const navigate = useNavigate();

//   const handleCameraClick = () => {
//     navigate('/camera');
//     setShowMenu(false);
//   };

//   const handleManualInput = () => {
//     navigate('/card-input');
//     setShowMenu(false);
//   };

//   return (
//     <>
//       {showMenu && (
//         <div className="fab-menu">
//           <button className="fab-menu-item" onClick={handleCameraClick}>
//             📷 카메라 촬영
//           </button>
//           <button className="fab-menu-item" onClick={handleManualInput}>
//             ✏️ 수기 입력
//           </button>
//         </div>
//       )}
//       <button
//         className="camera-btn"
//         onClick={() => setShowMenu(!showMenu)}
//       >
//         {showMenu ? '✕' : '📷'}
//       </button>
//     </>
//   );
// };

// export default FAB;



////////////////////////////////////////////////////////////


// import React from 'react';
// import './FAB.css';

// const FAB = ({}) => {
//   return (
//     <button
//         className="camera-btn"
//         onClick={() => alert("카메라 촬영 기능 실행!")}
//     >카메라
//     </button>
//   );
// };

// export default FAB;
