import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import InfoCard from '@/components/shared/InfoCard';
import QuestionPreview from '@/components/questions/QuestionPreview';
import { TestService } from '@/services/TestService';
import SharedPagination from '@/components/common/SharedPagination';
import { toast } from 'react-toastify';

const TestPreviewTab = ({ test }) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [previewQuestions, setPreviewQuestions] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [pageSize] = useState(5);
  const [fetchError, setFetchError] = useState(false);

  const formatDuration = durationMinutes => {
    if (!durationMinutes) return t('testPreviewTab.notSet', 'Not set');

    if (durationMinutes < 60) {
      return t('testPreviewTab.minutes', '{{duration}} minutes', {
        duration: durationMinutes,
      });
    }

    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;

    if (minutes === 0) {
      return t('testPreviewTab.hours', '{{hours}} {{hourLabel}}', {
        hours,
        hourLabel:
          hours === 1
            ? t('testPreviewTab.hour', 'hour')
            : t('testPreviewTab.hoursPlural', 'hours'),
      });
    }

    return t('testPreviewTab.hoursAndMinutes', '{{hours}}h {{minutes}}m', {
      hours,
      minutes,
    });
  };

  const fetchPreviewQuestions = useCallback(
    async (isRefreshing = false) => {
      if (!isRefreshing && fetchError) return;

      if (isRefreshing) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      try {
        const response = await TestService.getStudentTestPreview(
          test.id,
          currentPage,
          pageSize
        );
        setPreviewQuestions(response.content || []);
        setTotalPages(response.totalPages || 0);
        setTotalItems(response.totalElements || 0);
        setFetchError(false);

        if (isRefreshing) {
          toast.success(
            t(
              'testPreviewTab.previewRefreshed',
              'Preview refreshed successfully'
            )
          );
        }
      } catch {
        if (isRefreshing) {
          toast.error(
            t(
              'testPreviewTab.failedToRefresh',
              'Failed to refresh preview questions'
            )
          );
        } else {
          toast.error(
            t(
              'testPreviewTab.failedToLoad',
              'Failed to load preview questions. Using fallback data.'
            )
          );
        }

        setFetchError(!isRefreshing);

        if (!isRefreshing) {
          setPreviewQuestions([]);
          setTotalPages(0);
          setTotalItems(0);
        }
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    [test.id, currentPage, pageSize, fetchError, t]
  );

  useEffect(() => {
    if (test && test.id) {
      const timer = setTimeout(() => {
        fetchPreviewQuestions();
      }, 500);

      return () => clearTimeout(timer);
    }
  }, [fetchPreviewQuestions, test]);

  const handlePageChange = newPage => {
    if (!fetchError) {
      setCurrentPage(newPage);
    }
  };

  const handleRefresh = () => {
    fetchPreviewQuestions(true);
  };

  return (
    <InfoCard
      title={t('testPreviewTab.studentView', 'Student View')}
      description={t(
        'testPreviewTab.previewDescription',
        'Preview how this test will appear to students'
      )}
    >
      <div className="mb-6 p-4 bg-gray-50 border border-gray-200 rounded-lg">
        <h2 className="text-xl font-bold text-gray-900 mb-2">{test.title}</h2>
        {test.description && (
          <p className="text-gray-600 mb-4">{test.description}</p>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div>
            <span className="font-medium text-gray-500">
              {t('testPreviewTab.duration', 'Duration:')}{' '}
            </span>
            <span className="text-gray-900">
              {formatDuration(test.duration)}
            </span>
          </div>
          <div>
            <span className="font-medium text-gray-500">
              {t('testPreviewTab.totalPoints', 'Total Points:')}{' '}
            </span>
            <span className="text-gray-900">{test.totalScore}</span>
          </div>
        </div>

        <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-green-50 p-3 rounded-md border border-green-100 flex flex-col">
            <span className="text-sm font-medium text-green-800 flex items-center">
              <span className="h-2 w-2 bg-green-500 rounded-full mr-1" />
              {t('testPreviewTab.easyQuestions', 'Easy Questions:')}
            </span>
            <div className="flex items-center justify-between mt-1">
              <span className="text-green-700 font-medium text-base">
                {test.easyQuestionsCount || 0}
              </span>
              <span className="text-xs bg-green-200 text-green-800 px-2 py-1 rounded-full">
                {t('testPreviewTab.pointsEach', '{{score}} pts each', {
                  score: test.easyScore,
                })}
              </span>
            </div>
          </div>

          <div className="bg-yellow-50 p-3 rounded-md border border-yellow-100 flex flex-col">
            <span className="text-sm font-medium text-yellow-800 flex items-center">
              <span className="h-2 w-2 bg-yellow-500 rounded-full mr-1" />
              {t('testPreviewTab.mediumQuestions', 'Medium Questions:')}
            </span>
            <div className="flex items-center justify-between mt-1">
              <span className="text-yellow-700 font-medium text-base">
                {test.mediumQuestionsCount || 0}
              </span>
              <span className="text-xs bg-yellow-200 text-yellow-800 px-2 py-1 rounded-full">
                {t('testPreviewTab.pointsEach', '{{score}} pts each', {
                  score: test.mediumScore,
                })}
              </span>
            </div>
          </div>

          <div className="bg-red-50 p-3 rounded-md border border-red-100 flex flex-col">
            <span className="text-sm font-medium text-red-800 flex items-center">
              <span className="h-2 w-2 bg-red-500 rounded-full mr-1" />
              {t('testPreviewTab.hardQuestions', 'Hard Questions:')}
            </span>
            <div className="flex items-center justify-between mt-1">
              <span className="text-red-700 font-medium text-base">
                {test.hardQuestionsCount || 0}
              </span>
              <span className="text-xs bg-red-200 text-red-800 px-2 py-1 rounded-full">
                {t('testPreviewTab.pointsEach', '{{score}} pts each', {
                  score: test.hardScore,
                })}
              </span>
            </div>
          </div>
        </div>

        {test.aiEvaluate && (
          <div className="mt-3 text-sm text-gray-500 bg-blue-50 p-2 rounded border border-blue-100 flex items-center">
            <svg
              className="h-5 w-5 text-blue-500 mr-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
              />
            </svg>
            <span>
              {t(
                'testPreviewTab.aiEvaluationEnabled',
                'This test uses AI to automatically evaluate open-ended questions.'
              )}
            </span>
          </div>
        )}
      </div>

      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-medium text-gray-900">
          {t('testPreviewTab.randomQuestionPreview', 'Random Question Preview')}
        </h3>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-purple-600 hover:bg-purple-700 disabled:opacity-50"
        >
          {refreshing ? (
            <>
              <svg
                className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
              {t('testPreviewTab.refreshing', 'Refreshing...')}
            </>
          ) : (
            <>
              <svg
                className="h-4 w-4 mr-1.5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
              {t('testPreviewTab.generateNewPreview', 'Generate New Preview')}
            </>
          )}
        </button>
      </div>

      {fetchError && (
        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 my-4">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              <svg
                className="h-5 w-5 text-yellow-400"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-yellow-700">
                {t(
                  'testPreviewTab.errorLoading',
                  'Error loading preview. Please click "Generate New Preview" to try again.'
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-purple-600" />
          <p className="ml-3 text-gray-600">
            {t('testPreviewTab.loadingPreview', 'Loading preview...')}
          </p>
        </div>
      ) : previewQuestions.length === 0 ? (
        <div className="text-center py-12 bg-gray-50 rounded-lg">
          <svg
            className="mx-auto h-12 w-12 text-gray-400 mb-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="1"
              d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            {t('testPreviewTab.noQuestionsAvailable', 'No questions available')}
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            {t(
              'testPreviewTab.addQuestionsHint',
              'Make sure you have added questions to this test and configured the difficulty distribution.'
            )}
          </p>
        </div>
      ) : (
        <>
          <QuestionPreview questions={previewQuestions} />

          {!fetchError && (
            <div className="mt-4">
              <SharedPagination
                currentPage={currentPage}
                totalPages={totalPages}
                totalItems={totalItems}
                itemsPerPage={pageSize}
                onPageChange={handlePageChange}
                itemName={t('testPreviewTab.questions', 'questions')}
              />
            </div>
          )}
        </>
      )}
    </InfoCard>
  );
};

export default TestPreviewTab;
