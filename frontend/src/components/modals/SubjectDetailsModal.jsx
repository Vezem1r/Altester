import { useState } from 'react';
import { Link } from 'react-router-dom';
import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import FormField from '@/components/ui/FormField';
import DeleteConfirmationModal from './DeleteConfirmationModal';
import { useTranslation } from 'react-i18next';

const SubjectDetailsModal = ({ subject, closeModal, onUpdate, onDelete }) => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    name: subject.name,
    shortName: subject.shortName,
    description: subject.description || '',
  });

  const [isEditing, setIsEditing] = useState(false);
  const [errors, setErrors] = useState({});
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

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
        'SubjectDetailsModal.nameRequired',
        'Subject name is required'
      );
    }

    if (!formData.shortName.trim()) {
      newErrors.shortName = t(
        'SubjectDetailsModal.shortNameRequired',
        'Short name is required'
      );
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = e => {
    e.preventDefault();
    if (validateForm()) {
      onUpdate(subject.id, formData);
    }
  };

  const handleDeleteConfirm = () => {
    onDelete(subject.id);
    setShowDeleteConfirm(false);
  };

  const formatDate = dateString => {
    if (!dateString) return null;

    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        return null;
      }

      const options = {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      };

      return date.toLocaleDateString(undefined, options);
    } catch (e) {
      return dateString;
    }
  };

  const getGroupStatusBadge = group => {
    if (group.status === 'Active') {
      return (
        <span className="ml-2 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800">
          {t('SubjectDetailsModal.statusActive', 'Active')}
        </span>
      );
    } else if (group.status === 'Future') {
      return (
        <span className="ml-2 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
          {t('SubjectDetailsModal.statusFuture', 'Future')}
        </span>
      );
    }
    return null;
  };

  const formattedDate = formatDate(subject.modified);

  const modalHeader = (
    <div className="flex items-center">
      <div className="mx-auto flex-shrink-0 flex items-center justify-center h-16 w-16 rounded-full bg-purple-100 sm:mx-0">
        <svg
          className="h-10 w-10 text-purple-600"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
          />
        </svg>
      </div>
      <div className="mt-3 text-center sm:mt-0 sm:ml-6 sm:text-left">
        <h3 className="text-2xl leading-6 font-bold text-gray-900">
          {isEditing
            ? t('SubjectDetailsModal.editSubject', 'Edit Subject')
            : subject.name}
        </h3>
      </div>
    </div>
  );

  const getFooter = () => {
    if (isEditing) {
      return (
        <ModalFooter
          primaryButtonText={t(
            'SubjectDetailsModal.saveChanges',
            'Save Changes'
          )}
          secondaryButtonText={t('SubjectDetailsModal.cancel', 'Cancel')}
          onPrimaryClick={handleSubmit}
          onSecondaryClick={() => setIsEditing(false)}
          reverseOrder
        />
      );
    }

    return (
      <div className="sm:flex sm:flex-row-reverse sm:justify-start">
        <button
          type="button"
          className="inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-sm font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 sm:ml-3 sm:w-auto"
          onClick={() => setShowDeleteConfirm(true)}
        >
          {t('SubjectDetailsModal.delete', 'Delete')}
        </button>
        <button
          type="button"
          className="inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-purple-600 text-sm font-medium text-white hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 sm:ml-3 sm:w-auto"
          onClick={() => setIsEditing(true)}
        >
          {t('SubjectDetailsModal.edit', 'Edit')}
        </button>
        <button
          type="button"
          className="mt-3 inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 sm:mt-0 sm:ml-0 sm:w-auto"
          onClick={closeModal}
        >
          {t('SubjectDetailsModal.close', 'Close')}
        </button>
      </div>
    );
  };

  return (
    <>
      <Modal
        isOpen
        onClose={closeModal}
        size="md"
        footer={getFooter()}
        showCloseButton={false}
      >
        {modalHeader}

        {isEditing ? (
          <form onSubmit={handleSubmit} className="mt-4 space-y-4">
            <FormField
              label={t('SubjectDetailsModal.subjectName', 'Subject Name')}
              name="name"
              value={formData.name}
              onChange={handleChange}
              error={errors.name}
            />

            <FormField
              label={t('SubjectDetailsModal.shortName', 'Short Name')}
              name="shortName"
              value={formData.shortName}
              onChange={handleChange}
              error={errors.shortName}
              helperText={t(
                'SubjectDetailsModal.shortNameHelp',
                '(Will be converted to uppercase)'
              )}
            />

            <FormField
              label={t('SubjectDetailsModal.description', 'Description')}
              name="description"
              type="textarea"
              value={formData.description}
              onChange={handleChange}
              placeholder={t(
                'SubjectDetailsModal.descriptionPlaceholder',
                'Enter subject description...'
              )}
              rows={4}
            />
          </form>
        ) : (
          <div className="mt-4">
            <div className="flex items-center mb-6">
              <span className="mr-2 inline-flex items-center px-2.5 py-1 rounded-md text-sm font-medium bg-purple-100 text-purple-800">
                {subject.shortName}
              </span>
              {formattedDate && (
                <span className="text-sm text-gray-500">
                  {t('SubjectDetailsModal.lastUpdated', 'Last updated')}:{' '}
                  {formattedDate}
                </span>
              )}
            </div>

            <div className="bg-gray-50 p-4 rounded-xl shadow-inner">
              {subject.description ? (
                <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">
                  {subject.description}
                </p>
              ) : (
                <p className="text-sm text-gray-500 italic">
                  {t(
                    'SubjectDetailsModal.noDescription',
                    'No description provided'
                  )}
                </p>
              )}
            </div>

            <div className="mt-6 border-t border-gray-200 pt-4">
              <h4 className="text-base font-medium text-gray-900 mb-3">
                {t('SubjectDetailsModal.assignedGroups', 'Assigned Groups')}
              </h4>
              {subject.groups && subject.groups.length > 0 ? (
                <div className="grid gap-2 grid-cols-1 sm:grid-cols-2 md:grid-cols-3">
                  {subject.groups.map(group => (
                    <Link
                      key={group.id}
                      to={`/admin/groups/${group.id}`}
                      className="flex items-center p-2 border border-gray-200 rounded-lg hover:bg-purple-50 hover:border-purple-200 transition-colors cursor-pointer"
                      onClick={closeModal}
                    >
                      <div className="h-6 w-6 flex-shrink-0 rounded-full bg-purple-100 flex items-center justify-center mr-2">
                        <svg
                          className="h-3 w-3 text-purple-600"
                          xmlns="http://www.w3.org/2000/svg"
                          viewBox="0 0 20 20"
                          fill="currentColor"
                        >
                          <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z" />
                        </svg>
                      </div>
                      <div className="min-w-0 flex-grow">
                        <div className="text-xs font-medium text-gray-900 truncate flex items-center">
                          {group.name}
                          {getGroupStatusBadge(group)}
                        </div>
                      </div>
                    </Link>
                  ))}
                </div>
              ) : (
                <div className="bg-gray-50 rounded-lg p-4 text-center">
                  <svg
                    className="h-8 w-8 text-gray-400 mx-auto mb-2"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                    />
                  </svg>
                  <p className="text-xs text-gray-500">
                    {t(
                      'SubjectDetailsModal.noGroups',
                      'No groups assigned to this subject'
                    )}
                  </p>
                </div>
              )}
            </div>
          </div>
        )}
      </Modal>

      <DeleteConfirmationModal
        isOpen={showDeleteConfirm}
        title={t('SubjectDetailsModal.deleteSubject', 'Delete Subject')}
        description={t(
          'SubjectDetailsModal.deleteConfirmation',
          `Are you sure you want to delete this subject? This action cannot be undone and may affect ${subject.groups?.length || 0} group(s) assigned to this subject.`
        )}
        itemName={subject.name}
        confirmButtonText={t(
          'SubjectDetailsModal.deleteSubject',
          'Delete Subject'
        )}
        onConfirm={handleDeleteConfirm}
        onCancel={() => setShowDeleteConfirm(false)}
      />
    </>
  );
};

export default SubjectDetailsModal;
