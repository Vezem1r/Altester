import { memo } from 'react';
import { useTranslation } from 'react-i18next';

const TextAnswer = memo(({ studentAnswer, status }) => {
  const { t } = useTranslation();
  const isEmpty = studentAnswer.trim() === '';
  const isAiReviewed = status === 'AI_REVIEWED';

  return (
    <div className="px-6 py-4">
      <h3 className="text-md font-medium text-gray-900 mb-3 flex items-center">
        <svg
          className="h-5 w-5 text-purple-500 mr-2"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            fillRule="evenodd"
            d="M18 13V5a2 2 0 00-2-2H4a2 2 0 00-2 2v8a2 2 0 002 2h3l3 3 3-3h3a2 2 0 002-2zM5 7a1 1 0 011-1h8a1 1 0 110 2H6a1 1 0 01-1-1zm1 3a1 1 0 100 2h3a1 1 0 100-2H6z"
            clipRule="evenodd"
          />
        </svg>
        {t('textAnswer.yourTextAnswer', 'Your Text Answer')}
      </h3>

      <div className="flex mb-3">
        <div className="bg-blue-100 text-blue-800 text-xs font-medium px-3 py-1 rounded-full flex items-center">
          <svg
            className="h-4 w-4 mr-1"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
          </svg>
          {t('textAnswer.openEndedResponse', 'Open-ended response')}
        </div>
      </div>

      <div className="border border-gray-200 rounded-lg overflow-hidden">
        <div className="bg-gray-50 px-4 py-2 border-b border-gray-200">
          <div className="flex justify-between items-center">
            <span className="font-medium text-gray-700">
              {t('textAnswer.yourAnswer', 'Your Answer:')}
            </span>
            {isEmpty && (
              <span className="text-xs bg-yellow-100 text-yellow-800 px-2 py-0.5 rounded">
                {t('textAnswer.emptyResponse', 'Empty response')}
              </span>
            )}
          </div>
        </div>
        <div className="p-4 bg-white">
          {!isEmpty ? (
            <p className="whitespace-pre-wrap">{studentAnswer}</p>
          ) : (
            <p className="text-gray-400 italic">
              {t(
                'textAnswer.didNotProvideAnswer',
                'You did not provide an answer to this question.'
              )}
            </p>
          )}
        </div>
      </div>

      <div className="mt-3 bg-gray-50 border border-gray-200 rounded-lg p-3">
        <div className="flex items-start">
          <svg
            className="h-5 w-5 text-gray-500 mr-2 flex-shrink-0 mt-0.5"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zm-1 4a1 1 0 00-1 1v2a1 1 0 102 0v-2a1 1 0 00-1-1z"
              clipRule="evenodd"
            />
          </svg>
          <div>
            <p className="font-medium text-gray-700">
              {t('textAnswer.evaluationInformation', 'Evaluation information')}
            </p>
            <p className="mt-1 text-sm text-gray-600">
              {isAiReviewed
                ? t(
                    'textAnswer.aiEvaluatedAnswer',
                    'This answer was initially evaluated by AI and may be reviewed by a teacher.'
                  )
                : t(
                    'textAnswer.teacherEvaluatedAnswer',
                    'This answer was evaluated by your teacher.'
                  )}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
});

TextAnswer.displayName = 'TextAnswer';

export default TextAnswer;
