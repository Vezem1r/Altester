import { memo } from 'react';
import { useTranslation } from 'react-i18next';

const QuestionHeader = memo(
  ({ question, questionNumber, questionPercentage, aiGraded }) => {
    const { t } = useTranslation();

    const getScoreBadgeClass = () => {
      if (questionPercentage === 100) {
        return 'bg-green-100 text-green-800';
      } else if (questionPercentage > 0) {
        return 'bg-yellow-100 text-yellow-800';
      }
      return 'bg-red-100 text-red-800';
    };

    return (
      <div className="bg-gradient-to-r from-purple-500 to-indigo-600 px-6 py-4 relative overflow-hidden">
        <div className="absolute top-0 right-0 bg-white opacity-10 rounded-full w-24 h-24 -mt-8 -mr-8" />
        <div className="absolute bottom-0 left-0 bg-white opacity-10 rounded-full w-16 h-16 -mb-8 -ml-8" />

        <div className="flex items-start relative z-10">
          <div className="flex-shrink-0 mr-3">
            <span className="inline-flex items-center justify-center h-10 w-10 rounded-full bg-white text-purple-800 text-lg font-semibold">
              {questionNumber}
            </span>
          </div>
          <div>
            <h2
              className="text-lg font-medium text-white"
              dangerouslySetInnerHTML={{ __html: question.questionText }}
            />

            <div className="mt-2 flex items-center flex-wrap gap-2">
              <span
                className={`px-3 py-1 rounded-full text-xs font-medium ${getScoreBadgeClass()}`}
              >
                {t('questionHeader.teacherScore', 'Teacher Score')}:{' '}
                {question.score} {t('questionHeader.of', 'of')}{' '}
                {question.maxScore} ({questionPercentage}%)
              </span>

              {question.aiScore !== undefined && (
                <span className="px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  {t('questionHeader.aiScore', 'AI Score')}: {question.aiScore}{' '}
                  {t('questionHeader.of', 'of')} {question.maxScore} (
                  {Math.round((question.aiScore / question.maxScore) * 100)}%)
                </span>
              )}

              {aiGraded && (
                <span className="px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {t('questionHeader.aiGraded', 'AI Graded')}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  }
);

QuestionHeader.displayName = 'QuestionHeader';

export default QuestionHeader;
