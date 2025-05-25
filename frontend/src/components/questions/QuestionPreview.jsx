import { useTranslation } from 'react-i18next';
import AuthenticatedImage from './AuthenticatedImage';
import QuestionTypeBadge from './QuestionTypeBadge';

const QUESTION_TYPES = {
  TEXT_ONLY: 'TEXT_ONLY',
  IMAGE_ONLY: 'IMAGE_ONLY',
  TEXT_WITH_IMAGE: 'TEXT_WITH_IMAGE',
  MULTIPLE_CHOICE: 'MULTIPLE_CHOICE',
  IMAGE_WITH_MULTIPLE_CHOICE: 'IMAGE_WITH_MULTIPLE_CHOICE',
};

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
  easy: 'questionPreview.difficulty.easy',
  medium: 'questionPreview.difficulty.medium',
  hard: 'questionPreview.difficulty.hard',
};

const QuestionPreview = ({ questions }) => {
  const { t } = useTranslation();

  if (!questions || questions.length === 0) {
    return (
      <div className="text-center p-6 bg-gray-50 rounded-lg">
        <p className="text-gray-500">
          {t(
            'questionPreview.noQuestions',
            'No questions have been added to this test yet.'
          )}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {questions.map((question, index) => {
        const processedQuestion = { ...question };

        if (
          (processedQuestion.questionType === QUESTION_TYPES.MULTIPLE_CHOICE ||
            processedQuestion.questionType ===
              QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE) &&
          processedQuestion.options &&
          processedQuestion.options.length > 0
        ) {
          processedQuestion.options = processedQuestion.options.map(opt => {
            if ('correct' in opt && !('isCorrect' in opt)) {
              const { correct, ...rest } = opt;
              return {
                ...rest,
                isCorrect: Boolean(correct),
              };
            }
            return opt;
          });
        }

        return (
          <div
            key={processedQuestion.id}
            className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden"
          >
            <div className="bg-gray-50 p-4 border-b border-gray-200">
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <span className="inline-flex items-center justify-center h-6 w-6 rounded-full bg-purple-100 text-purple-800 text-sm font-medium mr-3">
                    {index + 1}
                  </span>
                  <h3 className="text-sm font-medium text-gray-900">
                    {processedQuestion.questionType ===
                      QUESTION_TYPES.IMAGE_ONLY ||
                    (processedQuestion.questionType ===
                      QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE &&
                      !processedQuestion.questionText)
                      ? t('questionPreview.imageQuestion', 'Image question')
                      : processedQuestion.questionText}
                  </h3>
                </div>
                <div className="flex items-center space-x-3">
                  <QuestionTypeBadge type={processedQuestion.questionType} />

                  {/* Display difficulty badge */}
                  {processedQuestion.difficulty && (
                    <span
                      className={`text-xs font-medium px-2 py-1 rounded-md ${getDifficultyBadgeClass(processedQuestion.difficulty)}`}
                    >
                      {t(
                        DIFFICULTY_TRANSLATION_KEYS[
                          processedQuestion.difficulty.toLowerCase()
                        ] || 'questionPreview.difficulty.unknown',
                        processedQuestion.difficulty
                      )}
                    </span>
                  )}

                  <span className="text-xs font-medium text-gray-500 bg-gray-100 px-2 py-1 rounded-md">
                    {processedQuestion.score}{' '}
                    {processedQuestion.score === 1
                      ? t('questionPreview.point', 'point')
                      : t('questionPreview.points', 'points')}
                  </span>
                </div>
              </div>
            </div>

            <div className="p-4">
              {renderQuestionContent(processedQuestion, t)}

              {(processedQuestion.questionType ===
                QUESTION_TYPES.MULTIPLE_CHOICE ||
                processedQuestion.questionType ===
                  QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE) &&
                processedQuestion.options &&
                processedQuestion.options.length > 0 && (
                  <div className="mt-4 space-y-2">
                    {processedQuestion.options.map((option, optionIndex) => (
                      <div
                        key={option.id || optionIndex}
                        className={`flex items-start p-3 rounded-md border ${
                          option.isCorrect || option.correct
                            ? 'border-green-200 bg-green-50'
                            : 'border-gray-200'
                        }`}
                      >
                        <div className="flex items-center h-5">
                          <input
                            id={`option-${processedQuestion.id}-${optionIndex}`}
                            name={`question-${processedQuestion.id}-${optionIndex}`}
                            type="checkbox"
                            checked={option.isCorrect || option.correct}
                            readOnly
                            className="focus:ring-purple-500 h-4 w-4 text-purple-600 border-gray-300 rounded"
                          />
                        </div>
                        <label
                          htmlFor={`option-${processedQuestion.id}-${optionIndex}`}
                          className="ml-3 block text-sm font-medium text-gray-700"
                        >
                          {option.text}
                          {option.description && (
                            <p className="text-gray-500 text-xs mt-1">
                              {option.description}
                            </p>
                          )}
                        </label>
                      </div>
                    ))}
                  </div>
                )}

              {/* Display correct answer if available */}
              {processedQuestion.correctAnswer && (
                <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
                  <h4 className="text-sm font-medium text-gray-700 mb-1">
                    {t('questionPreview.correctAnswer', 'Correct Answer:')}
                  </h4>
                  <p className="text-sm text-gray-800">
                    {processedQuestion.correctAnswer}
                  </p>
                </div>
              )}

              {processedQuestion.questionType !==
                QUESTION_TYPES.MULTIPLE_CHOICE &&
                processedQuestion.questionType !==
                  QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE && (
                  <div className="mt-4">
                    <label
                      htmlFor={`answer-${processedQuestion.id}`}
                      className="block text-sm font-medium text-gray-700 mb-1"
                    >
                      {t('questionPreview.answer', 'Answer')}
                    </label>
                    <div className="relative">
                      <textarea
                        id={`answer-${processedQuestion.id}`}
                        rows="3"
                        className="shadow-sm block w-full sm:text-sm border-gray-200 rounded-md bg-gray-50 cursor-not-allowed"
                        placeholder={t(
                          'questionPreview.answerPlaceholder',
                          'Student answer will appear here...'
                        )}
                        disabled
                      />
                      <div className="absolute inset-0 bg-transparent pointer-events-none" />
                    </div>
                  </div>
                )}
            </div>
          </div>
        );
      })}
    </div>
  );
};

const renderQuestionContent = (question, t) => {
  switch (question.questionType) {
    case QUESTION_TYPES.TEXT_ONLY:
      return <div className="text-gray-800">{question.questionText}</div>;

    case QUESTION_TYPES.IMAGE_ONLY:
      return (
        <div className="flex justify-center">
          <AuthenticatedImage
            imagePath={question.imagePath}
            alt={t('questionPreview.questionImage', 'Question')}
            className="max-h-64 object-contain rounded-md border border-gray-200 shadow-sm"
          />
        </div>
      );

    case QUESTION_TYPES.TEXT_WITH_IMAGE:
      return (
        <div>
          <div className="text-gray-800 mb-4">{question.questionText}</div>
          <div className="flex justify-center">
            <AuthenticatedImage
              imagePath={question.imagePath}
              alt={t(
                'questionPreview.questionIllustration',
                'Question illustration'
              )}
              className="max-h-64 object-contain rounded-md border border-gray-200 shadow-sm"
            />
          </div>
        </div>
      );

    case QUESTION_TYPES.MULTIPLE_CHOICE:
      return <div className="text-gray-800">{question.questionText}</div>;

    case QUESTION_TYPES.IMAGE_WITH_MULTIPLE_CHOICE:
      return (
        <div>
          {question.questionText && (
            <div className="text-gray-800 mb-4">{question.questionText}</div>
          )}
          <div className="flex justify-center">
            <AuthenticatedImage
              imagePath={question.imagePath}
              alt={t(
                'questionPreview.questionIllustration',
                'Question illustration'
              )}
              className="max-h-64 object-contain rounded-md border border-gray-200 shadow-sm"
            />
          </div>
        </div>
      );

    default:
      return (
        <div>{t('questionPreview.unknownType', 'Unknown question type')}</div>
      );
  }
};

export default QuestionPreview;
