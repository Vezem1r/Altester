import React from 'react';
import Header from './Header';
import { useNavigate } from 'react-router-dom';

const TeacherHeader = ({ user, onLogout }) => {
  const navigate = useNavigate();

  const handleLogoClick = () => {
    navigate('/teacher');
  };

  return (
    <Header
      user={user}
      onLogout={onLogout}
      logoText="AITester Teacher"
      onLogoClick={handleLogoClick}
    />
  );
};

export default TeacherHeader;
