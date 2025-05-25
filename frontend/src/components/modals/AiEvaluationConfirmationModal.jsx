import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import { useTranslation } from 'react-i18next';

const AiEvaluationConfirmationModal = ({
  isOpen,
  onConfirm,
  onCancel,
  isEnabling,
}) => {
  const { t } = useTranslation();

  const footer = (
    <ModalFooter
      primaryButtonText={
        isEnabling
          ? t('AiEvaluationConfirmationModal.enable', 'Enable')
          : t('AiEvaluationConfirmationModal.disable', 'Disable')
      }
      secondaryButtonText={t('AiEvaluationConfirmationModal.cancel', 'Cancel')}
      onPrimaryClick={onConfirm}
      onSecondaryClick={onCancel}
      primaryButtonClass={
        isEnabling
          ? 'bg-blue-600 hover:bg-blue-700 text-white'
          : 'bg-yellow-600 hover:bg-yellow-700 text-white'
      }
      reverseOrder
    />
  );

  const modalHeader = (
    <div className="bg-gradient-to-r from-purple-600 to-purple-700 px-4 py-5 sm:px-6">
      <div className="flex items-center">
        <div
          className={`mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full sm:mx-0 sm:h-10 sm:w-10 ${isEnabling ? 'bg-purple-400/20' : 'bg-purple-400/20'}`}
        >
          {isEnabling ? (
            <svg
              className="h-6 w-6 text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
              />
            </svg>
          ) : (
            <svg
              className="h-6 w-6 text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          )}
        </div>
        <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
          <h3 className="text-lg leading-6 font-medium text-white">
            {isEnabling
              ? t(
                  'AiEvaluationConfirmationModal.enableTitle',
                  'Enable AI Evaluation'
                )
              : t(
                  'AiEvaluationConfirmationModal.disableTitle',
                  'Disable AI Evaluation'
                )}
          </h3>
        </div>
      </div>
    </div>
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={onCancel}
      size="sm"
      footer={footer}
      showCloseButton={false}
    >
      {modalHeader}
      <div className="mt-2 p-4">
        <p className="text-sm text-gray-500">
          {isEnabling
            ? t(
                'AiEvaluationConfirmationModal.enableDescription',
                'AI evaluation will automatically grade open-ended questions based on the correct answer and any evaluation guidelines provided. Are you sure you want to enable AI evaluation for this test?'
              )
            : t(
                'AiEvaluationConfirmationModal.disableDescription',
                'Disabling AI evaluation means that open-ended questions will require manual grading. Are you sure you want to disable AI evaluation for this test?'
              )}
        </p>
      </div>
    </Modal>
  );
};

export default AiEvaluationConfirmationModal;
