import React from 'react';
import './InputField.css';

const InputField = ({ 
  label, 
  type = 'text', 
  placeholder, 
  value, 
  onChange, 
  name,
  rightButton,
  onRightButtonClick,
  disabled = false,
  readOnly = false 
}) => {
  return (
    <div className="input-field">
      <label htmlFor={name}>{label}</label>
      <div className="input-wrapper">
        <input
          id={name}
          name={name}
          type={type}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          disabled={disabled}
          readOnly={readOnly}
        />
        {rightButton && (
          <button 
            type="button" 
            className="input-button" 
            onClick={onRightButtonClick}
          >
            {rightButton}
          </button>
        )}
      </div>
    </div>
  );
};

export default InputField;
