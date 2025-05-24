import { useTranslation } from 'react-i18next';

const QuestionProgress = ({ answeredQuestions, totalQuestions }) => {
  const { t } = useTranslation();
  const progressPercentage =
    Math.round((answeredQuestions / totalQuestions) * 100) || 0;

  return (
    <div>
      <div className="flex justify-between items-center mb-1">
        <span className="text-sm font-medium text-gray-700 flex items-center">
          <svg
            className="mr-1 h-4 w-4 text-green-500"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
              clipRule="evenodd"
            />
          </svg>
          {t('questionProgress.savedAnswersProgress', 'Saved Answers Progress')}
        </span>
        <span className="text-sm font-medium text-purple-600">
          {t(
            'questionProgress.questionsAnswered',
            '{{answered}}/{{total}} questions answered',
            {
              answered: answeredQuestions,
              total: totalQuestions,
            }
          )}
        </span>
      </div>

      <div className="w-full bg-gray-200 rounded-full h-2.5">
        <div
          className="bg-purple-600 h-2.5 rounded-full transition-all duration-300 ease-in-out"
          style={{ width: `${progressPercentage}%` }}
        />
      </div>

      <div className="mt-1 text-right">
        <span className="text-xs text-gray-500">
          {t('questionProgress.percentComplete', '{{percent}}% complete', {
            percent: progressPercentage,
          })}
        </span>
      </div>
    </div>
  );
};

export default QuestionProgress;
