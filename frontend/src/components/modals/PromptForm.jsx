import { useState, useEffect, useCallback, useRef } from 'react';
import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import FormField from '@/components/ui/FormField';
import { useTranslation } from 'react-i18next';

const REQUIRED_FORMAT_SECTION = `Please format your response as follows:
Score: [number]
Feedback: [your detailed feedback explaining the score, what was correct, and what could be improved]`;

const REQUIRED_PLACEHOLDERS = [
  {
    key: '{{QUESTION}}',
    description: 'The actual question text',
    required: true,
  },
  {
    key: '{{CORRECT_ANSWER_SECTION}}',
    description: 'Section with the correct answer',
    required: true,
  },
  {
    key: '{{STUDENT_ANSWER}}',
    description: "Student's answer to be evaluated",
    required: true,
  },
  {
    key: '{{MAX_SCORE}}',
    description: 'Maximum possible score',
    required: true,
  },
  {
    key: '{{FORMAT_SECTION}}',
    description: 'Required response format instructions',
    required: true,
  },
];

const TEMPLATE_HELPERS = [
  {
    title: 'Basic Evaluation Template',
    template: `You are an AI assistant tasked with evaluating student answers.

Question: {{QUESTION}}

Correct Answer:
{{CORRECT_ANSWER_SECTION}}

Student's Answer:
{{STUDENT_ANSWER}}

Maximum Score: {{MAX_SCORE}}

Please evaluate the student's answer based on accuracy, completeness, and clarity. Provide a score and detailed feedback.

{{FORMAT_SECTION}}`,
  },
  {
    title: 'Rubric-Based Template',
    template: `Evaluate the following student answer using a rubric-based approach.

Question: {{QUESTION}}

Model Answer:
{{CORRECT_ANSWER_SECTION}}

Student's Response:
{{STUDENT_ANSWER}}

Grading Criteria (Total: {{MAX_SCORE}} points):
1. Accuracy (40%)
2. Completeness (30%)
3. Clarity and Organization (20%)
4. Use of Examples (10%)

Provide a score breakdown and constructive feedback.

{{FORMAT_SECTION}}`,
  },
];

const convertFormatSectionToPlaceholder = content => {
  if (!content) return content;

  const escapedFormatSection = REQUIRED_FORMAT_SECTION.replace(
    /[.*+?^${}()|[\]\\]/g,
    '\\$&'
  );
  const regex = new RegExp(escapedFormatSection, 'g');

  return content.replace(regex, '{{FORMAT_SECTION}}');
};

