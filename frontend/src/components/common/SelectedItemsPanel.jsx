import { motion } from 'framer-motion';
import { useTranslation } from 'react-i18next';

const SelectedItemsPanel = ({
  items,
  selectedIds,
  label,
  onRemove,
  onClearAll,
  onToggleVisibility,
  isVisible,
  renderItem,
  itemKey = 'id',
  itemLabel = 'name',
}) => {
  const { t } = useTranslation();

  if (!isVisible || selectedIds.length === 0) return null;

  return (
    <motion.div
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      className="p-4 bg-gradient-to-br from-purple-50 to-indigo-50 border border-purple-200 rounded-xl shadow-sm"
    >
      <div className="flex items-center justify-between mb-3">
        <h4 className="text-sm font-semibold text-purple-900">
          {t('selectedItemsPanel.selected', 'Selected')} {label} (
          {selectedIds.length})
        </h4>
        <div className="flex items-center gap-2">
          {onToggleVisibility && (
            <button
              type="button"
              onClick={onToggleVisibility}
              className="p-1 text-purple-600 hover:text-purple-700 transition-colors"
              aria-label={t(
                'selectedItemsPanel.hidePanel',
                'Hide selected panel'
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
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          )}
          <button
            type="button"
            onClick={onClearAll}
            className="px-3 py-1 bg-red-100 text-red-700 hover:bg-red-200 rounded-lg text-xs font-medium transition-colors"
          >
            {t('selectedItemsPanel.clearAll', 'Clear all')}
          </button>
        </div>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2 max-h-48 overflow-y-auto">
        {items
          .filter(item => selectedIds.includes(item[itemKey]))
          .map(item => (
            <motion.div
              key={item[itemKey]}
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              className="flex items-center justify-between bg-white px-3 py-2 rounded-lg border border-purple-200 shadow-sm"
            >
              {renderItem ? (
                renderItem(item)
              ) : (
                <div className="flex items-center">
                  <div className="w-8 h-8 rounded-full bg-gradient-to-br from-purple-500 to-indigo-600 text-white flex items-center justify-center text-xs font-bold mr-2">
                    {item[itemLabel]?.[0] || '?'}
                  </div>
                  <div className="text-sm">
                    <p className="font-medium text-gray-800 truncate max-w-[120px]">
                      {item[itemLabel]}
                    </p>
                    {item.username && (
                      <p className="text-xs text-gray-500">@{item.username}</p>
                    )}
                  </div>
                </div>
              )}
              <button
                type="button"
                onClick={() => onRemove(item)}
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
  );
};

export default SelectedItemsPanel;
