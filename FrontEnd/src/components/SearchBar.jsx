import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import './SearchBar.css';

const SearchBar = ({ onSearch }) => {
  const [inputValue, setInputValue] = useState('');

  const handleSearch = (e) => {
    e.preventDefault();
    if (onSearch) {
      onSearch(inputValue);
    }
  };

  return (
    <form className="search-container" onSubmit={handleSearch}>
      <div className="search-content">
        <input
          type="text"
          placeholder="이름, 회사명으로 검색"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          className="search-input"
        />
        <button type="submit" className="search-button">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="9" cy="9" r="8" stroke="#737164" strokeWidth="2"/>
            <path d="M14 14L18 18" stroke="#737164" strokeWidth="2" strokeLinecap="round"/>
          </svg>
        </button>
      </div>
    </form>
  );
};

export default SearchBar;



// import React, { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import './SearchBar.css';

// const SearchBar = () => {
  //// const [inputValue, setInputValue] = useState('');
  //// const navigate = useNavigate();

  //// const handleSearch = (e) => {
  ////   e.preventDefault();
  ////   if (inputValue.trim()) {
  ////     setSearchQuery(inputValue);
  ////     // 실제로는 여기서 API 호출하여 분석 결과를 받아와야 함
  ////     // 임시 데이터 생성
  ////     const mockResult = {
  ////       credibility: 82,
  ////       bias: 40,
  ////       dateSpread: 60,
  ////       sources: [
  ////         { date: '2025-10-30', title: '트럼프 “한국의 핵추진 잠수함 건조 승인' },
  ////         { date: '2025-10-30', title: '트럼프 \"한국의 핵추진 잠수함 건조 승인…한미군사동맹 강력' },
  ////         { date: '2025-10-30', title: '트럼프 대통령 “한국 핵추진 잠수함 건조 승인' }
  ////       ],
  ////       mainUrl: 'https://n.news.naver.com/mnews/article/214/0001458544?sid=104'
  ////     };
  ////     setAnalysisResult(mockResult);
  ////     navigate('/result');
  ////   }
  //// };

//   return (
//     <form className="search-container" >
//       <div className="search-box">
//         <input
//           type="text"
//           placeholder=""
//           // value={inputValue}
//           // onChange={(e) => setInputValue(e.target.value)}
//           className="search-input"
//         />
//         <button type="submit" className="search-button">
//           <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
//             <circle cx="9" cy="9" r="8" stroke="#737164" strokeWidth="2"/>
//             <path d="M14 14L18 18" stroke="#737164" strokeWidth="2" strokeLinecap="round"/>
//           </svg>
//         </button>
//       </div>
//     </form>
//   );
// };

// export default SearchBar;