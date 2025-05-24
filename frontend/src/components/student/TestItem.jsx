import { useState } from 'react';
import { formatDate, formatDuration } from '@/utils/formatters';
import { useTranslation } from 'react-i18next';

const TestItem = ({
  test,
  onStartTest,
  onViewAttempts,
  isCurrentSemester = true,
}) => {
  const [expanded, setExpanded] = useState(false);
  const { t } = useTranslation();

  const getTestStatus = () => {
    if (!isCurrentSemester) {
      return {
        label: t('testItem.status.viewOnly', 'View Only'),
        color: 'bg-yellow-100 text-yellow-800',
        gradientFrom: 'from-yellow-50',
        gradientTo: 'to-yellow-100',
        borderColor: 'border-yellow-200',
        iconBg: 'bg-yellow-500',
        icon: (
          <svg
            className="h-4 w-4 text-white"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
            <path
              fillRule="evenodd"
              d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z"
              clipRule="evenodd"
            />
          </svg>
        ),
      };
    }

    const now = new Date();
    const startTime = test.startTime ? new Date(test.startTime) : null;
    const endTime = test.endTime ? new Date(test.endTime) : null;

    if (test.status === 'IN_PROGRESS') {
      return {
        label: t('testItem.status.inProgress', 'Test in progress'),
        color: 'bg-red-100 text-red-800',
        gradientFrom: 'from-red-50',
        gradientTo: 'to-red-100',
        borderColor: 'border-red-200',
        iconBg: 'bg-red-400',
        icon: (
          <svg
            className="h-4 w-4 text-white"
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
        ),
      };
    }

    if (test.status === 'COMPLETED') {
      return {
        label: t('testItem.status.completed', 'Completed'),
        color: 'bg-blue-100 text-blue-800',
        gradientFrom: 'from-blue-50',
        gradientTo: 'to-blue-100',
        borderColor: 'border-blue-200',
        iconBg: 'bg-blue-400',
        icon: (
          <svg
            className="h-4 w-4 text-white"
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
        ),
      };
    }

    if (test.status === 'REVIEWED') {
      return {
        label: t('testItem.status.reviewed', 'Reviewed'),
        color: 'bg-purple-100 text-purple-800',
        gradientFrom: 'from-purple-50',
        gradientTo: 'to-purple-100',
        borderColor: 'border-purple-200',
        iconBg: 'bg-purple-400',
        icon: (
          <svg
            className="h-4 w-4 text-white"
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
        ),
      };
    }

    if (test.status === 'AI_REVIEWED') {
      return {
        label: t('testItem.status.aiReviewed', 'AI Reviewed'),
        color: 'bg-teal-100 text-teal-800',
        gradientFrom: 'from-teal-50',
        gradientTo: 'to-teal-100',
        borderColor: 'border-teal-200',
        iconBg: 'bg-teal-400',
        icon: (
          <svg
            className="h-4 w-4 text-white"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M6.672 1.911a1 1 0 10-1.932.518l.259.966a1 1 0 001.932-.518l-.26-.966zM2.429 4.74a1 1 0 10-.517 1.932l.966.259a1 1 0 00.517-1.932l-.966-.26zm8.814-.569a1 1 0 00-1.415-1.414l-.707.707a1 1 0 101.415 1.415l.707-.708zm-7.071 7.072l.707-.707A1 1 0 003.465 9.12l-.708.707a1 1 0 001.415 1.415zm3.2-5.171a1 1 0 00-1.3 1.3l4 10a1 1 0 001.823.075l1.38-2.759 3.018 3.02a1 1 0 001.414-1.415l-3.019-3.02 2.76-1.379a1 1 0 00-.076-1.822l-10-4z"
              clipRule="evenodd"
            />
          </svg>
        ),
      };
    }

    if (!startTime && !endTime) {
      return {
        label: t('testItem.status.available', 'Available'),
        color: 'bg-green-100 text-green-800',
        gradientFrom: 'from-green-50',
        gradientTo: 'to-green-100',
        borderColor: 'border-green-200',
        iconBg: 'bg-green-500',
        icon: (
          <svg
            className="h-4 w-4 text-white"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z"
              clipRule="evenodd"
            />
          </svg>
        ),
      };
    }

    if (startTime && now < startTime) {
      return {
        label: t('testItem.status.upcoming', 'Upcoming'),
        color: 'bg-orange-100 text-orange-800',
        gradientFrom: 'from-orange-50',
        gradientTo: 'to-orange-100',
        borderColor: 'border-orange-200',
        iconBg: 'bg-orange-500',
        icon: (
          <svg
            className="h-4 w-4 text-white"
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
        ),
      };
    }

    if (endTime && now > endTime) {
      return {
        label: t('testItem.status.expired', 'Expired'),
        color: 'bg-gray-100 text-gray-800',
        gradientFrom: 'from-gray-50',
        gradientTo: 'to-gray-100',
        borderColor: 'border-gray-200',
        iconBg: 'bg-gray-500',
        icon: (
          <svg
            className="h-4 w-4 text-white"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clipRule="evenodd"
            />
          </svg>
        ),
      };
    }

    return {
      label: t('testItem.status.active', 'Active'),
      color: 'bg-indigo-100 text-indigo-800',
      gradientFrom: 'from-indigo-50',
      gradientTo: 'to-indigo-100',
      borderColor: 'border-indigo-200',
      iconBg: 'bg-indigo-500',
      icon: (
        <svg
          className="h-4 w-4 text-white"
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
      ),
    };
  };

  const toggleExpand = () => {
    setExpanded(!expanded);
  };

  const handleStartTest = e => {
    if (e) e.stopPropagation();
    onStartTest(test);
  };

  const handleViewAttempts = e => {
    e.stopPropagation();
    onViewAttempts(test);
  };

  const testStatus = getTestStatus();

  const canTakeTest = () => {
    if (!isCurrentSemester) return false;

    const now = new Date();
    const endTime = test.endTime ? new Date(test.endTime) : null;
    if (endTime && now > endTime) return false;

    const startTime = test.startTime ? new Date(test.startTime) : null;
    if (startTime && now < startTime) return false;

    return test.remainingAttempts > 0 || test.maxAttempts === null;
  };

  const getBestScoreDisplay = () => {
    if (test.bestScore !== null) {
      const percentage = Math.round((test.bestScore / test.totalScore) * 100);
      const getColor = () => {
        if (percentage >= 90) return 'text-green-600';
        if (percentage >= 70) return 'text-emerald-600';
        if (percentage >= 50) return 'text-amber-600';
        return 'text-red-600';
      };

      return (
        <>
          <span className={`font-medium ${getColor()}`}>
            {test.bestScore} / {test.totalScore}
          </span>
          <span className="ml-2 text-xs text-gray-500">({percentage}%)</span>
        </>
      );
    }
    return (
      <span className="text-gray-500 italic">
        {t('testItem.notAttempted', 'Not attempted')}
      </span>
    );
  };

  const getButtonText = () => {
    return test.bestScore !== null
      ? t('testItem.retakeTest', 'Retake Test')
      : t('testItem.startTest', 'Start Test');
  };

  return (
    <div
      className={`bg-gradient-to-r ${testStatus.gradientFrom} ${testStatus.gradientTo} rounded-xl mb-4 shadow-sm hover:shadow-md transition-all duration-300 cursor-pointer border ${testStatus.borderColor} overflow-hidden group`}
    >
      <div
        className="px-5 py-4 flex items-center justify-between border-b border-gray-100"
        onClick={toggleExpand}
      >
        <div className="flex items-center space-x-3">
          <div
            className={`flex-shrink-0 w-8 h-8 ${testStatus.iconBg} rounded-full flex items-center justify-center shadow-sm`}
          >
            {testStatus.icon}
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-800 group-hover:text-purple-800 transition-colors">
              {test.title}
            </h3>
            <div className="flex items-center mt-1 space-x-4">
              <div className="flex items-center text-xs text-gray-500">
                <svg
                  className="h-3 w-3 mr-1 text-gray-400"
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
                {formatDuration(test.duration)}
              </div>
              <div className="flex items-center text-xs text-gray-500">
                <svg
                  className="h-3 w-3 mr-1 text-gray-400"
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
                {t('testItem.questionCount', '{{count}} questions', {
                  count: test.numberOfQuestions,
                })}
              </div>
              <div className="flex items-center text-xs text-gray-500">
                <svg
                  className="h-3 w-3 mr-1 text-gray-400"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path d="M18 3a1 1 0 00-1.447-.894L8.763 6H5a3 3 0 000 6h.28l1.771 5.316A1 1 0 008 18h1a1 1 0 001-1v-4.382l6.553 3.276A1 1 0 0018 15V3z" />
                </svg>
                {t('testItem.points', '{{points}} pts', {
                  points: test.totalScore,
                })}
              </div>
            </div>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <span
            className={`hidden sm:inline-flex px-3 py-1 text-xs font-medium rounded-full ${testStatus.color} shadow-sm`}
          >
            {testStatus.label}
          </span>

          <div className="bg-white p-2 rounded-full shadow-sm group-hover:bg-purple-100 transition-colors">
            <svg
              className={`h-4 w-4 text-gray-500 group-hover:text-purple-700 transform transition-transform duration-300 ${expanded ? 'rotate-180' : ''}`}
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
      </div>

      {expanded && (
        <div className="bg-white p-5 transition-all duration-300 ease-in-out">
          <div className="flex flex-wrap -mx-2">
            <div className="px-2 w-full md:w-1/2 mb-4">
              <div className="bg-gray-50 rounded-lg p-4 h-full border border-gray-100">
                <h4 className="font-medium text-gray-700 mb-3 flex items-center text-sm">
                  <svg
                    className="h-4 w-4 mr-2 text-purple-500"
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
                  {t('testItem.testOverview', 'Test Overview')}
                </h4>

                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-xs text-gray-500">
                      {t('testItem.duration', 'Duration:')}
                    </span>
                    <span className="text-sm font-medium text-gray-800">
                      {formatDuration(test.duration)}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-xs text-gray-500">
                      {t('testItem.questions', 'Questions:')}
                    </span>
                    <span className="text-sm font-medium text-gray-800">
                      {test.numberOfQuestions}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-xs text-gray-500">
                      {t('testItem.totalPoints', 'Total Points:')}
                    </span>
                    <span className="text-sm font-medium text-gray-800">
                      {test.totalScore}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-xs text-gray-500">
                      {t('testItem.attempts', 'Attempts:')}
                    </span>
                    <span className="text-sm font-medium text-gray-800">
                      {test.maxAttempts
                        ? t(
                            'testItem.attemptsCount',
                            '{{remaining}} / {{max}}',
                            {
                              remaining: test.remainingAttempts,
                              max: test.maxAttempts,
                            }
                          )
                        : t('testItem.attemptsUnlimited', 'Unlimited')}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className="px-2 w-full md:w-1/2 mb-4">
              <div className="bg-gray-50 rounded-lg p-4 h-full border border-gray-100">
                <h4 className="font-medium text-gray-700 mb-3 flex items-center text-sm">
                  <svg
                    className="h-4 w-4 mr-2 text-purple-500"
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
                  {t('testItem.schedule', 'Schedule')}
                </h4>

                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-xs text-gray-500">
                      {t('testItem.availableFrom', 'Available From:')}
                    </span>
                    <span className="text-sm font-medium text-gray-800">
                      {formatDate(test.startTime)}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-xs text-gray-500">
                      {t('testItem.availableUntil', 'Available Until:')}
                    </span>
                    <span className="text-sm font-medium text-gray-800">
                      {formatDate(test.endTime)}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-xs text-gray-500">
                      {t('testItem.status', 'Status:')}
                    </span>
                    <span
                      className={`text-sm font-medium px-2 py-0.5 rounded-full ${testStatus.color} text-xs`}
                    >
                      {testStatus.label}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-xs text-gray-500">
                      {t('testItem.bestScore', 'Best Score:')}
                    </span>
                    <div className="text-sm">{getBestScoreDisplay()}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {test.bestScore !== null && (
            <div className="mb-4">
              <div className="flex justify-between items-center mb-1">
                <span className="text-xs font-medium text-gray-500">
                  {t('testItem.yourScore', 'Your score')}
                </span>
                <span className="text-xs font-medium text-gray-700">
                  {Math.round((test.bestScore / test.totalScore) * 100)}%
                </span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-1.5 mb-4 overflow-hidden">
                <div
                  className="bg-gradient-to-r from-purple-500 to-indigo-600 h-1.5 rounded-full"
                  style={{
                    width: `${Math.round((test.bestScore / test.totalScore) * 100)}%`,
                  }}
                />
              </div>
            </div>
          )}

          <div className="flex justify-end space-x-3 mt-4">
            <button
              className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors"
              onClick={handleViewAttempts}
            >
              <svg
                className="mr-2 h-4 w-4 text-gray-500"
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
              {t('testItem.viewAttempts', 'View Attempts')}
            </button>

            {canTakeTest() && (
              <button
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg shadow-sm text-white bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors"
                onClick={handleStartTest}
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
                {getButtonText()}
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default TestItem;
