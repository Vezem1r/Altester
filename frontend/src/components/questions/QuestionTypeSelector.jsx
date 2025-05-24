import { useTranslation } from 'react-i18next';
import { getIconForQuestionType } from './QuestionIcons';
import { QUESTION_TYPES } from './QuestionManagement';

const TYPE_TRANSLATION_KEYS = {
  TEXT_ONLY: 'questionTypeSelector.text_only',
  IMAGE_ONLY: 'questionTypeSelector.image_only',
  TEXT_WITH_IMAGE: 'questionTypeSelector.text_with_image',
  MULTIPLE_CHOICE: 'questionTypeSelector.multiple_choice',
  IMAGE_WITH_MULTIPLE_CHOICE: 'questionTypeSelector.imageAndOptions',
};

const QuestionTypeSelector = ({ selectedType, onTypeChange }) => {
  const { t } = useTranslation();

  const getTypeLabel = key => {
    if (key === 'IMAGE_WITH_MULTIPLE_CHOICE') {
      return t('questionTypeSelector.imageAndOptions', 'Image & Options');
    }

    const translationKey = TYPE_TRANSLATION_KEYS[key];
    if (translationKey) {
      const formattedKey = key
        .split('_')
        .map(word => word.charAt(0) + word.slice(1).toLowerCase())
        .join(' ');

      return t(translationKey, formattedKey);
    }

    return key
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  };

  return (
    <div className="md:col-span-3">
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {t('questionTypeSelector.questionType', 'Question Type')}
      </label>
      <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
        {Object.entries(QUESTION_TYPES).map(([key, value]) => (
          <div
            key={key}
            onClick={() => onTypeChange(value)}
            className={`
              flex flex-col items-center p-3 rounded-lg cursor-pointer border
              ${
                selectedType === value
                  ? 'bg-purple-100 border-purple-500 font-medium'
                  : 'bg-gray-50 border-gray-200 hover:bg-gray-100'
              }
            `}
          >
            {getIconForQuestionType(value)}
            <span className="text-xs text-gray-700 text-center">
              {getTypeLabel(key)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default QuestionTypeSelector;
