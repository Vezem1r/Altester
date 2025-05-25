import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const TeacherSidebar = ({ username, registered }) => {
  const location = useLocation();
  const { t } = useTranslation();

  const isActive = path => {
    return (
      location.pathname === path || location.pathname.startsWith(`${path}/`)
    );
  };

  const navItems = [
    {
      title: t('teacherSidebar.dashboard', 'Dashboard'),
      path: '/teacher',
      icon: (
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
          />
        </svg>
      ),
    },
    {
      title: t('teacherSidebar.students', 'Students'),
      path: '/teacher/students',
      icon: (
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
          />
        </svg>
      ),
    },
    {
      title: t('teacherSidebar.groups', 'Groups'),
      path: '/teacher/groups',
      icon: (
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
          />
        </svg>
      ),
    },
    {
      title: t('teacherSidebar.tests', 'Tests'),
      path: '/teacher/tests',
      icon: (
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
          />
        </svg>
      ),
    },
    {
      title: t('teacherSidebar.apiKeys', 'API Keys'),
      path: '/teacher/api-keys',
      icon: (
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
          />
        </svg>
      ),
    },
    {
      title: t('teacherSidebar.prompts', 'Prompts'),
      path: '/teacher/prompts',
      icon: (
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
      ),
    },
  ];

  return (
    <div className="flex flex-col h-full">
      <nav className="flex-1 px-2 space-y-1 bg-white">
        {navItems.map((item, index) => (
          <Link
            key={index}
            to={item.path}
            className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md ${
              (item.path === '/teacher' &&
                isActive('/teacher') &&
                location.pathname === '/teacher') ||
              (item.path !== '/teacher' && isActive(item.path))
                ? 'bg-purple-50 text-purple-700'
                : 'text-gray-600 hover:bg-purple-50 hover:text-purple-700'
            }`}
          >
            <div
              className={`mr-3 h-6 w-6 ${
                (item.path === '/teacher' &&
                  isActive('/teacher') &&
                  location.pathname === '/teacher') ||
                (item.path !== '/teacher' && isActive(item.path))
                  ? 'text-purple-500'
                  : 'text-gray-400 group-hover:text-purple-500'
              }`}
            >
              {item.icon}
            </div>
            {item.title}
          </Link>
        ))}

        {!registered && (
          <div className="pt-4 px-3">
            <div className="bg-yellow-50 border-l-4 border-yellow-400 p-3 rounded">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg
                    className="h-5 w-5 text-yellow-400"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z"
                    />
                  </svg>
                </div>
                <div className="ml-3">
                  <p className="text-xs text-yellow-700">
                    {t(
                      'teacherSidebar.notRegisteredWarning',
                      'Your account is not fully registered. Some features may be limited.'
                    )}
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
      </nav>
    </div>
  );
};

export default TeacherSidebar;
