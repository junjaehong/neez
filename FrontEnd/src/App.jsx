import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AppProvider } from './context/AppContext';
import Start from './pages/Start';
import Login from './pages/Login';
import Join from './pages/Join';
import Main from './pages/Main';
import Mypage from './pages/MyPage';
import Mycard from './pages/Mycard';
import Cardlist from './pages/Cardlist';
import CameraCapture from './pages/CameraCapture';
import CardInput from './pages/CardInput';
import CardDetail from './pages/CardDetail';
import SttCardSelect from './pages/SttCardSelect';
import SttIng from './pages/SttIng';
import Setting from './pages/Setting';
import Password from './pages/Password';
import Email from './pages/Email';
import './App.css';

function App() {
  return (
    <AppProvider>
      <Router>
        <div className="App">
          <Routes>
            <Route path="/" element={<Start />} />
            <Route path="/login" element={<Login />} />
            <Route path="/join" element={<Join />} />
            <Route path="/main" element={<Main />} />
            <Route path="/mypage" element={<Mypage />} />
            <Route path="/mycard" element={<Mycard />} />
            <Route path="/cardlist" element={<Cardlist />} />
            <Route path="/carddetail/:id" element={<CardDetail />} />
            <Route path="/cameracapture" element={<CameraCapture />} />
            <Route path="/cardinput" element={<CardInput />} />
            <Route path="/sttcardselect" element={<SttCardSelect />} />
            <Route path="/stting" element={<SttIng />} />
            <Route path="/setting" element={<Setting />} />
            <Route path="/password" element={<Password />} />
            <Route path="/email" element={<Email />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </div>
      </Router>
    </AppProvider>
  );
}

export default App;
