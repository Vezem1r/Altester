import { Routes, Route, Navigate } from 'react-router-dom';
import { NotificationProvider } from '@/context/NotificationContext';
import StudentDashboardPage from '@/page/dashboards/StudentDashboardPage';
import TestPage from '@/page/student/TestPage';
import TestResultsPage from '@/page/student/TestResultsPage';
import AttemptReviewPage from '@/page/student/AttemptReviewPage';

const StudentApp = () => {
  return (
    <NotificationProvider>
      <Routes>
        <Route index element={<StudentDashboardPage />} />
        <Route path="tests/:id" element={<TestPage />} />
        <Route path="test-results/:id" element={<TestResultsPage />} />
        <Route path="attempt-review/:id" element={<AttemptReviewPage />} />
        <Route path="*" element={<Navigate to="/student" replace />} />
      </Routes>
    </NotificationProvider>
  );
};

export default StudentApp;
