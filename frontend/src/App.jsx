import { Suspense, useEffect, useRef, useState } from 'react';
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import AuthPage from '@/page/AuthPage';
import { AuthProvider, useAuth, api } from '@/context/AuthContext';
import { ProtectedRoute, RoleRoute } from '@/context/ProtectedRoute';
import AdminDashboard from '@/dashboard/AdminDashboard';
import TeacherDashboard from '@/dashboard/TeacherDashboard';
import StudentDashboard from '@/dashboard/StudentDashboard';
import { ChatProvider } from '@/context/ChatContext';
import ChatModal from '@/components/chat/ChatModal';
import { NotificationProvider } from '@/context/NotificationContext';
import '@/i18n/i18n';
import { setupDemoMode } from '@/services/apiUtils';

setupDemoMode();

const IS_DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true';

if (IS_DEMO_MODE) {
  window.onerror = () => true;
  
  window.onunhandledrejection = (event) => {
    event.preventDefault();
    return true;
  };
  
  window.addEventListener('error', (e) => {
    e.preventDefault();
    e.stopPropagation();
    return false;
  });
}

const SESSION_TOKEN_VALIDATED = 'token_validated_session';

const RoleBasedRedirect = () => {
  const userRole = localStorage.getItem('userRole');

  if (userRole === 'ADMIN') {
    return <Navigate to="/admin" replace />;
  } else if (userRole === 'TEACHER') {
    return <Navigate to="/teacher" replace />;
  } else if (userRole === 'STUDENT') {
    return <Navigate to="/student" replace />;
  }
  return <Navigate to="/" replace />;
};

const TokenValidator = () => {
  const { logout } = useAuth();
  const validationPerformedRef = useRef(false);

  useEffect(() => {
    if (IS_DEMO_MODE) {
      sessionStorage.setItem(SESSION_TOKEN_VALIDATED, 'true');
      return;
    }

    const token = localStorage.getItem('token');
    const sessionValidated = sessionStorage.getItem(SESSION_TOKEN_VALIDATED);

    if (
      !token ||
      sessionValidated === 'true' ||
      validationPerformedRef.current
    ) {
      return;
    }

    validationPerformedRef.current = true;

    const validateToken = async () => {
      try {
        await api.get('/auth/validate-token');
        sessionStorage.setItem(SESSION_TOKEN_VALIDATED, 'true');
      } catch {
        if (!IS_DEMO_MODE) {
          logout(true);
        }
      }
    };

    validateToken();
    const handleBeforeUnload = () => {};

    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [logout]);

  return null;
};

function AppRoutes() {
  const token = localStorage.getItem('token');
  const userRole = localStorage.getItem('userRole');
  const isAuthenticated = !!token;

  if (isAuthenticated && window.location.pathname === '/') {
    if (userRole === 'ADMIN') {
      return <Navigate to="/admin" replace />;
    } else if (userRole === 'TEACHER') {
      return <Navigate to="/teacher" replace />;
    } else if (userRole === 'STUDENT') {
      return <Navigate to="/student" replace />;
    }
  }

  const needsChat = userRole === 'STUDENT' || userRole === 'TEACHER';

  return (
    <>
      <Routes>
        <Route path="/" element={<AuthPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<RoleRoute allowedRoles={['TEACHER']} />}>
            <Route path="/teacher/*" element={<TeacherDashboard />} />
          </Route>

          <Route element={<RoleRoute allowedRoles={['STUDENT']} />}>
            <Route path="/student/*" element={<StudentDashboard />} />
          </Route>

          <Route element={<RoleRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin/*" element={<AdminDashboard />} />
          </Route>

          <Route path="*" element={<RoleBasedRedirect />} />
        </Route>
      </Routes>
      {needsChat && <ChatModal />}
    </>
  );
}

function App() {
  const [currentUserRole, setCurrentUserRole] = useState(
    localStorage.getItem('userRole')
  );

  useEffect(() => {
    const handleStorageChange = () => {
      setCurrentUserRole(localStorage.getItem('userRole'));
    };

    window.addEventListener('storage', handleStorageChange);

    const checkRole = () => {
      const newRole = localStorage.getItem('userRole');
      if (newRole !== currentUserRole) {
        setCurrentUserRole(newRole);
      }
    };

    const intervalId = window.setInterval(checkRole, 500);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.clearInterval(intervalId);
    };
  }, [currentUserRole]);

  const needsChatProvider =
    currentUserRole === 'STUDENT' || currentUserRole === 'TEACHER';

  return (
    <Router>
      <AuthProvider>
        <NotificationProvider>
          {needsChatProvider ? (
            <ChatProvider>
              <Suspense
                fallback={
                  <div className="flex items-center justify-center h-screen">
                    <div className="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-purple-500" />
                  </div>
                }
              >
                <TokenValidator />
                <ToastContainer position="top-right" autoClose={3000} />
                <AppRoutes />
              </Suspense>
            </ChatProvider>
          ) : (
            <Suspense
              fallback={
                <div className="flex items-center justify-center h-screen">
                  <div className="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-purple-500" />
                </div>
              }
            >
              <TokenValidator />
              <ToastContainer position="top-right" autoClose={3000} />
              <AppRoutes />
            </Suspense>
          )}
        </NotificationProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;