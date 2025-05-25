import React, { memo, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import SearchBar from '@/components/search/SearchBar';
import SharedPagination from '@/components/common/SharedPagination';
import { useTranslation } from 'react-i18next';

const SearchSelect = memo(
  ({
    label,
    placeholder,
    items,
    selectedItem,
    onSelect,
    onSearchChange,
    searchQuery,
    loading = false,
    error,
    renderItem,
    valueKey = 'id',
    labelKey = 'name',
    required = false,
    className = '',
    currentPage = 0,
    totalPages = 1,
    totalItems = 0,
    onPageChange,
    multiple = false,
    selectedItems = [],
    selectedIds = [],
    itemsPerPage = 12,
    onCloseSelected,
    showSelectedPanel = true,
    preservedSelectedItems = [],
  }) => {
    const { t } = useTranslation();
    const prevSearchQuery = useRef(searchQuery);

    useEffect(() => {
      if (prevSearchQuery.current !== searchQuery) {
        prevSearchQuery.current = searchQuery;
        if (onPageChange && currentPage !== 0) {
          onPageChange(0);
        }
      }
    }, [searchQuery, currentPage, onPageChange]);

    const isSelected = item => {
      if (multiple) {
        if (selectedIds && selectedIds.length > 0) {
          return selectedIds.includes(item[valueKey]);
        }
        return selectedItems.some(
          selected => selected[valueKey] === item[valueKey]
        );
      }
      return selectedItem && selectedItem[valueKey] === item[valueKey];
    };

    const allSelectedItems = React.useMemo(() => {
      if (!multiple) return [];
      return preservedSelectedItems;
    }, [preservedSelectedItems, multiple]);

    const getGridClass = () => {
      if (itemsPerPage === 20) {
        return 'grid grid-cols-1 lg:grid-cols-2 gap-4';
      } else if (itemsPerPage === 10) {
        return 'grid grid-cols-1 lg:grid-cols-1 gap-4';
      }
      return 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4';
    };

    const getItemHeight = () => {
      if (itemsPerPage === 20) return 'h-28';
      if (itemsPerPage === 10) return 'h-32';
      return 'h-32';
    };

    return (
      <div className={`space-y-4 ${className}`}>
        {label && (
          <label className="block text-sm font-semibold text-gray-800">
            {label}
            {required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}

        <SearchBar
          value={searchQuery}
          onChange={e => onSearchChange(e.target.value)}
          placeholder={placeholder}
        />

        {multiple && selectedIds.length > 0 && showSelectedPanel && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            className="p-4 bg-gradient-to-br from-purple-50 to-indigo-50 border border-purple-200 rounded-xl shadow-sm"
          >
            <div className="flex items-center justify-between mb-3">
              <h4 className="text-sm font-semibold text-purple-900">
                {t('searchSelect.selected', 'Selected')} {label} (
                {selectedIds.length})
              </h4>
              <div className="flex items-center gap-2">
                {onCloseSelected && (
                  <button
                    type="button"
                    onClick={onCloseSelected}
                    className="p-1 text-purple-600 hover:text-purple-700 transition-colors"
                    aria-label={t(
                      'searchSelect.closeSelectedPanel',
                      'Close selected panel'
                    )}
                  >
                    <svg
                      className="w-5 h-5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 9l-7 7-7-7"
                      />
                    </svg>
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => {
                    selectedIds.forEach(id => {
                      const item = allSelectedItems.find(
                        item => item[valueKey] === id
                      );
                      if (item) onSelect(item);
                    });
                  }}
                  className="px-3 py-1 bg-red-100 text-red-700 hover:bg-red-200 rounded-lg text-xs font-medium transition-colors"
                >
                  {t('searchSelect.clearAll', 'Clear all')}
                </button>
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2 max-h-48 overflow-y-auto">
              {allSelectedItems.map(item => (
                <motion.div
                  key={item[valueKey]}
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  className="flex items-center justify-between bg-white px-3 py-2 rounded-lg border border-purple-200 shadow-sm"
                >
                  <div className="flex items-center">
                    <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-500 to-indigo-600 text-white flex items-center justify-center text-xs font-bold mr-2">
                      {item[labelKey]?.[0] || '?'}
                    </div>
                    <div className="text-sm">
                      <p className="font-medium text-gray-800 truncate max-w-[120px]">
                        {item[labelKey]}
                      </p>
                      {item.username && (
                        <p className="text-xs text-gray-500">
                          @{item.username}
                        </p>
                      )}
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => onSelect(item)}
                    className="text-red-500 hover:bg-red-50 p-1 rounded transition-colors"
                  >
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M6 18L18 6M6 6l12 12"
                      />
                    </svg>
                  </button>
                </motion.div>
              ))}
            </div>
          </motion.div>
        )}

        <AnimatePresence mode="wait">
          {loading ? (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex justify-center items-center py-12"
            >
              <div className="relative">
                <div className="animate-spin rounded-full h-12 w-12 border-4 border-purple-600 border-t-transparent" />
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="h-4 w-4 bg-purple-600 rounded-full animate-pulse" />
                </div>
              </div>
            </motion.div>
          ) : (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
            >
              {items.length === 0 ? (
                <div className="text-center py-12 bg-gray-50 rounded-xl">
                  <svg
                    className="mx-auto h-12 w-12 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                    />
                  </svg>
                  <p className="mt-2 text-sm text-gray-500">
                    {searchQuery
                      ? t(
                          'searchSelect.noResultsFor',
                          'No results for "{{query}}"',
                          { query: searchQuery }
                        )
                      : t(
                          'searchSelect.noItemsAvailable',
                          'No items available'
                        )}
                  </p>
                </div>
              ) : (
                <motion.div
                  className={getGridClass()}
                  key={currentPage}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  transition={{ duration: 0.3, ease: 'easeOut' }}
                >
                  {items.map((item, index) => (
                    <motion.div
                      key={item[valueKey]}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.2, delay: index * 0.03 }}
                      whileHover={{ y: -2, scale: 1.01 }}
                      whileTap={{ scale: 0.99 }}
                      onClick={() => onSelect(item)}
                      className={`
                      relative overflow-hidden rounded-xl cursor-pointer transition-all duration-300 shadow-md
                      ${
                        isSelected(item)
                          ? 'bg-white border-2 border-purple-500'
                          : 'bg-white hover:shadow-lg border border-gray-200 hover:border-purple-300'
                      }
                      ${getItemHeight()}
                    `}
                    >
                      <div className="p-4 h-full flex flex-col preserve-3d">
                        {renderItem ? (
                          renderItem(item, isSelected(item))
                        ) : (
                          <div className="flex flex-col h-full">
                            <p className="font-semibold text-base text-gray-900">
                              {item[labelKey]}
                            </p>
                          </div>
                        )}
                      </div>
                      {isSelected(item) && (
                        <motion.div
                          initial={{ scale: 0, opacity: 0 }}
                          animate={{ scale: 1, opacity: 1 }}
                          className="absolute top-3 right-3"
                        >
                          <div className="bg-purple-500 rounded-full p-1.5 shadow-lg">
                            <svg
                              className="h-5 w-5 text-white"
                              fill="currentColor"
                              viewBox="0 0 20 20"
                            >
                              <path
                                fillRule="evenodd"
                                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                clipRule="evenodd"
                              />
                            </svg>
                          </div>
                        </motion.div>
                      )}
                    </motion.div>
                  ))}
                </motion.div>
              )}
            </motion.div>
          )}
        </AnimatePresence>

        {totalPages > 1 && (
          <div className="mt-6">
            <SharedPagination
              currentPage={currentPage}
              totalPages={totalPages}
              totalItems={totalItems}
              onPageChange={onPageChange}
              itemName={label?.toLowerCase() || 'items'}
              itemsPerPage={itemsPerPage}
            />
          </div>
        )}

        {error && (
          <motion.p
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-sm text-red-600 mt-2"
          >
            {error}
          </motion.p>
        )}
      </div>
    );
  }
);

SearchSelect.displayName = 'SearchSelect';

export default SearchSelect;
