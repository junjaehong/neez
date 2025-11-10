import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import './HashtagList.css';

const HashtagList = () => {
  const navigate = useNavigate();
  const { hashtags, deleteHashtag, addHashtag } = useApp();
  const [showAddInput, setShowAddInput] = useState(false);
  const [newHashtag, setNewHashtag] = useState('');

  const handleBack = () => {
    navigate('/cardlist');
  };

  const handleDelete = (tag) => {
    if (window.confirm(`"${tag}" 해시태그를 삭제하시겠습니까?\n관련된 명함의 해시태그도 함께 삭제됩니다.`)) {
      deleteHashtag(tag);
    }
  };

  const handleAdd = () => {
    if (newHashtag.trim()) {
      const tagWithoutHash = newHashtag.replace('#', '').trim();
      if (tagWithoutHash) {
        addHashtag(tagWithoutHash);
        setNewHashtag('');
        setShowAddInput(false);
      }
    }
  };

  return (
    <div className="hashtag-list-container">
      <div className="hashtag-list-box">
        <div className="hashtag-list-header">
          <button className="back-button" onClick={handleBack}>←</button>
          <h2># 해시태그 관리-삭제</h2>
          <button 
            className="add-hashtag-button"
            onClick={() => setShowAddInput(!showAddInput)}
          >
            +
          </button>
        </div>

        {showAddInput && (
          <div className="add-hashtag-section">
            <input
              type="text"
              value={newHashtag}
              onChange={(e) => setNewHashtag(e.target.value)}
              placeholder="새 해시태그 입력"
              onKeyPress={(e) => e.key === 'Enter' && handleAdd()}
              autoFocus
            />
            <button onClick={handleAdd}>추가</button>
          </div>
        )}

        <div className="hashtag-items">
          {hashtags.map((tag, index) => (
            <div key={index} className="hashtag-item">
              <span className="hashtag-text">해시태그{index + 1}</span>
              <span className="hashtag-value">#{tag}</span>
              <button 
                className="hashtag-delete"
                onClick={() => handleDelete(tag)}
              >
                ×
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default HashtagList;