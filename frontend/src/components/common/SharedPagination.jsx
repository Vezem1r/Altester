import PropTypes from 'prop-types';
import { useTranslation } from 'react-i18next';

const SharedPagination = ({
  currentPage,
  totalPages,
  totalItems,
  itemsPerPage = 10,
  onPageChange,
  itemName = 'items',
  showItemCounts = true,
  maxPageButtons = 5,
}) => {
  const { t } = useTranslation();

  const handlePreviousPage = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      onPageChange(currentPage + 1);
    }
  };

  const firstItemIndex = totalItems === 0 ? 0 : currentPage * itemsPerPage + 1;
  const lastItemIndex = Math.min((currentPage + 1) * itemsPerPage, totalItems);

  if (totalPages === 0) {
    return null;
  }

  const getPageButtons = () => {
    if (totalPages <= maxPageButtons) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    const halfMaxButtons = Math.floor(maxPageButtons / 2);
    let startPage = Math.max(0, currentPage - halfMaxButtons);
    let endPage = Math.min(totalPages - 1, currentPage + halfMaxButtons);

    if (currentPage < halfMaxButtons) {
      endPage = Math.min(maxPageButtons - 1, totalPages - 1);
    } else if (currentPage > totalPages - halfMaxButtons - 1) {
      startPage = Math.max(0, totalPages - maxPageButtons);
    }

    return Array.from(
      { length: endPage - startPage + 1 },
      (_, i) => startPage + i
    );
  };

  const pageButtons = getPageButtons();

  return (
    <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
      {/* Mobile pagination */}
      <div className="flex-1 flex justify-between sm:hidden">
        <button
          onClick={handlePreviousPage}
          disabled={currentPage === 0}
          className={`relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md ${
            currentPage === 0
              ? 'text-gray-300 bg-gray-50 cursor-not-allowed'
              : 'text-gray-700 bg-white hover:bg-gray-50'
          }`}
        >
          {t('sharedPagination.previous', 'Previous')}
        </button>
        <span className="text-sm text-gray-700 px-4 py-2">
          {t(
            'sharedPagination.pageXofY',
            'Page {{currentPage}} of {{totalPages}}',
            { currentPage: currentPage + 1, totalPages }
          )}
        </span>
        <button
          onClick={handleNextPage}
          disabled={currentPage >= totalPages - 1}
          className={`ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md ${
            currentPage >= totalPages - 1
              ? 'text-gray-300 bg-gray-50 cursor-not-allowed'
              : 'text-gray-700 bg-white hover:bg-gray-50'
          }`}
        >
          {t('sharedPagination.next', 'Next')}
        </button>
      </div>

      {/* Desktop pagination */}
      <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
        {showItemCounts && (
          <div>
            <p className="text-sm text-gray-700">
              {t(
                'sharedPagination.showingXtoYofZ',
                'Showing {{first}} to {{last}} of {{total}} {{itemName}}',
                {
                  first: firstItemIndex,
                  last: lastItemIndex,
                  total: totalItems,
                  itemName,
                }
              )}
            </p>
          </div>
        )}
        <div>
          <nav
            className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px"
            aria-label="Pagination"
          >
            {/* Previous button */}
            <button
              onClick={handlePreviousPage}
              disabled={currentPage === 0}
              className={`relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium ${
                currentPage === 0
                  ? 'text-gray-300 cursor-not-allowed'
                  : 'text-gray-500 hover:bg-gray-50'
              }`}
            >
              <span className="sr-only">
                {t('sharedPagination.previous', 'Previous')}
              </span>
              <svg
                className="h-5 w-5"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
                aria-hidden="true"
              >
                <path
                  fillRule="evenodd"
                  d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                  clipRule="evenodd"
                />
              </svg>
            </button>

            {/* First page button (if not in range) */}
            {pageButtons[0] > 0 && (
              <>
                <button
                  onClick={() => onPageChange(0)}
                  className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                >
                  1
                </button>
                {pageButtons[0] > 1 && (
                  <span className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                    ...
                  </span>
                )}
              </>
            )}

            {/* Page buttons */}
            {pageButtons.map(pageIndex => (
              <button
                key={pageIndex}
                onClick={() => onPageChange(pageIndex)}
                className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                  currentPage === pageIndex
                    ? 'z-10 bg-purple-50 border-purple-500 text-purple-600'
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                }`}
              >
                {pageIndex + 1}
              </button>
            ))}

            {/* Last page button (if not in range) */}
            {pageButtons[pageButtons.length - 1] < totalPages - 1 && (
              <>
                {pageButtons[pageButtons.length - 1] < totalPages - 2 && (
                  <span className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                    ...
                  </span>
                )}
                <button
                  onClick={() => onPageChange(totalPages - 1)}
                  className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                >
                  {totalPages}
                </button>
              </>
            )}

            {/* Next button */}
            <button
              onClick={handleNextPage}
              disabled={currentPage >= totalPages - 1}
              className={`relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium ${
                currentPage >= totalPages - 1
                  ? 'text-gray-300 cursor-not-allowed'
                  : 'text-gray-500 hover:bg-gray-50'
              }`}
            >
              <span className="sr-only">
                {t('sharedPagination.next', 'Next')}
              </span>
              <svg
                className="h-5 w-5"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
                aria-hidden="true"
              >
                <path
                  fillRule="evenodd"
                  d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                  clipRule="evenodd"
                />
              </svg>
            </button>
          </nav>
        </div>
      </div>
    </div>
  );
};

SharedPagination.propTypes = {
  currentPage: PropTypes.number.isRequired,
  totalPages: PropTypes.number.isRequired,
  totalItems: PropTypes.number.isRequired,
  itemsPerPage: PropTypes.number,
  onPageChange: PropTypes.func.isRequired,
  itemName: PropTypes.string,
  showItemCounts: PropTypes.bool,
  maxPageButtons: PropTypes.number,
};

export default SharedPagination;
