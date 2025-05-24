import React from 'react';
import Header from './Header';

const StudentHeader = ({ user, onLogout, resetToCurrentSemester }) => {
  return (
    <Header
      user={user}
      onLogout={onLogout}
      resetToCurrentSemester={resetToCurrentSemester}
      logoText="AITester"
    />
  );
};

export default StudentHeader;
