import { motion } from 'framer-motion';
import { useTranslation } from 'react-i18next';

const DifficultySelector = ({
  label,
  value,
  onChange,
  difficulties = ['EASY', 'MEDIUM', 'HARD'],
  error,
  description,
}) => {
  const { t } = useTranslation();

  const getDifficultyConfig = difficulty => {
    switch (difficulty) {
      case 'EASY':
        return {
          color: 'green',
          icon: '🟢',
          label: t('difficultySelector.easy', 'Easy'),
        };
      case 'MEDIUM':
        return {
          color: 'yellow',
          icon: '🟡',
          label: t('difficultySelector.medium', 'Medium'),
        };
      case 'HARD':
        return {
          color: 'red',
          icon: '🔴',
          label: t('difficultySelector.hard', 'Hard'),
        };
      default:
        return {
          color: 'gray',
          icon: '⚪',
          label: difficulty,
        };
    }
  };

  return (
    <div className="space-y-3">
      {label && (
        <label className="block text-sm font-medium text-gray-700">
          {label} <span className="text-red-500">*</span>
        </label>
      )}

      <div className="flex flex-wrap gap-3">
        {difficulties.map(difficulty => {
          const config = getDifficultyConfig(difficulty);
          const isSelected = value === difficulty;

          return (
            <motion.button
              key={difficulty}
              type="button"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => onChange(difficulty)}
              className={`
                px-4 py-2 rounded-lg font-medium transition-all duration-200 flex items-center gap-2
                ${
                  isSelected
                    ? 'bg-purple-600 text-white border-2 border-purple-600'
                    : `bg-${config.color}-50 text-${config.color}-800 border-2 border-${config.color}-200 hover:border-${config.color}-300`
                }
              `}
            >
              <span>{config.icon}</span>
              {config.label}
            </motion.button>
          );
        })}
      </div>

      {description && <p className="text-xs text-gray-500">{description}</p>}

      {error && <p className="text-sm text-red-600">{error}</p>}
    </div>
  );
};

export default DifficultySelector;
