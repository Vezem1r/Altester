import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import { useTranslation } from 'react-i18next';

const PromptViewModal = ({ prompt, onClose }) => {
  const { t } = useTranslation();

  const highlightPlaceholders = text => {
    const placeholders = [
      '{{QUESTION}}',
      '{{CORRECT_ANSWER_SECTION}}',
      '{{STUDENT_ANSWER}}',
      '{{MAX_SCORE}}',
    ];
    let highlightedText = text;

    placeholders.forEach(placeholder => {
      highlightedText = highlightedText.replace(
        new RegExp(placeholder, 'g'),
        `<span class="bg-yellow-200 px-1 rounded">${placeholder}</span>`
      );
    });

    return <div dangerouslySetInnerHTML={{ __html: highlightedText }} />;
  };

  const footer = (
    <ModalFooter
      primaryButtonText={t('PromptViewModal.close', 'Close')}
      onPrimaryClick={onClose}
      primaryButtonClass="bg-gray-600 hover:bg-gray-700 text-white"
      secondaryButtonText={null}
    />
  );

  return (
    <Modal
      isOpen={!!prompt}
      onClose={onClose}
      title={prompt?.title}
      size="lg"
      footer={footer}
    >
      <div className="space-y-4">
        <div>
          <span
            className={`px-2 py-1 text-xs font-medium rounded-full ${
              prompt?.public
                ? 'bg-green-100 text-green-800'
                : 'bg-gray-100 text-gray-800'
            }`}
          >
            {prompt?.public
              ? t('PromptViewModal.public', 'Public')
              : t('PromptViewModal.private', 'Private')}
          </span>
        </div>

        <div>
          <h4 className="text-sm font-medium text-gray-900">
            {t('PromptViewModal.description', 'Description')}
          </h4>
          <p className="mt-1 text-sm text-gray-600">
            {prompt?.description ||
              t('PromptViewModal.noDescription', 'No description provided')}
          </p>
        </div>

        <div>
          <h4 className="text-sm font-medium text-gray-900">
            {t('PromptViewModal.author', 'Author')}
          </h4>
          <p className="mt-1 text-sm text-gray-600">{prompt?.authorUsername}</p>
        </div>

        <div>
          <h4 className="text-sm font-medium text-gray-900">
            {t('PromptViewModal.created', 'Created')}
          </h4>
          <p className="mt-1 text-sm text-gray-600">
            {prompt?.created && new Date(prompt.created).toLocaleString()}
          </p>
        </div>

        {prompt?.lastModified && (
          <div>
            <h4 className="text-sm font-medium text-gray-900">
              {t('PromptViewModal.lastModified', 'Last Modified')}
            </h4>
            <p className="mt-1 text-sm text-gray-600">
              {new Date(prompt.lastModified).toLocaleString()}
            </p>
          </div>
        )}

        <div>
          <h4 className="text-sm font-medium text-gray-900">
            {t('PromptViewModal.promptContent', 'Prompt Content')}
          </h4>
          <div className="mt-2 p-4 bg-gray-50 rounded-md">
            <pre className="whitespace-pre-wrap text-sm font-mono text-gray-800">
              {prompt?.content && highlightPlaceholders(prompt.content)}
            </pre>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default PromptViewModal;
