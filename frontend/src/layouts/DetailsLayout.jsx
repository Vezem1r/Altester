import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import AppLayout from './AppLayout';

const DetailsLayout = ({ children, breadcrumbs = [] }) => {
  const { t } = useTranslation();
  const location = useLocation();

  const userRole = localStorage.getItem('userRole') || 'TEACHER';
  const basePath = userRole === 'ADMIN' ? '/admin' : '/teacher';

  return (
    <AppLayout>
      <nav
        className="mb-4 flex"
        aria-label={t('detailsLayoutBreadcrumb', 'Breadcrumb')}
      >
        <ol className="flex items-center space-x-4">
          <li>
            <Link to={basePath} className="text-gray-400 hover:text-gray-500">
              <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z" />
              </svg>
              <span className="sr-only">{t('detailsLayoutHome', 'Home')}</span>
            </Link>
          </li>
          {breadcrumbs.map((crumb, index) => (
            <li key={index} className="flex items-center">
              <svg
                className="flex-shrink-0 h-5 w-5 text-gray-400"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                  clipRule="evenodd"
                />
              </svg>
              {crumb.href ? (
                <Link
                  to={crumb.href}
                  className="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                >
                  {crumb.name}
                </Link>
              ) : (
                <span className="ml-4 text-sm font-medium text-gray-500">
                  {crumb.name}
                </span>
              )}
            </li>
          ))}
        </ol>
      </nav>
      {children}
    </AppLayout>
  );
};

export default DetailsLayout;
