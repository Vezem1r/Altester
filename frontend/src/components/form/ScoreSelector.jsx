import { motion } from 'framer-motion';
import { useTranslation } from 'react-i18next';

const ScoreSelector = ({
  label,
  value,
  onChange,
  predefinedScores = [1, 2, 3, 5, 10, 15, 20],
  min = 1,
  max = 100,
  error,
  description,
}) => {
  const { t } = useTranslation();

  return (
    <div className="space-y-3">
      {label && (
        <label className="block text-sm font-medium text-gray-700">
          {label} <span className="text-red-500">*</span>
        </label>
      )}

      <div className="space-y-3">
        <input
          type="number"
          value={value}
          onChange={e => onChange(parseInt(e.target.value) || 0)}
          min={min}
          max={max}
          className={`
            w-full px-4 py-2 border rounded-lg shadow-sm
            focus:ring-2 focus:ring-purple-500 focus:border-purple-500
            ${error ? 'border-red-300' : 'border-gray-300'}
          `}
        />

        <div className="flex flex-wrap gap-2">
          {predefinedScores.map(score => (
            <motion.button
              key={score}
              type="button"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => onChange(score)}
              className={`
                px-3 py-1.5 rounded-lg font-medium text-sm transition-all duration-200
                ${
                  value === score
                    ? 'bg-purple-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }
              `}
            >
              {score}{' '}
              {t('scoreSelector.points', {
                count: score,
                defaultValue: 'pt',
                defaultValue_plural: 'pts',
              })}
            </motion.button>
          ))}
        </div>
      </div>

      {description && <p className="text-xs text-gray-500">{description}</p>}

      {error && <p className="text-sm text-red-600">{error}</p>}
    </div>
  );
};

export default ScoreSelector;