const PromptForm = ({ isEditing, initialData, onSubmit, onCancel }) => {
  const { t } = useTranslation();

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    content: '',
    isPublic: false,
  });

  const [errors, setErrors] = useState({});
  const [showTemplateHelper, setShowTemplateHelper] = useState(false);
  const [usedPlaceholders, setUsedPlaceholders] = useState([]);
  const textareaRef = useRef(null);

  useEffect(() => {
    if (initialData) {
      const convertedContent = convertFormatSectionToPlaceholder(
        initialData.content
      );

      setFormData({
        title: initialData.title || '',
        description: initialData.description || '',
        content: convertedContent || '',
        isPublic: initialData.isPublic || false,
      });
    }
  }, [initialData]);

  useEffect(() => {
    const style = document.createElement('style');
    style.textContent = `
      .prompt-form textarea {
        padding-left: 16px !important;
        padding-right: 16px !important;
        text-indent: 0 !important;
        box-sizing: border-box !important;
      }
    `;
    document.head.appendChild(style);

    return () => {
      document.head.removeChild(style);
    };
  }, []);

  useEffect(() => {
    const used = REQUIRED_PLACEHOLDERS.filter(p =>
      formData.content.includes(p.key)
    ).map(p => p.key);
    setUsedPlaceholders(used);
  }, [formData.content]);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.title.trim()) {
      newErrors.title = t('PromptForm.titleRequired', 'Title is required');
    } else if (formData.title.length < 3) {
      newErrors.title = t(
        'PromptForm.titleTooShort',
        'Title must be at least 3 characters'
      );
    } else if (formData.title.length > 255) {
      newErrors.title = t(
        'PromptForm.titleTooLong',
        'Title must be less than 255 characters'
      );
    }

    if (formData.description && formData.description.length > 1000) {
      newErrors.description = t(
        'PromptForm.descriptionTooLong',
        'Description must be less than 1000 characters'
      );
    }

    const content = formData.content;
    if (!content.trim()) {
      newErrors.content = t(
        'PromptForm.contentRequired',
        'Content is required'
      );
    } else if (content.length < 50) {
      newErrors.content = t(
        'PromptForm.contentTooShort',
        'Content must be at least 50 characters'
      );
    } else if (content.length > 50000) {
      newErrors.content = t(
        'PromptForm.contentTooLong',
        'Content must be less than 50000 characters'
      );
    } else {
      const missingPlaceholders = REQUIRED_PLACEHOLDERS.filter(
        p => p.required && !content.includes(p.key)
      ).map(p => p.key);

      if (missingPlaceholders.length > 0) {
        newErrors.content = t(
          'PromptForm.missingPlaceholders',
          `Missing required placeholders: ${missingPlaceholders.join(', ')}`
        );
      }

      REQUIRED_PLACEHOLDERS.forEach(placeholder => {
        const count = (
          content.match(
            new RegExp(
              placeholder.key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'),
              'g'
            )
          ) || []
        ).length;
        if (count > 1) {
          newErrors.content = t(
            'PromptForm.duplicatePlaceholder',
            `Placeholder ${placeholder.key} can only be used once`
          );
        }
      });
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = e => {
    e.preventDefault();
    if (validateForm()) {
      const finalContent = formData.content.replace(
        '{{FORMAT_SECTION}}',
        REQUIRED_FORMAT_SECTION
      );
      onSubmit({
        ...formData,
        content: finalContent,
      });
    }
  };

  const insertPlaceholder = useCallback(
    placeholder => {
      const textarea = textareaRef.current;
      if (!textarea) return;

      if (usedPlaceholders.includes(placeholder)) {
        const updatedContent = formData.content.replace(placeholder, '');
        setFormData(prev => ({
          ...prev,
          content: updatedContent,
        }));

        if (errors.content && errors.content.includes(placeholder)) {
          setErrors(prev => ({ ...prev, content: null }));
        }
        return;
      }

      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const text = formData.content;
      const before = text.substring(0, start);
      const after = text.substring(end, text.length);

      let insertText = placeholder;
      if (placeholder === '{{FORMAT_SECTION}}') {
        insertText = `\n\n${placeholder}\n\n`;
      }

      const newContent = before + insertText + after;
      setFormData(prev => ({
        ...prev,
        content: newContent,
      }));

      requestAnimationFrame(() => {
        textarea.focus();
        const newPosition = start + insertText.length;
        textarea.setSelectionRange(newPosition, newPosition);
      });
    },
    [formData.content, usedPlaceholders, errors.content]
  );

  const applyTemplate = template => {
    setFormData(prev => ({
      ...prev,
      content: template,
    }));
    setShowTemplateHelper(false);
  };

  const handleChange = useCallback(
    e => {
      const { name, value, type, checked } = e.target;

      if (name === 'content') {
        const cursorPosition = e.target.selectionStart;
        setFormData(prev => ({
          ...prev,
          content: value,
        }));
        requestAnimationFrame(() => {
          if (textareaRef.current) {
            textareaRef.current.setSelectionRange(
              cursorPosition,
              cursorPosition
            );
          }
        });
      } else {
        setFormData(prev => ({
          ...prev,
          [name]: type === 'checkbox' ? checked : value,
        }));
      }

      if (errors[name]) {
        setErrors(prev => ({ ...prev, [name]: null }));
      }
    },
    [errors]
  );

  const footer = (
    <ModalFooter
      primaryButtonText={
        isEditing
          ? t('PromptForm.updatePrompt', 'Update Prompt')
          : t('PromptForm.createPrompt', 'Create Prompt')
      }
      secondaryButtonText={t('PromptForm.cancel', 'Cancel')}
      onPrimaryClick={handleSubmit}
      onSecondaryClick={onCancel}
      reverseOrder
    />
  );

  const PlaceholderSidebar = ({
    usedPlaceholders,
    onInsertPlaceholder,
    errors,
  }) => (
    <div className="w-80 bg-gray-50 border-l border-gray-200 overflow-y-auto">
      <div className="sticky top-0 bg-gray-50 border-b border-gray-200 px-4 py-3">
        <h3 className="text-sm font-medium text-gray-900">
          {t('PromptForm.requiredPlaceholders', 'Required Placeholders')}
        </h3>
        <p className="text-xs text-gray-500 mt-1">
          {t(
            'PromptForm.placeholdersHelp',
            'Click to insert, click again to remove'
          )}
        </p>
      </div>
      <div className="p-4 space-y-2">
        {REQUIRED_PLACEHOLDERS.map(placeholder => {
          const isUsed = usedPlaceholders.includes(placeholder.key);
          return (
            <button
              key={placeholder.key}
              type="button"
              onClick={() => onInsertPlaceholder(placeholder.key)}
              className={`w-full flex flex-col items-start px-3 py-2 text-sm rounded-md transition-colors ${
                isUsed
                  ? 'bg-green-100 border border-green-300 text-green-800 hover:bg-green-200'
                  : 'bg-white border border-gray-200 hover:bg-gray-100 hover:border-gray-300'
              }`}
            >
              <div className="flex justify-between items-center w-full">
                <span className="font-mono text-xs">{placeholder.key}</span>
                <span
                  className={`text-xs ${isUsed ? 'text-green-600' : 'text-gray-500'}`}
                >
                  {isUsed
                    ? t('PromptForm.clickToRemove', '✓ Click to remove')
                    : t('PromptForm.clickToInsert', 'Click to insert')}
                </span>
              </div>
              <span className="text-xs text-gray-600 mt-1 text-left">
                {placeholder.description}
              </span>
            </button>
          );
        })}
      </div>
      {errors.content && (
        <div className="px-4 pb-4">
          <div className="p-3 bg-red-50 border border-red-200 rounded-md">
            <p className="text-sm text-red-600">{errors.content}</p>
          </div>
        </div>
      )}
    </div>
  );

  const modalContent = (
    <div className="flex h-full prompt-form">
      <div className="flex-1 overflow-y-auto px-6 py-4">
        <form onSubmit={handleSubmit} className="space-y-4">
          <FormField
            label={t('PromptForm.title', 'Title')}
            name="title"
            value={formData.title}
            onChange={handleChange}
            placeholder={t(
              'PromptForm.titlePlaceholder',
              'Enter a descriptive title for your prompt'
            )}
            error={errors.title}
            required
          />

          <div>
            <FormField
              label={t('PromptForm.description', 'Description')}
              name="description"
              type="textarea"
              value={formData.description}
              onChange={handleChange}
              placeholder={t(
                'PromptForm.descriptionPlaceholder',
                'Brief description of what this prompt does'
              )}
              rows={2}
              error={errors.description}
            />
          </div>

          <div>
            <div className="flex justify-between items-center mb-2">
              <label
                htmlFor="content"
                className="block text-sm font-medium text-gray-700"
              >
                {t('PromptForm.promptContent', 'Prompt Content')}{' '}
                <span className="text-red-500">*</span>
              </label>
              <button
                type="button"
                onClick={() => setShowTemplateHelper(!showTemplateHelper)}
                className="text-sm text-purple-600 hover:text-purple-700"
              >
                {showTemplateHelper
                  ? t('PromptForm.hideTemplates', 'Hide Templates')
                  : t('PromptForm.useTemplate', 'Use Template')}
              </button>
            </div>

            {showTemplateHelper && (
              <div className="mb-3 p-3 bg-gray-50 rounded-md">
                <h4 className="text-sm font-medium text-gray-900 mb-2">
                  {t('PromptForm.templateExamples', 'Template Examples')}
                </h4>
                <div className="space-y-2">
                  {TEMPLATE_HELPERS.map((template, index) => (
                    <button
                      key={index}
                      type="button"
                      onClick={() => applyTemplate(template.template)}
                      className="w-full text-left px-3 py-2 text-sm bg-white border border-gray-200 rounded hover:bg-purple-50 hover:border-purple-300"
                    >
                      {template.title}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <textarea
              ref={textareaRef}
              name="content"
              id="content"
              rows={15}
              value={formData.content}
              onChange={handleChange}
              className={`shadow-sm block w-full sm:text-sm rounded-md font-mono leading-6 px-4 ${
                errors.content
                  ? 'border-red-300 focus:ring-red-500 focus:border-red-500'
                  : 'border-gray-300 focus:ring-purple-500 focus:border-purple-500'
              }`}
              style={{
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-word',
                resize: 'vertical',
                paddingLeft: '16px',
                paddingRight: '16px',
                boxSizing: 'border-box',
                textIndent: '0',
              }}
              placeholder={t(
                'PromptForm.contentPlaceholder',
                'Enter your prompt content with required placeholders...'
              )}
            />
          </div>

          <FormField
            label={t(
              'PromptForm.makePublic',
              'Make this prompt public (visible to all teachers)'
            )}
            name="isPublic"
            type="checkbox"
            value={formData.isPublic}
            onChange={handleChange}
          />
        </form>
      </div>

      <PlaceholderSidebar
        usedPlaceholders={usedPlaceholders}
        onInsertPlaceholder={insertPlaceholder}
        errors={errors}
      />
    </div>
  );

  return (
    <Modal
      isOpen
      onClose={onCancel}
      title={
        isEditing
          ? t('PromptForm.editPromptTitle', 'Edit Prompt')
          : t('PromptForm.createPromptTitle', 'Create New Prompt')
      }
      size="xl"
      footer={footer}
    >
      {modalContent}
    </Modal>
  );
};

export default PromptForm;
