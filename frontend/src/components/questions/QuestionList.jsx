import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import QuestionItem from './QuestionItem';

const QuestionList = ({
  questions,
  canEditTest,
  expandedQuestion,
  onEdit,
  onDelete,
  onToggleDetails,
}) => {
  const { t } = useTranslation();

  if (!questions || questions.length === 0) {
    return (
      <div className="text-center p-6 bg-gray-50 rounded-lg">
        <p className="text-gray-500">
          {t(
            'questionList.noQuestions',
            'No questions have been added to this test yet.'
          )}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {questions.map(question => (
        <QuestionItem
          key={question.id}
          question={question}
          isExpanded={expandedQuestion === question.id}
          canEditTest={canEditTest}
          onEdit={() => onEdit(question)}
          onDelete={() => onDelete(question.id)}
          onToggleDetails={() => onToggleDetails(question.id)}
        />
      ))}
    </div>
  );
};

export default memo(QuestionList);
