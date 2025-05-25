import { useTranslation } from 'react-i18next';
import { getIconForQuestionType } from './QuestionIcons';

const QuestionTypeBadge = ({ type }) => {
  const { t } = useTranslation();
  let label = '';
  let colorClass = '';

  switch (type) {
    case 'TEXT_ONLY':
      label = t('questionTypeBadge.textOnly', 'Text Only');
      colorClass = 'bg-blue-100 text-blue-800';
      break;
    case 'IMAGE_ONLY':
      label = t('questionTypeBadge.imageOnly', 'Image Only');
      colorClass = 'bg-indigo-100 text-indigo-800';
      break;
    case 'TEXT_WITH_IMAGE':
      label = t('questionTypeBadge.textWithImage', 'Text + Image');
      colorClass = 'bg-purple-100 text-purple-800';
      break;
    case 'MULTIPLE_CHOICE':
      label = t('questionTypeBadge.multipleChoice', 'Multiple Choice');
      colorClass = 'bg-teal-100 text-teal-800';
      break;
    case 'IMAGE_WITH_MULTIPLE_CHOICE':
      label = t('questionTypeBadge.imageWithChoices', 'Image + Choices');
      colorClass = 'bg-cyan-100 text-cyan-800';
      break;
    default:
      label = t('questionTypeBadge.unknownType', 'Unknown Type');
      colorClass = 'bg-gray-100 text-gray-800';
  }

  return (
    <span
      className={`inline-flex items-center px-2 py-1 rounded-md text-xs font-medium ${colorClass}`}
    >
      {getIconForQuestionType(type, 'h-3 w-3 mr-1')}
      {label}
    </span>
  );
};

export default QuestionTypeBadge;
