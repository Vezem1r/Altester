import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { useTranslation } from 'react-i18next';

export const ProtectedRoute = () => {
  const { isAuthenticated, isLoading } = useAuth();
  const { t } = useTranslation();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        {t('protectedRoute.loading', 'Loading...')}
      </div>
    );
  }

  return isAuthenticated ? <Outlet /> : <Navigate to="/" />;
};

export const RoleRoute = ({ allowedRoles }) => {
  const { userRole, isAuthenticated, isLoading } = useAuth();
  const { t } = useTranslation();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        {t('protectedRoute.loading', 'Loading...')}
      </div>
    );
  }

  const hasPermission = isAuthenticated && allowedRoles.includes(userRole);

  if (isAuthenticated && !hasPermission) {
    switch (userRole) {
      case 'TEACHER':
        return <Navigate to="/teacher" />;
      case 'STUDENT':
        return <Navigate to="/student" />;
      case 'ADMIN':
        return <Navigate to="/admin" />;
      default:
        return <Navigate to="/" />;
    }
  }

  return hasPermission ? <Outlet /> : <Navigate to="/" />;
};
