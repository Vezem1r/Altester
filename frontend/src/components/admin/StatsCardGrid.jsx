import StatsCard from './StatsCard';
import { useTranslation } from 'react-i18next';

const StatsCardGrid = ({ stats }) => {
  const { t } = useTranslation();

  const cards = [
    {
      title: t('statsCardGrid.teachers', 'Teachers'),
      count: stats.teachersCount,
      icon: (
        <svg
          className="h-6 w-6 text-white"
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
      bgColor: 'bg-indigo-500',
      textColor: 'text-indigo-600',
      linkTo: '/admin/teachers',
      hoverColor: 'hover:text-indigo-900',
    },
    {
      title: t('statsCardGrid.students', 'Students'),
      count: stats.studentsCount,
      icon: (
        <svg
          className="h-6 w-6 text-white"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path d="M12 14l9-5-9-5-9 5 9 5z" />
          <path d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998a12.078 12.078 0 01.665-6.479L12 14z" />
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M12 14l9-5-9-5-9 5 9 5zm0 0l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998a12.078 12.078 0 01.665-6.479L12 14zm-4 6v-7.5l4-2.222"
          />
        </svg>
      ),
      bgColor: 'bg-purple-500',
      textColor: 'text-purple-600',
      linkTo: '/admin/students',
      hoverColor: 'hover:text-purple-900',
    },
    {
      title: t('statsCardGrid.subjects', 'Subjects'),
      count: stats.subjectsCount,
      icon: (
        <svg
          className="h-6 w-6 text-white"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
          />
        </svg>
      ),
      bgColor: 'bg-blue-500',
      textColor: 'text-blue-600',
      linkTo: '/admin/subjects',
      hoverColor: 'hover:text-blue-900',
    },
    {
      title: t('statsCardGrid.groups', 'Groups'),
      count: stats.groupsCount,
      icon: (
        <svg
          className="h-6 w-6 text-white"
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
      bgColor: 'bg-green-500',
      textColor: 'text-green-600',
      linkTo: '/admin/groups',
      hoverColor: 'hover:text-green-900',
    },
    {
      title: t('statsCardGrid.tests', 'Tests'),
      count: stats.testsCount,
      icon: (
        <svg
          className="h-6 w-6 text-white"
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
      bgColor: 'bg-yellow-500',
      textColor: 'text-yellow-600',
      linkTo: '/admin/tests',
      hoverColor: 'hover:text-yellow-900',
    },
  ];

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
      {cards.map((card, index) => (
        <StatsCard key={index} {...card} />
      ))}

      <div className="bg-white overflow-hidden shadow rounded-lg">
        <div className="p-5">
          <div className="flex items-center">
            <div className="flex-shrink-0 bg-purple-100 rounded-md p-3">
              <svg
                className="h-6 w-6 text-purple-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                />
              </svg>
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  {t('statsCardGrid.aiAccuracy', 'AI Accuracy')}
                </dt>
                <dd>
                  <div className="text-lg font-medium text-gray-900">
                    {stats.aiAccuracy || '98.7'}%
                  </div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
        <div className="bg-gray-50 px-5 py-3">
          <div className="text-sm">
            <div className="flex items-center justify-between">
              <span className="font-medium text-purple-600">
                {t('statsCardGrid.active', 'Active')}
              </span>
              <span className="text-gray-500 text-xs inline-flex items-center">
                <svg
                  className="h-4 w-4 mr-1 text-green-500"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
                {t('statsCardGrid.highPerformance', 'High Performance')}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StatsCardGrid;
