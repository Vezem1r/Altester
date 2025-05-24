import { useState } from 'react';
import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import FormField from '@/components/ui/FormField';
import { useTranslation } from 'react-i18next';

const CreateSubjectModal = ({ closeModal, onSubmit }) => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    name: '',
    shortName: '',
    description: '',
  });
  const [errors, setErrors] = useState({});

  const handleChange = e => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });

    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: null,
      });
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.name.trim()) {
      newErrors.name = t(
        'CreateSubjectModal.nameRequired',
        'Subject name is required'
      );
    }

    if (!formData.shortName.trim()) {
      newErrors.shortName = t(
        'CreateSubjectModal.shortNameRequired',
        'Short name is required'
      );
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = e => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
    }
  };

  const modalHeader = (
    <div className="flex items-center">
      <div className="mx-auto flex-shrink-0 flex items-center justify-center h-14 w-14 rounded-full bg-purple-100 sm:mx-0">
        <svg
          className="h-8 w-8 text-purple-600"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 6v6m0 0v6m0-6h6m-6 0H6"
          />
        </svg>
      </div>
      <div className="mt-3 text-center sm:mt-0 sm:ml-6 sm:text-left">
        <h3 className="text-2xl leading-6 font-bold text-gray-900">
          {t('CreateSubjectModal.title', 'Create New Subject')}
        </h3>
      </div>
    </div>
  );

  const footer = (
    <ModalFooter
      primaryButtonText={t(
        'CreateSubjectModal.createSubject',
        'Create Subject'
      )}
      secondaryButtonText={t('CreateSubjectModal.cancel', 'Cancel')}
      onPrimaryClick={handleSubmit}
      onSecondaryClick={closeModal}
      reverseOrder
    />
  );

  return (
    <Modal
      isOpen
      onClose={closeModal}
      size="md"
      footer={footer}
      showCloseButton={false}
    >
      {modalHeader}
      <form onSubmit={handleSubmit} className="mt-6 space-y-6">
        <FormField
          label={t('CreateSubjectModal.subjectName', 'Subject Name')}
          name="name"
          value={formData.name}
          onChange={handleChange}
          maxLength={63}
          placeholder={t(
            'CreateSubjectModal.enterSubjectName',
            'Enter subject name'
          )}
          error={errors.name}
          required
        />

        <FormField
          label={t('CreateSubjectModal.shortName', 'Short Name')}
          name="shortName"
          value={formData.shortName}
          onChange={handleChange}
          maxLength={6}
          placeholder={t(
            'CreateSubjectModal.shortNameExample',
            'e.g. MATH, ENG, SCI'
          )}
          helperText={t(
            'CreateSubjectModal.shortNameHelp',
            '(Will be converted to uppercase)'
          )}
          error={errors.shortName}
          required
        />

        <FormField
          label={t('CreateSubjectModal.description', 'Description')}
          name="description"
          type="textarea"
          value={formData.description}
          onChange={handleChange}
          maxLength={255}
          placeholder={t(
            'CreateSubjectModal.descriptionPlaceholder',
            'Provide a description of the subject'
          )}
          rows={4}
        />
      </form>
    </Modal>
  );
};

export default CreateSubjectModal;
