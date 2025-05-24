import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import TeacherLayout from '@/layouts/TeacherLayout';
import { TeacherService } from '@/services/TeacherService';
import AIModelComponent from '@/components/admin/AIModelComponent';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const DashboardHome = () => {
  const { t } = useTranslation();
  const [teacherData, setTeacherData] = useState({
    username: '',
    students: 0,
    tests: 0,
    aiAccuracy: 0,
    subjects: [],
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTeacherData = async () => {
      try {
        setLoading(true);
        const data = await TeacherService.getTeacherPage();
        setTeacherData(data);
      } catch (error) {
        toast.error(
          error.message ||
            t(
              'teacherDashboardPage.errorLoadingData',
              'Error loading dashboard data'
            )
        );
      } finally {
        setLoading(false);
      }
    };

    fetchTeacherData();
  }, [t]);

  const getTotalGroupCount = () => {
    if (!teacherData.subjects || teacherData.subjects.length === 0) return 0;

    return teacherData.subjects.reduce((total, subject) => {
      return (
        total + (subject.groups?.filter(group => group.active)?.length || 0)
      );
    }, 0);
  };

  const generateStatsCards = () => {
    const cards = [];

    if (teacherData.students !== undefined) {
      cards.push({
        id: 'students',
        title: t('teacherDashboardPage.studentsLabel', 'Students'),
        count: teacherData.students,
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
        hoverColor: 'hover:text-purple-900',
        linkTo: '/teacher/students',
      });
    }

    if (teacherData.tests !== undefined) {
      cards.push({
        id: 'tests',
        title: t('teacherDashboardPage.tests', 'Tests'),
        count: teacherData.tests,
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
        hoverColor: 'hover:text-yellow-900',
        linkTo: '/teacher/tests',
      });
    }

    if (teacherData.subjects && teacherData.subjects.length > 0) {
      cards.push({
        id: 'groups',
        title: t('teacherDashboardPage.activeGroupsLabel', 'Active Groups'),
        count: getTotalGroupCount(),
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
        hoverColor: 'hover:text-green-900',
        linkTo: '/teacher/groups',
      });
    }

    if (teacherData.aiAccuracy !== undefined) {
      cards.push({
        id: 'ai-accuracy',
        title: t('teacherDashboardPage.aiAccuracy', 'AI Accuracy'),
        isAICard: true,
        aiAccuracy: teacherData.aiAccuracy,
      });
    }

    return cards;
  };

  const renderStatsCard = card => {
    if (card.isAICard) {
      return (
        <div
          key={card.id}
          className="bg-white overflow-hidden shadow rounded-lg"
        >
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
                    {t('teacherDashboardPage.aiAccuracy', 'AI Accuracy')}
                  </dt>
                  <dd>
                    <div className="text-lg font-medium text-gray-900">
                      {card.aiAccuracy || '0'}%
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
                  {t('teacherDashboardPage.active', 'Active')}
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
                  {t(
                    'teacherDashboardPage.highPerformance',
                    'High Performance'
                  )}
                </span>
              </div>
            </div>
          </div>
        </div>
      );
    }
    return (
      <div key={card.id} className="bg-white overflow-hidden shadow rounded-lg">
        <div className="p-5">
          <div className="flex items-center">
            <div className={`flex-shrink-0 ${card.bgColor} rounded-md p-3`}>
              {card.icon}
            </div>
            <div className="ml-5 w-0 flex-1">
              <dl>
                <dt className="text-sm font-medium text-gray-500 truncate">
                  {card.title}
                </dt>
                <dd>
                  <div className="text-lg font-medium text-gray-900">
                    {card.count !== undefined && card.count !== null
                      ? card.count.toLocaleString()
                      : '0'}
                  </div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
        <div className="bg-gray-50 px-5 py-3">
          <div className="text-sm">
            <Link
              to={card.linkTo}
              className={`font-medium ${card.textColor} ${card.hoverColor}`}
            >
              {t('teacherDashboardPage.viewAll', 'View all {{items}}', {
                items: card.title.toLowerCase(),
              })}
            </Link>
          </div>
        </div>
      </div>
    );
  };

  const getSubjectColor = index => {
    const colors = [
      {
        light: 'bg-blue-50',
        medium: 'bg-blue-100',
        dark: 'text-blue-800',
        accent: 'bg-blue-500',
        border: 'border-blue-200',
        text: 'text-blue-600',
        hover: 'hover:bg-blue-600',
        badge: 'bg-blue-100 text-blue-800',
      },
      {
        light: 'bg-purple-50',
        medium: 'bg-purple-100',
        dark: 'text-purple-800',
        accent: 'bg-purple-500',
        border: 'border-purple-200',
        text: 'text-purple-600',
        hover: 'hover:bg-purple-600',
        badge: 'bg-purple-100 text-purple-800',
      },
      {
        light: 'bg-indigo-50',
        medium: 'bg-indigo-100',
        dark: 'text-indigo-800',
        accent: 'bg-indigo-500',
        border: 'border-indigo-200',
        text: 'text-indigo-600',
        hover: 'hover:bg-indigo-600',
        badge: 'bg-indigo-100 text-indigo-800',
      },
      {
        light: 'bg-green-50',
        medium: 'bg-green-100',
        dark: 'text-green-800',
        accent: 'bg-green-500',
        border: 'border-green-200',
        text: 'text-green-600',
        hover: 'hover:bg-green-600',
        badge: 'bg-green-100 text-green-800',
      },
      {
        light: 'bg-red-50',
        medium: 'bg-red-100',
        dark: 'text-red-800',
        accent: 'bg-red-500',
        border: 'border-red-200',
        text: 'text-red-600',
        hover: 'hover:bg-red-600',
        badge: 'bg-red-100 text-red-800',
      },
      {
        light: 'bg-yellow-50',
        medium: 'bg-yellow-100',
        dark: 'text-yellow-800',
        accent: 'bg-yellow-500',
        border: 'border-yellow-200',
        text: 'text-yellow-600',
        hover: 'hover:bg-yellow-600',
        badge: 'bg-yellow-100 text-yellow-800',
      },
      {
        light: 'bg-pink-50',
        medium: 'bg-pink-100',
        dark: 'text-pink-800',
        accent: 'bg-pink-500',
        border: 'border-pink-200',
        text: 'text-pink-600',
        hover: 'hover:bg-pink-600',
        badge: 'bg-pink-100 text-pink-800',
      },
      {
        light: 'bg-cyan-50',
        medium: 'bg-cyan-100',
        dark: 'text-cyan-800',
        accent: 'bg-cyan-500',
        border: 'border-cyan-200',
        text: 'text-cyan-600',
        hover: 'hover:bg-cyan-600',
        badge: 'bg-cyan-100 text-cyan-800',
      },
    ];

    return colors[index % colors.length];
  };

  return (
    <TeacherLayout>
      <div className="bg-white shadow overflow-hidden sm:rounded-lg mb-6">
        <div className="px-4 py-5 sm:px-6">
          <h1 className="text-lg leading-6 font-medium text-gray-900">
            {t('teacherDashboardPage.title', 'Teacher Dashboard')}
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-gray-500">
            {t('teacherDashboardPage.welcome', 'Welcome')},{' '}
            {teacherData.username}
          </p>
        </div>

        <div className="border-t border-gray-200">
          {loading ? (
            <div className="flex justify-center items-center py-12">
              <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-purple-600" />
            </div>
          ) : (
            <div className="px-4 py-5 sm:p-6">
              <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
                {generateStatsCards().map(card => renderStatsCard(card))}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="mt-8 mb-8">
        <div className="bg-white rounded-xl overflow-hidden shadow-lg">
          <div className="px-6 py-5 border-b border-gray-200">
            <div>
              <h3 className="text-xl font-bold text-gray-800">
                {t('teacherDashboardPage.mySubjects', 'My Subjects')}
              </h3>
              <p className="text-sm text-gray-600 mt-1">
                {t(
                  'teacherDashboardPage.coursesAndGroups',
                  'Courses and student groups'
                )}
              </p>
            </div>
          </div>

          <div className="p-6">
            {teacherData.subjects && teacherData.subjects.length > 0 ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
                {teacherData.subjects.map((subject, idx) => {
                  const activeGroups =
                    subject.groups?.filter(group => group.active) || [];
                  const totalStudents = activeGroups.reduce(
                    (sum, group) => sum + group.studentCount,
                    0
                  );
                  const totalTests = activeGroups.reduce(
                    (sum, group) => sum + group.testCount,
                    0
                  );
                  const color = getSubjectColor(idx);

                  if (activeGroups.length === 0) return null;

                  return (
                    <div
                      key={subject.name}
                      className="bg-white rounded-xl overflow-hidden shadow-md transition-all duration-300 hover:shadow-xl border border-gray-200 flex flex-col"
                    >
                      <div className="p-5 flex flex-col">
                        <div className="flex justify-between items-start">
                          <div className="flex-1">
                            <h3 className="text-lg font-bold text-gray-900 mb-1">
                              {subject.name}
                            </h3>
                            {subject.description && (
                              <p className="text-sm text-gray-600 line-clamp-2 mb-3">
                                {subject.description}
                              </p>
                            )}
                          </div>
                          <span
                            className={`ml-2 px-3 py-1 text-xs font-semibold rounded-full ${color.badge} flex items-center`}
                          >
                            {subject.shortName ||
                              subject.name.substring(0, 3).toUpperCase()}
                          </span>
                        </div>

                        <div className="grid grid-cols-3 gap-2 my-3">
                          <div
                            className={`${color.medium} rounded-lg p-3 text-center`}
                          >
                            <div className="text-xl font-bold text-gray-800">
                              {activeGroups.length}
                            </div>
                            <div className="text-xs text-gray-600 mt-1">
                              {t('teacherDashboardPage.groups', 'Groups')}
                            </div>
                          </div>
                          <div
                            className={`${color.medium} rounded-lg p-3 text-center`}
                          >
                            <div className="text-xl font-bold text-gray-800">
                              {totalStudents}
                            </div>
                            <div className="text-xs text-gray-600 mt-1">
                              {t(
                                'teacherDashboardPage.studentsCount',
                                'Students'
                              )}
                            </div>
                          </div>
                          <div
                            className={`${color.medium} rounded-lg p-3 text-center`}
                          >
                            <div className="text-xl font-bold text-gray-800">
                              {totalTests}
                            </div>
                            <div className="text-xs text-gray-600 mt-1">
                              {t('teacherDashboardPage.tests', 'Tests')}
                            </div>
                          </div>
                        </div>
                      </div>

                      <div className="flex-1">
                        <div className="px-5 py-2 bg-gray-50">
                          <h4 className="text-sm font-semibold text-gray-700 flex items-center">
                            <svg
                              className="h-4 w-4 mr-1"
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
                            {t(
                              'teacherDashboardPage.activeGroupsTitle',
                              'Active Groups'
                            )}
                          </h4>
                        </div>
                        <div className="max-h-56 overflow-y-auto px-5 py-3 bg-white">
                          {activeGroups.map(group => (
                            <Link
                              key={group.id || group.groupName}
                              to={`/teacher/groups/${group.id}`}
                              className={`block mb-2 p-3 rounded-lg shadow-sm bg-white hover:shadow-md hover:bg-gray-50 transition-all duration-200 border-l-4 ${color.border} cursor-pointer`}
                            >
                              <div className="flex justify-between items-center">
                                <h5 className="font-medium text-gray-800">
                                  {group.groupName}
                                </h5>
                                <div
                                  className={`px-2 py-1 rounded-full text-xs ${color.badge}`}
                                >
                                  {group.studentCount}{' '}
                                  {group.studentCount === 1
                                    ? t(
                                        'teacherDashboardPage.student',
                                        'student'
                                      )
                                    : t(
                                        'teacherDashboardPage.students',
                                        'students'
                                      )}
                                </div>
                              </div>

                              <div className="flex items-center mt-2 text-sm text-gray-600">
                                <div className="flex items-center mr-4">
                                  <svg
                                    className="h-4 w-4 mr-1"
                                    xmlns="http://www.w3.org/2000/svg"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    stroke="currentColor"
                                  >
                                    <path
                                      strokeLinecap="round"
                                      strokeLinejoin="round"
                                      strokeWidth="2"
                                      d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                                    />
                                  </svg>
                                  {group.testCount}{' '}
                                  {t('teacherDashboardPage.tests', 'Tests')}
                                </div>
                                {group.testCount > 0 && (
                                  <div className="flex items-center ml-auto">
                                    <span
                                      className={`${color.text} text-xs font-medium`}
                                    >
                                      {t(
                                        'teacherDashboardPage.active',
                                        'Active'
                                      )}
                                    </span>
                                  </div>
                                )}
                              </div>
                            </Link>
                          ))}

                          {activeGroups.length === 0 && (
                            <div className="text-center py-4 text-gray-500 text-sm">
                              {t(
                                'teacherDashboardPage.noActiveGroups',
                                'No active groups'
                              )}
                            </div>
                          )}
                        </div>
                      </div>

                      <div className="px-5 py-3 bg-gray-50 border-t border-gray-100">
                        <span className="text-xs font-medium text-gray-500">
                          {activeGroups.length}{' '}
                          {activeGroups.length === 1
                            ? t(
                                'teacherDashboardPage.activeGroup',
                                'active group'
                              )
                            : t(
                                'teacherDashboardPage.activeGroups',
                                'active groups'
                              )}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow overflow-hidden p-8 text-center">
                <svg
                  className="mx-auto h-12 w-12 text-gray-400"
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
                <h3 className="mt-2 text-sm font-medium text-gray-900">
                  {t(
                    'teacherDashboardPage.noSubjectsFound',
                    'No subjects found'
                  )}
                </h3>
                <p className="mt-1 text-sm text-gray-500">
                  {t(
                    'teacherDashboardPage.noSubjectsAssigned',
                    "You don't have any subjects assigned yet."
                  )}
                </p>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="mt-6">
        <AIModelComponent />
      </div>
    </TeacherLayout>
  );
};

export default DashboardHome;
