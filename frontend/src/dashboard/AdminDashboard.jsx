import { Routes, Route, Navigate } from 'react-router-dom';
import DashboardHome from '@/page/dashboards/AdminDashboardPage';
import { CreateGroupPage, TestFormPage } from '@/page/creation';
import {
  GroupDetailsPage,
  TestDetailsPage,
  AttemptReviewPage,
} from '@/page/details';
import {
  TeachersPage,
  StudentsPage,
  SubjectsPage,
  GroupsPage,
  TestsPage,
  ApiKeysPage,
  PromptsPage,
} from '@/page/tables';

const AdminDashboard = () => {
  return (
    <Routes>
      <Route path="/" element={<DashboardHome />} />
      <Route path="/teachers" element={<TeachersPage />} />
      <Route path="/students" element={<StudentsPage />} />
      <Route path="/subjects" element={<SubjectsPage />} />
      <Route path="/groups" element={<GroupsPage />} />
      <Route path="/groups/create" element={<CreateGroupPage />} />
      <Route path="/groups/:id" element={<GroupDetailsPage />} />
      <Route path="/prompts" element={<PromptsPage />} />

      <Route path="/api-keys" element={<ApiKeysPage />} />

      <Route path="/tests" element={<TestsPage />} />
      <Route path="/tests/create" element={<TestFormPage />} />
      <Route path="/tests/:id" element={<TestDetailsPage />} />
      <Route path="/tests/:id/edit" element={<TestFormPage />} />

      <Route path="*" element={<Navigate to="/admin" replace />} />

      <Route
        path="/attempts/review/:attemptId"
        element={<AttemptReviewPage />}
      />
    </Routes>
  );
};

export default AdminDashboard;
