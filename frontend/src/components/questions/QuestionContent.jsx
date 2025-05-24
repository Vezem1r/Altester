import { useTranslation } from 'react-i18next';
import AuthenticatedImage from './AuthenticatedImage';
import { QUESTION_TYPES } from './QuestionManagement';

const QuestionContent = ({ question }) => {
  const { t } = useTranslation();

  switch (question.questionType) {
    case QUESTION_TYPES.TEXT_ONLY:
      return <div className="text-gray-800">{question.questionText}</div>;

    case QUESTION_TYPES.IMAGE_ONLY:
      return (
        <div className="flex justify-center">
          <AuthenticatedImage
            imagePath={question.imagePath}
            alt={t('questionContent.question', 'Question')}
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
                'questionContent.questionIllustration',
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
                'questionContent.questionIllustration',
                'Question illustration'
              )}
              className="max-h-64 object-contain rounded-md border border-gray-200 shadow-sm"
            />
          </div>
        </div>
      );

    default:
      return (
        <div className="text-yellow-600">
          {t('questionContent.unknownType', 'Unknown question type')}:{' '}
          {question.questionType}
        </div>
      );
  }
};

export default QuestionContent;
