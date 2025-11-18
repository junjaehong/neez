import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, BrowserRouter } from 'react-router-dom';
import Start from './pages/Start';
import Login from './pages/Login';
import Join from './pages/Join';
import Main from './pages/Main';
import MyPage from './pages/MyPage';
import MyCard from './pages/MyCard';
import CardList from './pages/CardList';
import CardDetail from './pages/CardDetail';
import HashtagList from './pages/HashtagList';
import './App.css';
import { AppProvider } from './context/AppContext';

function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Start />} />
          <Route path="/login" element={<Login />} />
          <Route path="/join" element={<Join />} />
          <Route path="/main" element={<Main />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/mycard" element={<MyCard />} />
          <Route path="/cardlist" element={<CardList />} />
          <Route path="/hashtaglist" element={<HashtagList />} />
          <Route path="/carddetail/:id" element={<CardDetail />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}

export default App
