import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import { useTranslation } from 'react-i18next';

const DeleteConfirmationModal = ({
  isOpen,
  title = 'Delete Item',
  description,
  itemName,
  confirmButtonText = 'Delete',
  onConfirm,
  onCancel,
  loading = false,
}) => {
  const { t } = useTranslation();

  const defaultDescription = t(
    'DeleteConfirmationModal.defaultDescription',
    `Are you sure you want to delete ${itemName ? `"${itemName}"` : 'this item'}? This action cannot be undone.`
  );

  const modalTitle = (
    <div className="flex items-center">
      <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10">
        <svg
          className="h-6 w-6 text-red-600"
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
      </div>
      <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
        <h3 className="text-lg leading-6 font-medium text-gray-900">{title}</h3>
      </div>
    </div>
  );

  const footer = (
    <ModalFooter
      primaryButtonText={confirmButtonText}
      secondaryButtonText={t('DeleteConfirmationModal.cancel', 'Cancel')}
      onPrimaryClick={onConfirm}
      onSecondaryClick={onCancel}
      primaryButtonClass="bg-red-600 hover:bg-red-700 text-white"
      loading={loading}
      reverseOrder
    />
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={onCancel}
      size="sm"
      footer={footer}
      showCloseButton={false}
    >
      {modalTitle}
      <div className="mt-4">
        <p className="text-sm text-gray-500">
          {description || defaultDescription}
        </p>
        {itemName && description && (
          <div className="mt-3 p-3 bg-red-50 rounded-md">
            <p className="text-sm font-medium text-red-800">{itemName}</p>
          </div>
        )}
      </div>
    </Modal>
  );
};

export default DeleteConfirmationModal;
