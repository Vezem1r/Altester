import React, { useCallback, useRef, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { formatDate, formatDurationBetween } from '@/utils/formatters';
import { useTranslation } from 'react-i18next';

const StatusBadge = React.memo(({ status }) => {
  const { t } = useTranslation();
  let label, bgColor, textColor;

  switch (status) {
    case 'IN_PROGRESS':
      label = t('testAttemptsModal.statusBadge.inProgress', 'In Progress');
      bgColor = 'bg-blue-100';
      textColor = 'text-blue-800';
      break;
    case 'COMPLETED':
      label = t('testAttemptsModal.statusBadge.completed', 'Completed');
      bgColor = 'bg-green-100';
      textColor = 'text-green-800';
      break;
    case 'REVIEWED':
      label = t('testAttemptsModal.statusBadge.reviewed', 'Reviewed');
      bgColor = 'bg-purple-100';
      textColor = 'text-purple-800';
      break;
    case 'AI_REVIEWED':
      label = t('testAttemptsModal.statusBadge.aiReviewed', 'AI Reviewed');
      bgColor = 'bg-blue-100';
      textColor = 'text-blue-800';
      break;
    default:
      label = status || t('testAttemptsModal.statusBadge.unknown', 'Unknown');
      bgColor = 'bg-gray-100';
      textColor = 'text-gray-800';
  }

  return (
    <span
      className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${bgColor} ${textColor}`}
    >
      {label}
    </span>
  );
});

const AttemptCard = React.memo(({ attempt, totalScore, onViewReview }) => {
  const { t } = useTranslation();
  const scorePercentage =
    attempt.score !== null ? Math.round((attempt.score / totalScore) * 100) : 0;

  const handleViewReview = useCallback(() => {
    onViewReview(attempt.attemptId);
  }, [attempt.attemptId, onViewReview]);

  return (
    <div className="bg-white rounded-lg overflow-hidden border border-gray-200 mb-4 will-change-transform">
      <div className="p-4 border-b border-gray-200 flex flex-wrap justify-between">
        <div>
          <h4 className="font-medium text-gray-900">
            {t('testAttemptsModal.attemptNumber', 'Attempt #{{number}}', {
              number: attempt.attemptNumber,
            })}
          </h4>
          <p className="text-sm text-gray-500">
            {formatDate(attempt.startTime)}
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <StatusBadge status={attempt.status} />

          {attempt.score !== null && (
            <div className="text-sm font-medium">
              <span className="font-semibold">{attempt.score}</span>
              <span className="text-gray-500"> / {totalScore}</span>
              <span className="text-gray-400 text-xs ml-1">
                ({scorePercentage}%)
              </span>
            </div>
          )}
        </div>
      </div>

      <div className="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 mb-2 text-sm">
          <div>
            <span className="text-xs text-gray-500 block">
              {t('testAttemptsModal.completionTime', 'Completion Time')}
            </span>
            <span className="font-medium">
              {formatDurationBetween(attempt.startTime, attempt.endTime)}
            </span>
          </div>

          <div>
            <span className="text-xs text-gray-500 block">
              {t('testAttemptsModal.questions', 'Questions')}
            </span>
            <span className="font-medium">
              {attempt.answeredQuestions} / {attempt.totalQuestions}
            </span>
          </div>

          <div>
            <span className="text-xs text-gray-500 block">
              {t('testAttemptsModal.score', 'Score')}
            </span>
            <span className="font-medium">
              {attempt.score !== null
                ? `${scorePercentage}%`
                : t('testAttemptsModal.notAvailable', 'Not available')}
            </span>
          </div>
        </div>

        {attempt.score !== null && (
          <div className="w-full bg-gray-200 rounded-full h-1 mb-4">
            <div
              className="bg-purple-600 h-1 rounded-full"
              style={{ width: `${scorePercentage}%` }}
            />
          </div>
        )}

        {(attempt.status === 'REVIEWED' ||
          attempt.status === 'AI_REVIEWED') && (
          <div className="mt-2 text-right">
            <button
              onClick={handleViewReview}
              className="inline-flex items-center px-4 py-2 text-white text-sm font-medium rounded-md bg-purple-600 hover:bg-purple-700"
            >
              {t(
                'testAttemptsModal.viewDetailedFeedback',
                'View Detailed Feedback'
              )}
              {attempt.status === 'AI_REVIEWED' && (
                <svg
                  className="ml-2 h-4 w-4"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path d="M6.672 1.911a1 1 0 10-1.932.518l.259.966a1 1 0 001.932-.518l-.26-.966zM2.429 4.74a1 1 0 10-.517 1.932l.966.259a1 1 0 00.517-1.932l-.966-.26zm8.814-.569a1 1 0 00-1.415-1.414l-.707.707a1 1 0 101.415 1.415l.707-.708zm-7.071 7.072l.707-.707A1 1 0 003.465 9.12l-.708.707a1 1 0 001.415 1.415zm3.2-5.171a1 1 0 00-1.3 1.3l4 10a1 1 0 001.823.075l1.38-2.759 3.018 3.02a1 1 0 001.414-1.415l-3.019-3.02 2.76-1.379a1 1 0 00-.076-1.822l-10-4z" />
                </svg>
              )}
            </button>
          </div>
        )}
      </div>
    </div>
  );
});

const TestAttemptsModal = ({
  attempts = [],
  testTitle,
  totalScore,
  onClose,
}) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const scrollContainerRef = useRef(null);
  const [visibleRange, setVisibleRange] = useState({ start: 0, end: 20 });

  const hasAttempts = attempts && attempts.length > 0;

  useEffect(() => {
    if (!scrollContainerRef.current || !hasAttempts) return;

    const handleScroll = () => {
      const { scrollTop, clientHeight, scrollHeight } =
        scrollContainerRef.current;
      const itemHeight = 175;

      const startIndex = Math.max(0, Math.floor(scrollTop / itemHeight) - 5);
      const visibleItems = Math.ceil(clientHeight / itemHeight) + 10;
      const endIndex = Math.min(attempts.length, startIndex + visibleItems);

      setVisibleRange({ start: startIndex, end: endIndex });

      if (scrollTop + clientHeight >= scrollHeight - 300) {
        setVisibleRange(prev => ({
          start: prev.start,
          end: Math.min(attempts.length, prev.end + 5),
        }));
      }
    };

    let ticking = false;
    const scrollListener = () => {
      if (!ticking) {
        window.requestAnimationFrame(() => {
          handleScroll();
          ticking = false;
        });
        ticking = true;
      }
    };

    const scrollContainer = scrollContainerRef.current;
    scrollContainer.addEventListener('scroll', scrollListener);

    handleScroll();

    return () => {
      if (scrollContainer) {
        scrollContainer.removeEventListener('scroll', scrollListener);
      }
    };
  }, [hasAttempts, attempts.length]);

  const handleViewReview = useCallback(
    attemptId => {
      onClose();
      navigate(`/student/attempt-review/${attemptId}`);
    },
    [navigate, onClose]
  );

  const visibleAttempts = hasAttempts
    ? attempts.slice(visibleRange.start, visibleRange.end)
    : [];

  const renderEmptyState = () => (
    <div className="p-8 text-center">
      <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-purple-100 text-purple-600 mb-4">
        <svg
          className="h-8 w-8"
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
      </div>
      <h3 className="text-xl font-bold text-gray-900 mb-2">
        {t('testAttemptsModal.noAttemptsFound', 'No attempts found')}
      </h3>
      <p className="text-gray-600 max-w-md mx-auto">
        {t(
          'testAttemptsModal.noAttemptsDescription',
          "You haven't completed any attempts for this test yet. Start a test to see your results here."
        )}
      </p>
    </div>
  );

  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4 sm:px-0"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden"
        onClick={e => e.stopPropagation()}
        style={{ transform: 'translateZ(0)' }}
      >
        <div className="bg-purple-600 px-6 py-4 sticky top-0 z-10 text-white">
          <div className="flex justify-between items-center">
            <div>
              <h3 className="text-xl font-bold">
                {t('testAttemptsModal.title', 'Test Attempts')}
              </h3>
              <p className="text-sm text-purple-100">{testTitle}</p>
            </div>
            <button
              onClick={onClose}
              className="text-white hover:text-purple-200 focus:outline-none bg-white/20 rounded-full p-2"
            >
              <svg
                className="h-5 w-5"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        </div>

        <div
          ref={scrollContainerRef}
          className="overflow-y-auto overflow-x-hidden"
          style={{
            maxHeight: 'calc(90vh - 120px)',
            scrollbarWidth: 'thin',
            willChange: 'scroll-position',
          }}
        >
          {!hasAttempts ? (
            renderEmptyState()
          ) : (
            <div className="p-4 pb-6">
              {visibleRange.start > 0 && (
                <div style={{ height: `${visibleRange.start * 175}px` }} />
              )}

              {visibleAttempts.map(attempt => (
                <AttemptCard
                  key={attempt.attemptId}
                  attempt={attempt}
                  totalScore={totalScore}
                  onViewReview={handleViewReview}
                />
              ))}

              {visibleRange.end < attempts.length && (
                <div
                  style={{
                    height: `${(attempts.length - visibleRange.end) * 175}px`,
                  }}
                />
              )}
            </div>
          )}
        </div>

        <div className="px-6 py-3 bg-gray-50 border-t border-gray-200 sticky bottom-0 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-white border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            {t('testAttemptsModal.close', 'Close')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default TestAttemptsModal;
