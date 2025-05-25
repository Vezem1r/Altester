import { useEffect, useState, useRef, memo } from 'react';
import { useTranslation } from 'react-i18next';

const SearchBar = memo(
  ({ searchQuery, setSearchQuery, handleSearch, loading }) => {
    const { t } = useTranslation();
    const [localQuery, setLocalQuery] = useState(searchQuery);
    const isInitialMount = useRef(true);
    const inputRef = useRef(null);
    const searchTimeoutRef = useRef(null);
    const userInteractedRef = useRef(false);

    const handleInputChange = e => {
      const newQuery = e.target.value;
      setLocalQuery(newQuery);
      setSearchQuery(newQuery);
      userInteractedRef.current = true;
    };

    const handleSubmit = e => {
      e.preventDefault();

      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
        searchTimeoutRef.current = null;
      }

      const syntheticEvent = { preventDefault: () => {} };
      handleSearch(syntheticEvent);
    };

    const handleClearSearch = () => {
      setLocalQuery('');
      setSearchQuery('');
      userInteractedRef.current = true;

      if (inputRef.current) {
        inputRef.current.focus();
      }

      const syntheticEvent = { preventDefault: () => {}, forceClear: true };
      handleSearch(syntheticEvent);
    };

    useEffect(() => {
      setLocalQuery(searchQuery);
    }, [searchQuery]);

    useEffect(() => {
      if (isInitialMount.current) {
        isInitialMount.current = false;
        return;
      }

      if (!userInteractedRef.current) {
        return;
      }

      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }

      searchTimeoutRef.current = setTimeout(() => {
        const inputHasFocus = document.activeElement === inputRef.current;

        try {
          const syntheticEvent = { preventDefault: () => {} };
          const result = handleSearch(syntheticEvent);

          if (result && typeof result.finally === 'function') {
            result.finally(() => {
              if (inputHasFocus && inputRef.current) {
                inputRef.current.focus();
              }
            });
          }
        } catch {}
      }, 500);

      return () => {
        if (searchTimeoutRef.current) {
          clearTimeout(searchTimeoutRef.current);
        }
      };
    }, [localQuery, handleSearch]);

    return (
      <div className="max-w-lg w-full mx-auto">
        <form onSubmit={handleSubmit} className="relative">
          <div className="relative">
            <input
              ref={inputRef}
              type="text"
              name="search"
              id="search"
              placeholder={t(
                'studentPageSearchBar.placeholder',
                'Search tests by title...'
              )}
              value={localQuery}
              onChange={handleInputChange}
              className="block w-full pl-4 pr-16 py-3 border-gray-300 rounded-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent shadow-sm"
              aria-label={t('studentPageSearchBar.ariaLabel', 'Search tests')}
            />
            <div className="absolute inset-y-0 right-0 pr-3 flex items-center space-x-1">
              {localQuery && (
                <button
                  type="button"
                  onClick={handleClearSearch}
                  className="text-gray-400 hover:text-gray-600 focus:outline-none mr-1"
                  aria-label={t(
                    'studentPageSearchBar.clearSearch',
                    'Clear search'
                  )}
                >
                  <svg
                    className="h-5 w-5"
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
                </button>
              )}
              {loading ? (
                <svg
                  className="animate-spin h-5 w-5 text-gray-400"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
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
              ) : (
                <svg
                  className="h-5 w-5 text-gray-400"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
                    clipRule="evenodd"
                  />
                </svg>
              )}
            </div>
          </div>
        </form>
      </div>
    );
  }
);

SearchBar.displayName = 'SearchBar';

export default SearchBar;
