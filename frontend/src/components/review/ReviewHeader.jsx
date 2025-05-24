import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import ReviewPagination from '@/components/review/ReviewPagination';

const ReviewHeader = memo(
  ({
    testTitle,
    score,
    aiScore,
    totalScore,
    showSummary,
    currentPage,
    totalPages,
    onToggleSummary,
    onBackToDashboard,
    questions,
  }) => {
    const { t } = useTranslation();

    return (
      <div className="bg-white shadow-md rounded-lg p-4 sticky top-16 z-40 border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid grid-cols-3 items-center">
            <div className="col-span-1">
              <h1 className="text-lg font-bold text-gray-900">{testTitle}</h1>
            </div>

            <div className="col-span-1 flex justify-center items-center space-x-3">
              {aiScore !== undefined && (
                <span className="px-3 py-1 text-sm font-medium rounded-full bg-green-100 text-green-800">
                  {t('reviewHeader.ai', 'AI')}: {aiScore}/{totalScore}
                </span>
              )}

              <span className="px-3 py-1 text-sm font-medium rounded-full bg-purple-100 text-purple-800">
                {t('reviewHeader.teacher', 'Teacher')}: {score}/{totalScore}
              </span>
            </div>

            <div className="col-span-1 flex justify-end items-center gap-2">
              <button
                onClick={onToggleSummary}
                className="inline-flex items-center px-3 py-1.5 border border-transparent text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
              >
                {showSummary
                  ? t('reviewHeader.viewQuestions', 'View Questions')
                  : t('reviewHeader.viewSummary', 'View Summary')}
              </button>
              <button
                onClick={onBackToDashboard}
                className="inline-flex items-center px-3 py-1.5 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
              >
                {t('reviewHeader.dashboard', 'Dashboard')}
              </button>
            </div>
          </div>

          {!showSummary && questions.length > 0 && (
            <div className="flex flex-wrap items-center justify-between mt-4 pt-3 border-t border-gray-200">
              <div className="flex items-center">
                <span className="text-sm text-gray-600">
                  {t(
                    'reviewHeader.questionOf',
                    'Question {{current}} of {{total}}',
                    { current: currentPage, total: totalPages }
                  )}
                </span>
              </div>

              <ReviewPagination
                currentPage={currentPage}
                totalPages={totalPages}
                showSummary={showSummary}
              />
            </div>
          )}
        </div>
      </div>
    );
  }
);

ReviewHeader.displayName = 'ReviewHeader';

export default ReviewHeader;
