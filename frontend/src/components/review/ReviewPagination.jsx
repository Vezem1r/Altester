import { memo, useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { ReviewNavigationContext } from './ReviewNavigationContext';

const ReviewPagination = memo(
  ({
    currentPage: propCurrentPage,
    totalPages: propTotalPages,
    showSummary: propShowSummary,
    onPrevPage,
    onNextPage,
    onGoToPage,
  }) => {
    const { t } = useTranslation();
    const navigation = useContext(ReviewNavigationContext);

    const handlePrevPage = onPrevPage || navigation?.goToPrevPage;
    const handleNextPage = onNextPage || navigation?.goToNextPage;
    const handleGoToPage = onGoToPage || navigation?.goToPage;
    const currentPage =
      propCurrentPage !== undefined ? propCurrentPage : navigation?.currentPage;
    const totalPages =
      propTotalPages !== undefined ? propTotalPages : navigation?.totalPages;
    const showSummary =
      propShowSummary !== undefined ? propShowSummary : navigation?.showSummary;

    return (
      <div className="flex space-x-2">
        <button
          onClick={handlePrevPage}
          disabled={currentPage === 1 && showSummary}
          className="inline-flex items-center px-3 py-1 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:hover:bg-white"
        >
          <svg
            className="h-4 w-4 mr-1"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
              clipRule="evenodd"
            />
          </svg>
          {t('reviewPagination.previous', 'Previous')}
        </button>

        <div className="flex space-x-1">
          {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
            <button
              key={page}
              onClick={() => handleGoToPage(page)}
              className={`w-8 h-8 rounded-full flex items-center justify-center text-sm ${
                currentPage === page
                  ? 'bg-purple-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {page}
            </button>
          ))}
        </div>

        <button
          onClick={handleNextPage}
          disabled={currentPage === totalPages}
          className="inline-flex items-center px-3 py-1 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:hover:bg-white"
        >
          {t('reviewPagination.next', 'Next')}
          <svg
            className="h-4 w-4 ml-1"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
              clipRule="evenodd"
            />
          </svg>
        </button>
      </div>
    );
  }
);

ReviewPagination.displayName = 'ReviewPagination';

export default ReviewPagination;
