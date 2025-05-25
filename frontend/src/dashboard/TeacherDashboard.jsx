import { Routes, Route, Navigate } from 'react-router-dom';
import DashboardHome from '@/page/dashboards/TeacherDashboardPage';
import { NotificationProvider } from '@/context/NotificationContext';
import {
  TestsPage,
  GroupsPage,
  StudentsPage,
  ApiKeysPage,
  PromptsPage,
} from '@/page/tables';
import {
  TestDetailsPage,
  AttemptReviewPage,
  TeacherGroupDetailsPage,
} from '@/page/details';
import { TestFormPage } from '@/page/creation';

const TeacherDashboard = () => {
  return (
    <NotificationProvider>
      <Routes>
        <Route index element={<DashboardHome />} />
        <Route path="students" element={<StudentsPage />} />
        <Route path="groups" element={<GroupsPage />} />
        <Route path="groups/:id" element={<TeacherGroupDetailsPage />} />
        <Route path="tests" element={<TestsPage />} />
        <Route path="tests/:id" element={<TestDetailsPage />} />
        <Route path="tests/create" element={<TestFormPage />} />
        <Route path="tests/:id/edit" element={<TestFormPage />} />

        <Route path="api-keys" element={<ApiKeysPage />} />
        <Route path="prompts" element={<PromptsPage />} />

        <Route
          path="attempts/review/:attemptId"
          element={<AttemptReviewPage />}
        />

        <Route path="*" element={<Navigate to="/teacher" replace />} />
      </Routes>
    </NotificationProvider>
  );
};

export default TeacherDashboard;
