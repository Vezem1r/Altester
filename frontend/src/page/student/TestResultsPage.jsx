import React, {
  useState,
  useEffect,
  useCallback,
  useMemo,
  useRef,
} from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { StudentService } from '@/services/StudentService';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';
import StudentHeader from '@/components/header/StudentHeader';

const TestResultsPage = () => {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const { id: attemptId } = useParams();
  const { user, logout } = useAuth();

  const [result, setResult] = useState(location.state?.result || null);
  const [loading, setLoading] = useState(!location.state?.result);

  const fetchedRef = useRef(false);

  const fetchAttemptResult = useCallback(async () => {
    if (fetchedRef.current || !attemptId || location.state?.result) {
      return;
    }

    try {
      setLoading(true);
      fetchedRef.current = true;

      const statusResponse = await StudentService.getAttemptStatus(attemptId);

      if (!statusResponse.isCompleted) {
        toast.error(
          t(
            'testResultsPage.testNotCompleted',
            'This test attempt has not been completed'
          )
        );
        navigate('/student');
        return;
      }

      setResult({
        attemptId: statusResponse.attemptId,
        testTitle: statusResponse.testTitle,
        score: statusResponse.score || 0,
        totalScore: statusResponse.totalScore || 0,
        questionsAnswered: statusResponse.answeredQuestions || 0,
        totalQuestions: statusResponse.totalQuestions || 0,
        completed: statusResponse.isCompleted || false,
        status: statusResponse.status || 'COMPLETED',
      });
    } catch (error) {
      toast.error(
        error.message ||
          t('testResultsPage.failedToLoad', 'Failed to load test results')
      );
      navigate('/student');
    } finally {
      setLoading(false);
    }
  }, [attemptId, location.state, navigate, t]);

  useEffect(() => {
    fetchAttemptResult();

    return () => {
      fetchedRef.current = false;
    };
  }, [fetchAttemptResult]);

  const handleNavigateToStudent = useCallback(() => {
    navigate('/student');
  }, [navigate]);

  const handleViewDetailedReview = useCallback(() => {
    navigate(`/student/attempt-review/${result.attemptId}`);
  }, [navigate, result]);

  const getResultStatus = useMemo(() => {
    if (!result) return null;

    if (result.status === 'AI_REVIEWED') {
      const percentage =
        Math.round((result.score / result.totalScore) * 100) || 0;

      if (percentage >= 90) {
        return {
          label: t('testResultsPage.excellent', 'Excellent'),
          color: 'text-green-600',
          bgColor: 'bg-green-100',
          icon: (
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100">
              <svg
                className="h-8 w-8 text-green-600"
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
          ),
        };
      } else if (percentage >= 75) {
        return {
          label: t('testResultsPage.veryGood', 'Very Good'),
          color: 'text-blue-600',
          bgColor: 'bg-blue-100',
          icon: (
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-blue-100">
              <svg
                className="h-8 w-8 text-blue-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905a3.61 3.61 0 01-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5"
                />
              </svg>
            </div>
          ),
        };
      } else if (percentage >= 60) {
        return {
          label: t('testResultsPage.good', 'Good'),
          color: 'text-purple-600',
          bgColor: 'bg-purple-100',
          icon: (
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-purple-100">
              <svg
                className="h-8 w-8 text-purple-600"
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
          ),
        };
      } else if (percentage >= 40) {
        return {
          label: t('testResultsPage.fair', 'Fair'),
          color: 'text-yellow-600',
          bgColor: 'bg-yellow-100',
          icon: (
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-yellow-100">
              <svg
                className="h-8 w-8 text-yellow-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                />
              </svg>
            </div>
          ),
        };
      }
      return {
        label: t('testResultsPage.needsImprovement', 'Needs Improvement'),
        color: 'text-red-600',
        bgColor: 'bg-red-100',
        icon: (
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100">
            <svg
              className="h-8 w-8 text-red-600"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
        ),
      };
    }
    return {
      label: t('testResultsPage.testCompleted', 'Test Completed'),
      color: 'text-purple-600',
      bgColor: 'bg-purple-100',
      icon: (
        <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100">
          <svg
            className="h-8 w-8 text-green-600"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 13l4 4L19 7"
            />
          </svg>
        </div>
      ),
    };
  }, [result, t]);

  const percentage = useMemo(() => {
    if (!result) return 0;
    return Math.round((result.score / result.totalScore) * 100) || 0;
  }, [result]);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-700" />
      </div>
    );
  }

  if (!result) {
    navigate('/student');
    return null;
  }

  const status = getResultStatus;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="sticky top-0 z-50">
        <StudentHeader
          user={user}
          onLogout={logout}
          resetToCurrentSemester={handleNavigateToStudent}
        />
      </div>

      <div className="pt-6 pb-12">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-white shadow-lg overflow-hidden rounded-xl">
            <div className="px-6 py-8 bg-purple-600 text-white">
              <h1 className="text-2xl font-bold">
                {t('testResultsPage.testResults', 'Test Results')}
              </h1>
              <p className="mt-1 text-purple-100">{result.testTitle}</p>
            </div>

            <div className="px-6 py-8 border-b border-gray-200">
              <div className="text-center">
                {status.icon}
                <h3 className={`mt-4 text-xl font-bold ${status.color}`}>
                  {status.label}
                </h3>

                <div className="mt-6">
                  <div className="relative pt-1">
                    <div className="overflow-hidden h-6 text-xs flex rounded-full bg-purple-50">
                      <div
                        style={{
                          width:
                            result.status === 'AI_REVIEWED'
                              ? `${percentage}%`
                              : `${Math.round((result.questionsAnswered / result.totalQuestions) * 100)}%`,
                        }}
                        className="shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center transition-all duration-500 bg-purple-500"
                      />
                    </div>
                  </div>

                  {result.status === 'AI_REVIEWED' ? (
                    <>
                      <div className="mt-6 flex justify-center items-baseline">
                        <span className="text-6xl font-extrabold text-gray-900">
                          {result.score}
                        </span>
                        <span className="ml-1 text-2xl text-gray-500">
                          / {result.totalScore}
                        </span>
                      </div>
                      <p className="mt-1 text-lg font-medium text-gray-700">
                        {percentage}% {t('testResultsPage.score', 'score')}
                      </p>
                      <div className="mt-2 flex justify-center">
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-purple-100 text-purple-800">
                          <svg
                            className="mr-2 h-4 w-4"
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-8.707l-3-3a1 1 0 00-1.414 0l-3 3a1 1 0 001.414 1.414L9 9.414V13a1 1 0 102 0V9.414l1.293 1.293a1 1 0 001.414-1.414z"
                              clipRule="evenodd"
                            />
                          </svg>
                          {t('testResultsPage.gradedByAI', 'Graded by AI')}
                        </span>
                      </div>
                    </>
                  ) : (
                    <div className="mt-6 text-center">
                      <p className="text-lg font-medium text-purple-700">
                        {t(
                          'testResultsPage.completedQuestions',
                          'You have completed {{answered}} of {{total}} questions',
                          {
                            answered: result.questionsAnswered,
                            total: result.totalQuestions,
                          }
                        )}
                      </p>
                      <div className="mt-3 bg-yellow-50 border border-yellow-100 rounded-lg p-4 inline-block">
                        <div className="flex items-center">
                          <svg
                            className="h-5 w-5 text-yellow-600 mr-2"
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                              clipRule="evenodd"
                            />
                          </svg>
                          <span className="text-sm text-yellow-800">
                            {t(
                              'testResultsPage.teacherReviewNote',
                              'Your test results will be reviewed by your teacher'
                            )}
                          </span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="px-6 py-8 bg-gray-50">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {t('testResultsPage.testDetails', 'Test Details')}
              </h3>
              <dl className="grid grid-cols-1 gap-x-4 gap-y-6 sm:grid-cols-2">
                <div className="sm:col-span-1">
                  <dt className="text-sm font-medium text-gray-500">
                    {t('testResultsPage.testTitle', 'Test Title')}
                  </dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {result.testTitle}
                  </dd>
                </div>
                <div className="sm:col-span-1">
                  <dt className="text-sm font-medium text-gray-500">
                    {t(
                      'testResultsPage.questionsAnswered',
                      'Questions Answered'
                    )}
                  </dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {result.questionsAnswered} {t('testResultsPage.of', 'of')}{' '}
                    {result.totalQuestions}
                  </dd>
                </div>

                {result.status === 'AI_REVIEWED' && (
                  <>
                    <div className="sm:col-span-1">
                      <dt className="text-sm font-medium text-gray-500">
                        {t('testResultsPage.reviewStatus', 'Review Status')}
                      </dt>
                      <dd className="mt-1 text-sm text-gray-900">
                        <span className="px-2 py-1 text-xs font-medium rounded-full bg-purple-100 text-purple-800">
                          {t('testResultsPage.aiReviewed', 'AI Reviewed')}
                        </span>
                      </dd>
                    </div>
                    <div className="sm:col-span-1">
                      <dt className="text-sm font-medium text-gray-500">
                        {t('testResultsPage.review', 'Review')}
                      </dt>
                      <dd className="mt-1 text-sm text-gray-900">
                        <button
                          onClick={handleViewDetailedReview}
                          className="inline-flex items-center px-3 py-1 text-sm font-medium rounded text-purple-700 bg-purple-100 hover:bg-purple-200"
                        >
                          {t(
                            'testResultsPage.viewDetailedReview',
                            'View Detailed Review'
                          )}
                        </button>
                      </dd>
                    </div>
                  </>
                )}

                {result.status !== 'AI_REVIEWED' && (
                  <div className="sm:col-span-2">
                    <div className="bg-purple-50 rounded-lg p-3 mt-2">
                      <div className="flex">
                        <div className="flex-shrink-0">
                          <svg
                            className="h-5 w-5 text-purple-600"
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </div>
                        <div className="ml-3">
                          <p className="text-sm text-purple-800">
                            {t(
                              'testResultsPage.awaitingReviewNote',
                              'Your test has been submitted and is awaiting review by your teacher. Results will be available once grading is complete.'
                            )}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </dl>
            </div>

            <div className="px-6 py-4 bg-gray-100 flex justify-end">
              <button
                onClick={handleNavigateToStudent}
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
              >
                {t('testResultsPage.returnToDashboard', 'Return to Dashboard')}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default React.memo(TestResultsPage);
