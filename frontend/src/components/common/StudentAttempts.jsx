import { useState, useEffect } from 'react';
import { AttemptService } from '@/services/AttemptService';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

const StudentAttempts = ({ student }) => {
  const { t } = useTranslation();
  const [attempts, setAttempts] = useState({ tests: [] });
  const [loading, setLoading] = useState(true);
  const [expandedTest, setExpandedTest] = useState(null);
  const [userRole, setUserRole] = useState('ADMIN');
  const navigate = useNavigate();

  useEffect(() => {
    const role = localStorage.getItem('userRole') || 'ADMIN';
    setUserRole(role);

    if (student && student.username) {
      fetchStudentAttempts();
    }
  }, [student]);

  const fetchStudentAttempts = async () => {
    try {
      if (!student || !student.username) {
        setAttempts({ tests: [] });
        setLoading(false);
        return;
      }

      setLoading(true);

      let attemptsData;

      if (userRole === 'ADMIN') {
        attemptsData = await AttemptService.getStudentAttemptsForAdmin(
          student.username
        );
      } else if (userRole === 'TEACHER') {
        attemptsData = await AttemptService.getStudentAttemptsForTeacher(
          student.username
        );
      } else {
        throw new Error('Unauthorized access');
      }

      setAttempts(attemptsData || { tests: [] });
    } catch (error) {
      toast.error(
        error.message ||
          t('studentAttempts.failedToLoad', 'Failed to load student attempts')
      );
      setAttempts({ tests: [] });
    } finally {
      setLoading(false);
    }
  };

  const toggleTest = testId => {
    if (expandedTest === testId) {
      setExpandedTest(null);
    } else {
      setExpandedTest(testId);
    }
  };

  const goToTestDetails = (testId, e) => {
    e.stopPropagation(); // Prevent toggling the test expansion
    navigate(`/admin/tests/${testId}`);
  };

  const handleReviewAttempt = (attemptId, e) => {
    e.preventDefault();
    const basePath = userRole === 'ADMIN' ? '/admin' : '/teacher';
    navigate(`${basePath}/attempts/review/${attemptId}`);
  };

  const formatDate = dateString => {
    if (!dateString) return t('studentAttempts.na', 'N/A');
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  const calculateDuration = (startTime, endTime) => {
    if (!startTime || !endTime) return t('studentAttempts.na', 'N/A');

    const start = new Date(startTime);
    const end = new Date(endTime);
    const durationMs = end - start;

    if (durationMs < 0)
      return t('studentAttempts.invalidDuration', 'Invalid duration');

    const minutes = Math.floor(durationMs / 60000);
    const seconds = Math.floor((durationMs % 60000) / 1000);

    if (minutes < 60) {
      return t('studentAttempts.minutesSeconds', '{{minutes}}m {{seconds}}s', {
        minutes,
        seconds,
      });
    }

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    return t(
      'studentAttempts.hoursMinutesSeconds',
      '{{hours}}h {{minutes}}m {{seconds}}s',
      {
        hours,
        minutes: remainingMinutes,
        seconds,
      }
    );
  };

  const getStatusColor = status => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800';
      case 'TIMEOUT':
        return 'bg-yellow-100 text-yellow-800';
      case 'ABANDONED':
        return 'bg-red-100 text-red-800';
      case 'REVIEWED':
        return 'bg-purple-100 text-purple-800';
      case 'AI_REVIEWED':
        return 'bg-indigo-100 text-indigo-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const extractStudentAttempts = () => {
    const result = [];

    if (!attempts.tests) return [];

    attempts.tests.forEach(test => {
      test.attempts.forEach(attempt => {
        result.push({
          ...attempt,
          testId: test.testId,
          testName: test.testName,
        });
      });
    });

    return result.sort((a, b) => {
      return new Date(b.startTime) - new Date(a.startTime);
    });
  };

  const getCompletedTestsCount = () => {
    return attempts.tests.filter(test =>
      test.attempts.some(
        attempt =>
          attempt.status === 'COMPLETED' ||
          attempt.status === 'REVIEWED' ||
          attempt.status === 'AI_REVIEWED'
      )
    ).length;
  };

  const studentAttempts = extractStudentAttempts();
  const completedTestsCount = getCompletedTestsCount();

  if (loading) {
    return (
      <div className="flex justify-center items-center py-6">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600" />
        <p className="ml-3 text-sm text-gray-600">
          {t('studentAttempts.loadingAttempts', 'Loading test attempts...')}
        </p>
      </div>
    );
  }

  if (studentAttempts.length === 0) {
    return (
      <div className="text-center py-6 bg-gray-50 rounded-lg border border-gray-200 mt-4">
        <svg
          className="mx-auto h-10 w-10 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1}
            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
          />
        </svg>
        <h3 className="mt-2 text-sm font-medium text-gray-900">
          {t('studentAttempts.noAttemptsFound', 'No test attempts found')}
        </h3>
        <p className="mt-1 text-sm text-gray-500">
          {t(
            'studentAttempts.noTestsTaken',
            "This student hasn't taken any tests yet."
          )}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4 mt-4">
      <h4 className="text-base font-medium text-gray-900 mb-3">
        {t('studentAttempts.testAttempts', 'Test Attempts')}
      </h4>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
          <div className="flex items-center">
            <div className="flex-shrink-0 bg-indigo-100 rounded-md p-2">
              <svg
                className="h-5 w-5 text-indigo-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-gray-500">
                {t('studentAttempts.totalAttempts', 'Total Attempts')}
              </p>
              <p className="text-lg font-semibold text-gray-900">
                {studentAttempts.length}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
          <div className="flex items-center">
            <div className="flex-shrink-0 bg-purple-100 rounded-md p-2">
              <svg
                className="h-5 w-5 text-purple-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-gray-500">
                {t('studentAttempts.completedTests', 'Completed Tests')}
              </p>
              <p className="text-lg font-semibold text-gray-900">
                {completedTestsCount}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
          <div className="flex items-center">
            <div className="flex-shrink-0 bg-green-100 rounded-md p-2">
              <svg
                className="h-5 w-5 text-green-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-gray-500">
                {t('studentAttempts.averageScore', 'Average Score')}
              </p>
              <p className="text-lg font-semibold text-gray-900">
                {studentAttempts.filter(a => a.score !== null).length > 0
                  ? Math.round(
                      studentAttempts.reduce(
                        (sum, a) => sum + (a.score || 0),
                        0
                      ) / studentAttempts.filter(a => a.score !== null).length
                    )
                  : 0}{' '}
                {t('studentAttempts.pts', 'pts')}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="space-y-3 max-h-[500px] overflow-y-auto pr-1">
        {attempts.tests.map(test => (
          <div
            key={`test-${test.testId}`}
            className="bg-white shadow overflow-hidden rounded-lg border border-gray-200"
          >
            <div
              onClick={() => toggleTest(test.testId)}
              className="px-4 py-3 cursor-pointer hover:bg-gray-50 flex justify-between items-center"
            >
              <div>
                <h5 className="text-sm font-medium text-gray-900">
                  {test.testName}
                </h5>
                <p className="text-xs text-gray-500">
                  {test.attempts.length}{' '}
                  {test.attempts.length === 1
                    ? t('studentAttempts.attempt', 'attempt')
                    : t('studentAttempts.attempts', 'attempts')}
                </p>
              </div>
              <div className="flex items-center space-x-2">
                <button
                  onClick={e => goToTestDetails(test.testId, e)}
                  className="inline-flex items-center px-2.5 py-1.5 border border-transparent text-xs font-medium rounded text-indigo-700 bg-indigo-100 hover:bg-indigo-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                >
                  {t('studentAttempts.viewTest', 'View Test')}
                </button>
                <svg
                  className={`h-5 w-5 text-gray-500 transform transition-transform duration-150 ${expandedTest === test.testId ? 'rotate-180' : ''}`}
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
            </div>

            {expandedTest === test.testId && (
              <div className="border-t border-gray-200">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th
                        scope="col"
                        className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                      >
                        {t('studentAttempts.attemptNumber', 'Attempt #')}
                      </th>
                      <th
                        scope="col"
                        className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                      >
                        {t('studentAttempts.date', 'Date')}
                      </th>
                      <th
                        scope="col"
                        className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                      >
                        {t('studentAttempts.duration', 'Duration')}
                      </th>
                      <th
                        scope="col"
                        className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                      >
                        {t('studentAttempts.score', 'Score')}
                      </th>
                      <th
                        scope="col"
                        className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                      >
                        {t('studentAttempts.status', 'Status')}
                      </th>
                      <th
                        scope="col"
                        className="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
                      >
                        {t('studentAttempts.actions', 'Actions')}
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {test.attempts
                      .sort((a, b) => a.attemptNumber - b.attemptNumber)
                      .map(attempt => (
                        <tr
                          key={`test-${test.testId}-attempt-${attempt.attemptId}`}
                          className="hover:bg-gray-50"
                        >
                          <td className="px-4 py-2 whitespace-nowrap text-sm text-gray-900">
                            {attempt.attemptNumber}
                          </td>
                          <td className="px-4 py-2 whitespace-nowrap text-sm text-gray-500">
                            {formatDate(attempt.startTime)}
                          </td>
                          <td className="px-4 py-2 whitespace-nowrap text-sm text-gray-500">
                            {calculateDuration(
                              attempt.startTime,
                              attempt.endTime
                            )}
                          </td>
                          <td className="px-4 py-2 whitespace-nowrap text-sm font-medium text-gray-900">
                            {attempt.score !== null
                              ? t(
                                  'studentAttempts.scorePoints',
                                  '{{score}} pts',
                                  { score: attempt.score }
                                )
                              : '-'}
                          </td>
                          <td className="px-4 py-2 whitespace-nowrap">
                            <span
                              className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(attempt.status)}`}
                            >
                              {attempt.status}
                            </span>
                          </td>
                          <td className="px-4 py-2 whitespace-nowrap text-right text-sm font-medium">
                            <button
                              onClick={e =>
                                handleReviewAttempt(attempt.attemptId, e)
                              }
                              className="text-purple-600 hover:text-purple-900"
                            >
                              {t(
                                'studentAttempts.reviewAttempt',
                                'Review Attempt'
                              )}
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default StudentAttempts;
