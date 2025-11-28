import React, { useRef, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { loadConfig } from '../api/configLoader';
import { getAuthHeader } from '../api/auth';
import axios from 'axios';
import './CameraCapture.css';

const CameraCapture = () => {
  const navigate = useNavigate();
  const { addCard } = useApp();
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [stream, setStream] = useState(null);
  const [capturedImage, setCapturedImage] = useState(null);
  const [extractedData, setExtractedData] = useState({
    name: '',
    cardCompanyName: '',
    department: '',
    position: '',
    email: '',
    phoneNumber: '',
    lineNumber: '',
    faxNumber: '',
    address: '',
    // memoContent: '',
    // hashTags: []
  });

  /////////////////////////////////////
  const [baseURL, setBaseURL] = useState('');

  // config.xml에서 baseURL 가져오기
  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const config = await loadConfig();
        setBaseURL(config.baseURL);
      } catch (err) {
        console.error('config 로드 실패:', err);
      }
    };
    fetchConfig();
  }, []);
  /////////////////////////////////////

  function base64ToBlob(base64) {
    const arr = base64.split(',');
    const mime = arr[0].match(/:(.*?);/)[1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);

    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }

    return new Blob([u8arr], { type: mime });
  }
  /////////////////////////////////////

  const handleOCR = async (base64Image) => {
    if (!baseURL) return;
    try {
      // Base64 → Blob 변환
      const blob = base64ToBlob(base64Image);
      // const response = await fetch(base64Image);
      // const blob = await (await fetch(base64Image)).blob();

      // FormData에 담기
      const formData = new FormData();
      formData.append('file', blob, 'capture.png');

      for (let pair of formData.entries()) {
         console.log("폼데이터:", pair[0], pair[1] instanceof Blob, pair[1].size);
      }

      // OCR API 호출
      const res = await fetch(`${baseURL}/api/bizcards/read/upload`, {
        method: 'POST',
        headers: {
          ...getAuthHeader()
        },
        body: formData,
      });

       if (!res.ok) {
        throw new Error(`HTTP 오류: ${res.status}`);
      }
      
      const data = await res.json();
      console.log('✅ OCR 응답 데이터:', data);

      // API 결과 구조에 맞게 매핑
      // (예: res.data.result.name, res.data.result.phone 등)
      if (data.success && data.data) {
        setExtractedData({
          idx: data.data.idx || '',
          userIdx: data.data.userIdx || '',
          name: data.data.name || '',
          cardCompanyName: data.data.cardCompanyName || '',
          department: data.data.department || '',
          position: data.data.position || '',
          email: data.data.email || '',
          phoneNumber: data.data.phoneNumber || '',
          lineNumber: data.data.lineNumber || '',
          faxNumber: data.data.faxNumber || '',
          address: data.data.address || '',
          // memoContent: res.data.data.memoContent || '',
          // hashTags: res.data.data.hashTags || ''
        });
        
      }
    } catch (err) {
      console.error('❌ OCR 요청 실패:', err);
      alert('OCR 분석 중 오류가 발생했습니다.');
    }
  };

  useEffect(() => {
    // 카메라 권한 요청 및 스트림 시작
    const startCamera = async () => {
      try {
        const mediaStream = await navigator.mediaDevices.getUserMedia({ 
          video: { facingMode: 'environment' } 
        });
        if (videoRef.current) {
          videoRef.current.srcObject = mediaStream;
        }
        setStream(mediaStream);
      } catch (err) {
        console.error('카메라 접근 오류:', err);
        alert('카메라에 접근할 수 없습니다. 권한을 확인해주세요.');
      }
    };

    startCamera();

    // 컴포넌트 언마운트 시 스트림 정리
    return () => {
      if (stream) {
        stream.getTracks().forEach(track => track.stop());
      }
    };
  }, []);

  const capturePhoto = () => {
    if (videoRef.current && canvasRef.current) {
      const video = videoRef.current;
      const canvas = canvasRef.current;
      const context = canvas.getContext('2d');
      
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      context.drawImage(video, 0, 0, canvas.width, canvas.height);
      
      const imageData = canvas.toDataURL('image/png');
      setCapturedImage(imageData);
      
      // OCR 시뮬레이션 (실제로는 API 호출)
      handleOCR(imageData);
      
      // 카메라 스트림 정지
      if (stream) {
        stream.getTracks().forEach(track => track.stop());
      }
    }
  };

  // const simulateOCR = () => {
  //   // 실제로는 OCR API를 호출하여 명함 정보를 추출
  //   // 여기서는 시뮬레이션으로 임시 데이터 생성
  //   setTimeout(() => {
  //     setExtractedData({
  //       name: '김영희',
  //       position: '대리',
  //       department: '기획팀',
  //       company: 'TechCorp',
  //       phone: '010-9876-5432',
  //       email: 'kim@techcorp.com'
  //     });
  //   }, 1000);
  // };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setExtractedData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSave = () => {
    if (!extractedData.name) {
      alert('이름은 필수 입력 사항입니다.');
      return;
    }
    
    addCard(extractedData);
    alert('명함이 저장되었습니다!');
    navigate('/cardlist');
  };

  const handleRetake = () => {
    setCapturedImage(null);
    setExtractedData({
      name: '',
      position: '',
      department: '',
      company: '',
      phone: '',
      email: ''
    });
    
    // 카메라 재시작
    const startCamera = async () => {
      try {
        const mediaStream = await navigator.mediaDevices.getUserMedia({ 
          video: { facingMode: 'environment' } 
        });
        if (videoRef.current) {
          videoRef.current.srcObject = mediaStream;
        }
        setStream(mediaStream);
      } catch (err) {
        console.error('카메라 접근 오류:', err);
      }
    };
    startCamera();
  };

  const handleBack = () => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
    }
    navigate(-1);
  };

  return (
    <div className="camera-container">
      <div className="camera-box">
        <div className="camera-header">
          <button className="back-btn" onClick={handleBack}>
            ←
          </button>
          <p>명함 촬영</p>
          <div></div>
        </div>

        {!capturedImage ? (
          <div className="camera-view">
            <video 
              ref={videoRef} 
              autoPlay 
              playsInline 
              className="camera-video"
            />
            <canvas ref={canvasRef} style={{ display: 'none' }} />
            <div className="camera-overlay">
              <div className="camera-frame"></div>
            </div>
            <button className="capture-button" onClick={capturePhoto}>
              📷
            </button>
          </div>
        ) : (
          <div className="captured-view">
            <img src={capturedImage} alt="Captured" className="captured-image" />
            
            <div className="extracted-data">
              <h3>추출된 정보</h3>
              <p className="ocr-notice">정보를 확인하고 수정해주세요</p>
              
              <div className="data-fields">
                <div className="data-field">
                  <label>이름 *</label>
                  <input
                    type="text"
                    name="name"
                    value={extractedData.name}
                    onChange={handleInputChange}
                    placeholder="이름 입력"
                  />
                </div>
                
                <div className="data-field">
                  <label>직급</label>
                  <input
                    type="text"
                    name="position"
                    value={extractedData.position}
                    onChange={handleInputChange}
                    placeholder="직급 입력"
                  />
                </div>
                
                <div className="data-field">
                  <label>부서</label>
                  <input
                    type="text"
                    name="department"
                    value={extractedData.department}
                    onChange={handleInputChange}
                    placeholder="부서 입력"
                  />
                </div>
                
                <div className="data-field">
                  <label>회사</label>
                  <input
                    type="text"
                    name="company"
                    value={extractedData.company}
                    onChange={handleInputChange}
                    placeholder="회사명 입력"
                  />
                </div>
                
                <div className="data-field">
                  <label>전화번호</label>
                  <input
                    type="tel"
                    name="phone"
                    value={extractedData.phone}
                    onChange={handleInputChange}
                    placeholder="010-0000-0000"
                  />
                </div>
                
                <div className="data-field">
                  <label>이메일</label>
                  <input
                    type="email"
                    name="email"
                    value={extractedData.email}
                    onChange={handleInputChange}
                    placeholder="email@example.com"
                  />
                </div>
              </div>
              
              <div className="action-buttons">
                <button className="retake-button" onClick={handleRetake}>
                  다시 촬영
                </button>
                <button className="save-button" onClick={handleSave}>
                  저장
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CameraCapture;
