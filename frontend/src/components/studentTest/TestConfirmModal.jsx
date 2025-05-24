import { formatDate, formatDuration } from '@/utils/formatters';
import { useTranslation } from 'react-i18next';

const TestConfirmModal = ({ test, onConfirm, onCancel }) => {
  const { t } = useTranslation();

  if (!test) return null;

  return (
    <div className="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center z-50 px-4 sm:px-0 backdrop-blur-sm">
      <div className="bg-white rounded-xl shadow-2xl max-w-md w-full overflow-hidden transform transition-all animate-fadeIn">
        {/* Header */}
        <div className="bg-gradient-to-r from-purple-600 to-indigo-600 px-6 py-5 text-white relative">
          <div className="absolute top-0 right-0 -mt-6 -mr-6 w-24 h-24 bg-purple-200 rounded-full opacity-20" />
          <div className="absolute bottom-0 left-0 -mb-6 -ml-6 w-16 h-16 bg-indigo-200 rounded-full opacity-20" />

          <h3 className="text-xl font-bold relative z-10 flex items-center">
            <svg
              className="h-6 w-6 mr-2"
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
            {t('testConfirmModal.startTest', 'Start Test')}
          </h3>
          <p className="text-purple-100 text-sm mt-1 relative z-10">
            {t(
              'testConfirmModal.pleaseConfirm',
              'Please confirm to begin your assessment'
            )}
          </p>
        </div>

        {/* Test details */}
        <div className="p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-start">
            <span className="bg-purple-100 text-purple-700 rounded-full w-8 h-8 flex items-center justify-center mr-2 flex-shrink-0">
              <svg
                className="h-4 w-4"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z"
                  clipRule="evenodd"
                />
              </svg>
            </span>
            {test.title}
          </h2>

          {test.description && (
            <p className="text-gray-600 mb-6 pl-10">{test.description}</p>
          )}

          <div className="bg-gray-50 rounded-lg p-4 mb-6 shadow-inner">
            <div className="grid grid-cols-2 gap-4">
              <div className="flex items-center space-x-3">
                <div className="text-purple-500">
                  <svg
                    className="h-5 w-5"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path
                      fillRule="evenodd"
                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
                      clipRule="evenodd"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500">
                    {t('testConfirmModal.duration', 'Duration')}
                  </p>
                  <p className="text-sm font-semibold text-gray-800">
                    {formatDuration(test.duration)}
                  </p>
                </div>
              </div>

              <div className="flex items-center space-x-3">
                <div className="text-purple-500">
                  <svg
                    className="h-5 w-5"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path d="M18 3a1 1 0 00-1.447-.894L8.763 6H5a3 3 0 000 6h.28l1.771 5.316A1 1 0 008 18h1a1 1 0 001-1v-4.382l6.553 3.276A1 1 0 0018 15V3z" />
                  </svg>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500">
                    {t('testConfirmModal.points', 'Points')}
                  </p>
                  <p className="text-sm font-semibold text-gray-800">
                    {t('testConfirmModal.pointsValue', '{{points}} points', {
                      points: test.totalScore,
                    })}
                  </p>
                </div>
              </div>

              <div className="flex items-center space-x-3">
                <div className="text-purple-500">
                  <svg
                    className="h-5 w-5"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z" />
                    <path
                      fillRule="evenodd"
                      d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z"
                      clipRule="evenodd"
                    />
                  </svg>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500">
                    {t('testConfirmModal.questions', 'Questions')}
                  </p>
                  <p className="text-sm font-semibold text-gray-800">
                    {test.numberOfQuestions}
                  </p>
                </div>
              </div>

              {test.maxAttempts && (
                <div className="flex items-center space-x-3">
                  <div className="text-purple-500">
                    <svg
                      className="h-5 w-5"
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                    >
                      <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-xs font-medium text-gray-500">
                      {t('testConfirmModal.attempts', 'Attempts')}
                    </p>
                    <p className="text-sm font-semibold text-gray-800">
                      {t(
                        'testConfirmModal.attemptsValue',
                        '{{remaining}} of {{max}}',
                        {
                          remaining: test.remainingAttempts,
                          max: test.maxAttempts,
                        }
                      )}
                    </p>
                  </div>
                </div>
              )}
            </div>

            {(test.startTime || test.endTime) && (
              <div className="mt-4 pt-4 border-t border-gray-200">
                <div className="grid grid-cols-2 gap-4">
                  {test.startTime && (
                    <div className="flex items-center space-x-3">
                      <div className="text-purple-500">
                        <svg
                          className="h-5 w-5"
                          xmlns="http://www.w3.org/2000/svg"
                          viewBox="0 0 20 20"
                          fill="currentColor"
                        >
                          <path
                            fillRule="evenodd"
                            d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </div>
                      <div>
                        <p className="text-xs font-medium text-gray-500">
                          {t('testConfirmModal.from', 'From')}
                        </p>
                        <p className="text-sm font-semibold text-gray-800">
                          {formatDate(test.startTime)}
                        </p>
                      </div>
                    </div>
                  )}

                  {test.endTime && (
                    <div className="flex items-center space-x-3">
                      <div className="text-purple-500">
                        <svg
                          className="h-5 w-5"
                          xmlns="http://www.w3.org/2000/svg"
                          viewBox="0 0 20 20"
                          fill="currentColor"
                        >
                          <path
                            fillRule="evenodd"
                            d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </div>
                      <div>
                        <p className="text-xs font-medium text-gray-500">
                          {t('testConfirmModal.until', 'Until')}
                        </p>
                        <p className="text-sm font-semibold text-gray-800">
                          {formatDate(test.endTime)}
                        </p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Alert area */}
          <div className="mb-6 bg-amber-50 rounded-lg p-4 border-l-4 border-amber-400">
            <div className="flex">
              <div className="flex-shrink-0 text-amber-500">
                <svg
                  className="h-5 w-5"
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
                <h3 className="text-sm font-medium text-amber-800">
                  {t(
                    'testConfirmModal.importantInformation',
                    'Important information'
                  )}
                </h3>
                <div className="mt-2 text-sm text-amber-700">
                  <ul className="list-disc pl-5 space-y-1">
                    <li>
                      {t(
                        'testConfirmModal.timerCannotBePaused',
                        'Once started, the test timer cannot be paused'
                      )}
                    </li>
                    <li>
                      {t(
                        'testConfirmModal.canReconnect',
                        'If you close the browser, you can reconnect to continue'
                      )}
                    </li>
                    <li>
                      {t(
                        'testConfirmModal.submitBeforeExpiry',
                        'Submit your test before the time expires'
                      )}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          {/* Action buttons */}
          <div className="flex justify-end space-x-3">
            <button
              type="button"
              className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors"
              onClick={onCancel}
            >
              {t('testConfirmModal.cancel', 'Cancel')}
            </button>
            <button
              type="button"
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors"
              onClick={onConfirm}
            >
              <svg
                className="mr-2 h-4 w-4"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
                  clipRule="evenodd"
                />
              </svg>
              {t('testConfirmModal.startTest', 'Start Test')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TestConfirmModal;
