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
    <form className="search-area" onSubmit={handleSearch}>
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

