import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import Button from '../components/Button';
import './MyCard.css';

const MyCard = () => {
  const navigate = useNavigate();
  const { myCard, updateMyCard } = useApp();
  const [formData, setFormData] = useState({
    ...myCard
  });

  const handleBack = () => {
    navigate('/mypage');
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
    updateMyCard(formData);
    alert('내 명함이 수정되었습니다!');
    navigate('/main');
  };

  return (
    <div className="mycard-container">
      <div className="mycard-box">

        {/* 내 명함 관리 헤더 */}
        <div className="mycard-header">
          <button className="back-button" onClick={handleBack}>
            ←
          </button>
          <h2>내 명함 관리</h2>
          <div></div>
        </div>

        {/* 내 명함 수정 폼 */}
        <div className="mycard-content">
          <table>
            <tbody>
              <tr>
                <td>이름</td>
                <td>
                  <input 
                    type="text" 
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    // placeholder='홍길동'
                  />
                </td>
              </tr>
              <tr>
                <td>직급</td>
                <td>
                  <input 
                    type="text" 
                    name="position"
                    value={formData.position}
                    onChange={handleInputChange}
                    // placeholder='팀장'
                  />
                </td>
              </tr>
              <tr>
                <td>부서</td>
                <td>
                  <input 
                    type="text" 
                    name="department"
                    value={formData.department}
                    onChange={handleInputChange}
                    // placeholder='총무팀'
                  />
                </td>
              </tr>
              <tr>
                <td>회사 이름</td>
                <td>
                  <input 
                    type="text" 
                    name="company"
                    value={formData.company}
                    onChange={handleInputChange}
                    // placeholder='NaverCloud'
                  />
                </td>
              </tr>
              <tr>
                <td>휴대전화</td>
                <td>
                  <input 
                    type="text" 
                    name="phone"
                    value={formData.phone}
                    onChange={handleInputChange}
                    // placeholder='010-1234-5678'
                  />
                </td>
              </tr>
              <tr>
                <td>이메일</td>
                <td>
                  <input 
                    type="email" 
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    // placeholder='asdf@naver.com'
                  />
                </td>
              </tr>
              <tr>
                <td>주소</td>
                <td>
                  <input 
                    type="text" 
                    name="address"
                    value={formData.address || ''}
                    onChange={handleInputChange}
                    // placeholder='서울시 강남구'
                  />
                </td>
              </tr>
              <tr>
                <td>팩스</td>
                <td>
                  <input 
                    type="text" 
                    name="fax"
                    value={formData.fax || ''}
                    onChange={handleInputChange}
                    // placeholder='02-123-4567'
                  />
                </td>
              </tr>
              <tr>
                <td>웹사이트</td>
                <td>
                  <input 
                    type="text" 
                    name="website"
                    value={formData.website || ''}
                    onChange={handleInputChange}
                    // placeholder='www.navercloud.com'
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <Button type="button" variant="primary" fullWidth onClick={handleSubmit}>
          저장
        </Button>
        
      </div>
    </div>
  );
};

export default MyCard;



// import React, { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import '../components/InputField';
// import Button from '../components/Button';
// import './Mycard.css';

// const Mycard = () => {

//   const navigate = useNavigate();
//     const handleBack = () => {
//       navigate('/mypage');
//     };

    
//   return (
//     <div className="mycard-container">
//       <div className="mycard-box">

//         {/* 내 명함 관리 헤더 */}
//         <div className="mycard-header">
//           <button className="back-button" onClick={handleBack}>
//             ←
//           </button>
//         </div>

//         {/* 내 명함 수정안 */}
//         <div className="mycard-content">
//           <table>
//               <tbody>
//                 <tr>
//                   <td>이름</td>
//                   <td><input type="text" placeholder='홍길동'/></td>
//                 </tr>
//                 <tr>
//                   <td>직급</td>
//                   <td><input type="text" placeholder='팀장'/></td>
//                 </tr>
//                 <tr>
//                   <td>부서</td>
//                   <td><input type="text" placeholder='총무팀'/></td>
//                 </tr>
//                 <tr>
//                   <td>회사 이름</td>
//                   <td><input type="text" placeholder='NaverCloud'/></td>
//                 </tr>
//                 <tr>
//                   <td>휴대전화</td>
//                   <td><input type="text" placeholder='010-1234-5678'/></td>
//                 </tr>
//                 <tr>
//                   <td>이메일</td>
//                   <td>asdf@naver.com</td>
//                 </tr>
//                 {/* <tr>
//                   <td>유선전화</td>
//                   <td><input type="text" placeholder='036-000-0000'/></td>
//                 </tr> */}
//               </tbody>
//             </table>
//           {/* <form onSubmit={handleSubmit} className="login-form">
//             <InputField
//               name="id"
//               type="text"
//               placeholder="ID"
//               value={formData.id}
//               onChange={handleChange}
//             />
//             <InputField
//               name="password"
//               type="password"
//               placeholder="PW"
//               value={formData.password}
//               onChange={handleChange}
//             />
//             </form> */}
//         <p className="mycard-plus">추가</p>
//         </div>
//         <Button type="submit" variant="primary" fullWidth>
//           저장
//         </Button>
        
//       </div>
//     </div>
//   );
// };

// export default Mycard;
