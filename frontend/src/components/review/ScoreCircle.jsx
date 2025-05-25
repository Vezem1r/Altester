import { memo } from 'react';
import { useTranslation } from 'react-i18next';

const ScoreCircle = memo(({ percentage }) => {
  const { t } = useTranslation();

  return (
    <div className="mb-4 relative w-36 h-36">
      <svg className="w-full h-full" viewBox="0 0 36 36">
        <circle
          cx="18"
          cy="18"
          r="16"
          fill="none"
          stroke="#e2e8f0"
          strokeWidth="2"
        />

        {percentage > 0 && (
          <circle
            cx="18"
            cy="18"
            r="16"
            fill="none"
            stroke="#8b5cf6"
            strokeWidth="2"
            strokeDasharray={`${percentage * 0.01 * 2 * Math.PI * 16} ${2 * Math.PI * 16}`}
            strokeDashoffset="0"
            transform="rotate(-90 18 18)"
          />
        )}
      </svg>

      <div className="absolute inset-0 flex items-center justify-center">
        <div className="text-center">
          <div className="text-3xl font-bold text-gray-900">{percentage}%</div>
          <div className="text-sm text-gray-500">
            {t('scoreCircle.score', 'Score')}
          </div>
        </div>
      </div>
    </div>
  );
});

ScoreCircle.displayName = 'ScoreCircle';

export default ScoreCircle;
