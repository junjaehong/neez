import React from 'react';
import './App.css';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AppProvider, useApp } from './context/AppContext';
import Start from './pages/Start';
import Login from './pages/Login';
import Join from './pages/Join';
import Main from './pages/Main';
import Mypage from './pages/MyPage';
import Mycard from './pages/MyCard';
import Cardlist from './pages/Cardlist';
import CameraCapture from './pages/CameraCapture';
import CardInput from './pages/CardInput';
import CardDetail from './pages/CardDetail';
import SttCardSelect from './pages/SttCardSelect';
import SttIng from './pages/SttIng';
import Setting from './pages/Setting';
import Password from './pages/Password';
import Email from './pages/Email';
import FAB from './components/FAB';
///////////
import { loadConfig } from './api/configLoader';
import api from './api/client';

async function initApi() {
  const { baseURL } = await loadConfig();
  api.defaults.baseURL = baseURL;
}

initApi();
/////////////
const ProtectedRoute = ({ children }) => {
  const { isLoggedIn } = useApp();
  if (!isLoggedIn) return <Navigate to="/login" replace />;
  return children;
};

function AppRoutes() {
  const { isLoggedIn } = useApp();
  const { pathname } = useLocation();

  // 로그인/회원가입 경로에서는 FAB 숨김
  const noFabPaths = ['/login', '/join'];
  const showFAB = isLoggedIn && !noFabPaths.includes(pathname);

  return (
    <div className="App">
      {showFAB && <FAB />}

      <Routes>
        <Route path="/" element={<Start />} />
        <Route path="/login" element={<Login />} />
        <Route path="/join" element={<Join />} />

        {/* 보호 라우트: 로그인 필요 */}
        <Route path="/main" element={
          <ProtectedRoute><Main /></ProtectedRoute>
        } />
        <Route path="/mypage" element={
          <ProtectedRoute><Mypage /></ProtectedRoute>
        } />
        <Route path="/mycard" element={
          <ProtectedRoute><Mycard /></ProtectedRoute>
        } />
        <Route path="/cardlist" element={
          <ProtectedRoute><Cardlist /></ProtectedRoute>
        } />
        <Route path="/carddetail/:id" element={
          <ProtectedRoute><CardDetail /></ProtectedRoute>
        } />
        <Route path="/cameracapture" element={
          <ProtectedRoute><CameraCapture /></ProtectedRoute>
        } />
        <Route path="/cardinput" element={
          <ProtectedRoute><CardInput /></ProtectedRoute>
        } />
        <Route path="/sttcardselect" element={
          <ProtectedRoute><SttCardSelect /></ProtectedRoute>
        } />
        <Route path="/stting" element={
          <ProtectedRoute><SttIng /></ProtectedRoute>
        } />
        <Route path="/setting" element={
          <ProtectedRoute><Setting /></ProtectedRoute>
        } />
        <Route path="/password" element={
          <ProtectedRoute><Password /></ProtectedRoute>
        } />
        <Route path="/email" element={
          <ProtectedRoute><Email /></ProtectedRoute>
        } />

        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </div>
  );
}

function App() {
  return (
    // <AppProvider>
    <Router>
      <AppRoutes />
    </Router>
    // </AppProvider>
  );
}

export default App;
