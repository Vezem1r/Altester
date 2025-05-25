import { motion } from 'framer-motion';
import FormField from '@/components/form/FormField';
import { useTranslation } from 'react-i18next';

const QuestionDistribution = ({
  easyCount,
  mediumCount,
  hardCount,
  onChange,
  error,
}) => {
  const { t } = useTranslation();

  const difficulties = [
    {
      key: 'easy',
      label: t('questionDistribution.easy', 'Easy'),
      description: t('questionDistribution.basicQuestions', 'Basic questions'),
      color: 'green',
      bgGradient: 'from-green-50 via-green-100 to-emerald-100',
      borderColor: 'border-green-300',
      iconBg: 'bg-green-500',
      textColor: 'text-green-800',
      count: Number(easyCount),
      icon: (
        <svg
          className="w-5 h-5 text-white"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M5 13l4 4L19 7"
          />
        </svg>
      ),
    },
    {
      key: 'medium',
      label: t('questionDistribution.medium', 'Medium'),
      description: t(
        'questionDistribution.intermediateLevel',
        'Intermediate level'
      ),
      color: 'yellow',
      bgGradient: 'from-yellow-50 via-amber-100 to-orange-100',
      borderColor: 'border-yellow-300',
      iconBg: 'bg-yellow-500',
      textColor: 'text-yellow-800',
      count: Number(mediumCount),
      icon: (
        <svg
          className="w-5 h-5 text-white"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M13 10V3L4 14h7v7l9-11h-7z"
          />
        </svg>
      ),
    },
    {
      key: 'hard',
      label: t('questionDistribution.hard', 'Hard'),
      description: t(
        'questionDistribution.advancedQuestions',
        'Advanced questions'
      ),
      color: 'red',
      bgGradient: 'from-red-50 via-red-100 to-pink-100',
      borderColor: 'border-red-300',
      iconBg: 'bg-red-500',
      textColor: 'text-red-800',
      count: Number(hardCount),
      icon: (
        <svg
          className="w-5 h-5 text-white"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      ),
    },
  ];

  const totalQuestions =
    Number(easyCount || 0) + Number(mediumCount || 0) + Number(hardCount || 0);

  return (
    <div className="mt-6">
      <div className="flex items-center justify-between mb-4">
        <h4 className="text-sm font-semibold text-gray-800">
          {t('questionDistribution.title', 'Question Distribution')}
        </h4>
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500">
            {t('questionDistribution.totalQuestions', 'Total questions')}:
          </span>
          <span className="text-sm font-bold text-purple-600">
            {totalQuestions}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {difficulties.map(
          (
            {
              key,
              label,
              description,
              bgGradient,
              borderColor,
              iconBg,
              textColor,
              count,
              icon,
            },
            index
          ) => (
            <motion.div
              key={key}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: index * 0.1 }}
              className={`relative overflow-hidden bg-gradient-to-br ${bgGradient} p-5 rounded-xl border-2 ${borderColor} shadow-sm hover:shadow-md transition-all duration-300`}
            >
              <div className="absolute top-0 right-0 -mt-4 -mr-4 h-20 w-20 rounded-full bg-white/10 blur-2xl" />

              <div className="relative">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-10 h-10 rounded-xl ${iconBg} flex items-center justify-center shadow-lg`}
                    >
                      {icon}
                    </div>
                    <div>
                      <h5 className={`font-bold ${textColor}`}>{label}</h5>
                      <p className={`text-xs ${textColor} opacity-70`}>
                        {description}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="relative">
                  <FormField
                    name={`${key}QuestionsCount`}
                    type="number"
                    value={count}
                    onChange={onChange}
                    min="0"
                    className="!bg-white/70 backdrop-blur-sm"
                  />
                  {Number(count) > 0 && (
                    <motion.div
                      initial={{ scale: 0 }}
                      animate={{ scale: 1 }}
                      className={`absolute -top-2 -right-2 w-6 h-6 rounded-full ${iconBg} flex items-center justify-center text-white text-xs font-bold shadow-md`}
                    >
                      {Number(count)}
                    </motion.div>
                  )}
                </div>
              </div>
            </motion.div>
          )
        )}
      </div>

      {totalQuestions > 0 && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="mt-4 p-3 bg-purple-50 border border-purple-200 rounded-lg"
        >
          <div className="flex items-center justify-between">
            <span className="text-sm text-purple-700">
              {t('questionDistribution.distribution', 'Distribution')}:
            </span>
            <div className="flex items-center gap-4">
              {difficulties.map(({ key, label, color, count }) => (
                <div key={key} className="flex items-center gap-1">
                  <span className={`w-3 h-3 rounded-full bg-${color}-500`} />
                  <span className="text-sm text-gray-600">
                    {label}:{' '}
                    <span className="font-semibold">
                      {totalQuestions > 0
                        ? ((Number(count) / totalQuestions) * 100).toFixed(0)
                        : 0}
                      %
                    </span>
                  </span>
                </div>
              ))}
            </div>
          </div>
        </motion.div>
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
};

export default QuestionDistribution;
