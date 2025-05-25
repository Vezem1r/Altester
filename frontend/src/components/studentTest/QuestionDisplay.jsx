import AuthenticatedImage from '@/components/questions/AuthenticatedImage';
import { useTranslation } from 'react-i18next';

const QuestionDisplay = ({
  question,
  questionNumber,
  answer = { selectedOptionIds: [], answerText: '' },
  onOptionSelect,
  onTextChange,
}) => {
  const { t } = useTranslation();
  const isMultipleChoice = question.questionType.includes('MULTIPLE');

  return (
    <div className="mb-8 pb-6 border-b border-gray-200 last:border-0">
      <div className="mb-4">
        <div className="flex items-start">
          <div className="flex-shrink-0 mr-2">
            <span className="flex items-center justify-center h-6 w-6 rounded-full bg-purple-100 text-purple-800 text-sm font-semibold">
              {questionNumber}
            </span>
          </div>
          <h3
            className="text-lg font-medium text-gray-900"
            dangerouslySetInnerHTML={{ __html: question.questionText }}
          />
        </div>

        {question.score && (
          <div className="mt-1 ml-8">
            <span className="text-sm text-gray-500">
              ({question.score}{' '}
              {question.score === 1
                ? t('questionDisplay.point', 'point')
                : t('questionDisplay.points', 'points')}
              )
            </span>
          </div>
        )}
      </div>

      {question.imagePath && (
        <div className="mb-4 ml-8">
          <AuthenticatedImage
            imagePath={question.imagePath}
            alt={t('questionDisplay.questionImage', 'Question')}
            className="max-w-full rounded-md shadow-sm"
          />
        </div>
      )}

      <div className="ml-8">
        {question.questionType.includes('CHOICE') ? (
          <div className="space-y-2">
            {question.options.map(option => (
              <div
                key={option.id}
                className={`flex items-start p-3 rounded-md cursor-pointer hover:bg-gray-50 ${
                  answer.selectedOptionIds.includes(option.id)
                    ? 'bg-purple-50 border border-purple-200'
                    : 'bg-white border border-gray-200'
                }`}
                onClick={() =>
                  onOptionSelect(question.id, option.id, isMultipleChoice)
                }
              >
                <div className="flex-shrink-0 mr-2">
                  {isMultipleChoice ? (
                    <div className="relative flex items-start">
                      <div className="flex items-center h-5">
                        <input
                          type="checkbox"
                          className="h-4 w-4 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
                          checked={answer.selectedOptionIds.includes(option.id)}
                          onChange={() =>
                            onOptionSelect(question.id, option.id, true)
                          }
                        />
                      </div>
                    </div>
                  ) : (
                    <div className="relative flex items-start">
                      <div className="flex items-center h-5">
                        <input
                          type="radio"
                          className="h-4 w-4 text-purple-600 border-gray-300 focus:ring-purple-500"
                          checked={answer.selectedOptionIds.includes(option.id)}
                          onChange={() =>
                            onOptionSelect(question.id, option.id, false)
                          }
                        />
                      </div>
                    </div>
                  )}
                </div>
                <div>
                  <div dangerouslySetInnerHTML={{ __html: option.text }} />
                  {option.description && (
                    <p className="text-sm text-gray-500 mt-1">
                      {option.description}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div>
            <textarea
              rows="4"
              className="shadow-sm block w-full focus:ring-purple-500 focus:border-purple-500 sm:text-sm border border-gray-300 rounded-md"
              placeholder={t(
                'questionDisplay.enterYourAnswer',
                'Enter your answer here...'
              )}
              value={answer.answerText || ''}
              onChange={e => onTextChange(question.id, e.target.value)}
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default QuestionDisplay;
