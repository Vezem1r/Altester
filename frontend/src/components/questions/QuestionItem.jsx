import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import QuestionTypeBadge from './QuestionTypeBadge';
import QuestionContent from './QuestionContent';
import { QUESTION_TYPES } from './QuestionManagement';

const getDifficultyBadgeClass = difficulty => {
  switch (difficulty) {
    case 'EASY':
      return 'bg-green-100 text-green-800';
    case 'MEDIUM':
      return 'bg-yellow-100 text-yellow-800';
    case 'HARD':
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

const DIFFICULTY_TRANSLATION_KEYS = {
  EASY: 'questionItem.difficulty.easy',
  MEDIUM: 'questionItem.difficulty.medium',
  HARD: 'questionItem.difficulty.hard',
};

const getDifficultyTranslationKey = difficulty => {
  return (
    DIFFICULTY_TRANSLATION_KEYS[difficulty] || 'questionItem.difficulty.unknown'
  );
};

const QuestionItem = ({
  question,
  isExpanded,
  canEditTest,
  onEdit,
  onDelete,
  onToggleDetails,
}) => {
  const { t } = useTranslation();

  return (
    <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
      <div
        className="flex items-center justify-between p-4 cursor-pointer bg-gray-50 border-b border-gray-200"
        onClick={onToggleDetails}
      >
        <div className="flex items-center">
          <div className="flex-1">
            <h3 className="text-sm font-medium text-gray-900 flex items-center">
              <span className="truncate">
                {question.questionText ||
                  t('questionItem.imageQuestion', 'Image question')}
              </span>
              <span className="ml-2 text-xs font-normal text-gray-500">
                ({question.score} {t('questionItem.points', 'points')})
              </span>
            </h3>
            <div className="flex flex-wrap items-center gap-2 mt-1">
              <QuestionTypeBadge type={question.questionType} />

              {/* Difficulty badge */}
              {question.difficulty && (
                <span
                  className={`px-2 py-0.5 text-xs font-medium rounded-full ${getDifficultyBadgeClass(question.difficulty)}`}
                >
                  {t(
                    getDifficultyTranslationKey(question.difficulty),
                    question.difficulty
                  )}
                </span>
              )}
            </div>
          </div>
        </div>

        {canEditTest && (
          <div className="flex space-x-2" onClick={e => e.stopPropagation()}>
            <button
              onClick={onEdit}
              className="text-blue-600 hover:text-blue-800"
              type="button"
              aria-label={t('questionItem.edit', 'Edit')}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                />
              </svg>
            </button>
            <button
              onClick={onDelete}
              className="text-red-600 hover:text-red-800"
              type="button"
              aria-label={t('questionItem.delete', 'Delete')}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                />
              </svg>
            </button>
          </div>
        )}
      </div>

      {isExpanded && (
        <div className="p-4 border-t border-gray-200">
          <QuestionContent question={question} />

          {(question.questionType === QUESTION_TYPES.MULTIPLE_CHOICE ||
            question.questionType ===
              QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE) &&
            question.options &&
            question.options.length > 0 && (
              <div className="mt-4">
                <h4 className="font-medium text-gray-700 mb-2">
                  {t('questionItem.options', 'Options:')}
                </h4>
                <ul className="space-y-2">
                  {question.options.map((option, optIndex) => {
                    const isCorrectOption = option.isCorrect || option.correct;

                    return (
                      <li
                        key={option.id || optIndex}
                        className={`flex items-start p-2 rounded ${
                          isCorrectOption
                            ? 'bg-green-50 border border-green-200'
                            : 'bg-gray-50'
                        }`}
                      >
                        <div
                          className={`flex-shrink-0 h-5 w-5 rounded-full mr-2 ${
                            isCorrectOption ? 'bg-green-500' : 'bg-gray-300'
                          }`}
                        >
                          {isCorrectOption && (
                            <svg
                              className="h-5 w-5 text-white"
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
                          )}
                        </div>
                        <div className="ml-2">
                          <div className="text-sm font-medium">
                            {option.text}
                          </div>
                          {option.description && (
                            <div className="text-xs text-gray-500 mt-1">
                              {option.description}
                            </div>
                          )}
                        </div>
                      </li>
                    );
                  })}
                </ul>
              </div>
            )}

          {question.correctAnswer && (
            <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
              <h4 className="font-medium text-gray-700 mb-1">
                {t('questionItem.correctAnswer', 'Correct Answer:')}
              </h4>
              <p className="text-sm text-gray-800">{question.correctAnswer}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default memo(QuestionItem);
