import { motion } from 'framer-motion';
import { useTranslation } from 'react-i18next';

const ActionButtons = ({
  primaryAction,
  secondaryAction,
  cancelAction,
  loading = false,
  align = 'right',
  compact = false,
}) => {
  const { t } = useTranslation();

  const alignmentClasses = {
    left: 'justify-start',
    center: 'justify-center',
    right: 'justify-end',
    between: 'justify-between',
  };

  const buttonClass = compact ? 'px-4 py-2' : 'px-6 py-2.5';

  return (
    <div
      className={`flex items-center gap-3 ${compact ? '' : 'mt-6'} ${alignmentClasses[align]}`}
    >
      {cancelAction && (
        <motion.button
          type="button"
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={cancelAction.onClick}
          disabled={loading || cancelAction.disabled}
          className={`${buttonClass} border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed`}
        >
          {cancelAction.label || t('actionButtons.cancel', 'Cancel')}
        </motion.button>
      )}

      {secondaryAction && (
        <motion.button
          type="button"
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={secondaryAction.onClick}
          disabled={loading || secondaryAction.disabled}
          className={`${buttonClass} border border-purple-300 text-purple-700 rounded-lg hover:bg-purple-50 font-medium transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed`}
        >
          {secondaryAction.label}
        </motion.button>
      )}

      {primaryAction && (
        <motion.button
          type={primaryAction.type || 'button'}
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={primaryAction.onClick}
          disabled={loading || primaryAction.disabled}
          className={`${buttonClass} bg-purple-600 text-white rounded-lg hover:bg-purple-700 font-medium transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center`}
        >
          {loading ? (
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
              {primaryAction.loadingLabel ||
                t('actionButtons.processing', 'Processing...')}
            </>
          ) : (
            primaryAction.label
          )}
        </motion.button>
      )}
    </div>
  );
};

export default ActionButtons;
