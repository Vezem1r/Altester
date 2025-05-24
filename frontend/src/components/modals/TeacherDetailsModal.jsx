import { useState } from 'react';
import { AdminService } from '@/services/AdminService';
import { toast } from 'react-toastify';
import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import FormField from '@/components/ui/FormField';
import { DetailsList } from '@/components/ui/DetailsList';
import DeleteConfirmationModal from './DeleteConfirmationModal';
import { useTranslation } from 'react-i18next';

const TeacherDetailsModal = ({
  teacher,
  closeModal,
  onUpdate,
  directEdit = false,
}) => {
  const { t } = useTranslation();
  const [isEditing, setIsEditing] = useState(directEdit);
  const [showPromoteConfirm, setShowPromoteConfirm] = useState(false);
  const [formData, setFormData] = useState({
    name: teacher.firstName,
    lastname: teacher.lastName,
    email: teacher.email,
    username: teacher.username,
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
    const validationErrors = {};

    if (!formData.name) {
      validationErrors.name = t(
        'TeacherDetailsModal.firstNameRequired',
        'First name is required'
      );
    } else if (formData.name.length > 63) {
      validationErrors.name = t(
        'TeacherDetailsModal.firstNameTooLong',
        'First name cannot exceed 63 characters'
      );
    }

    if (!formData.lastname) {
      validationErrors.lastname = t(
        'TeacherDetailsModal.lastNameRequired',
        'Last name is required'
      );
    } else if (formData.lastname.length > 127) {
      validationErrors.lastname = t(
        'TeacherDetailsModal.lastNameTooLong',
        'Last name cannot exceed 127 characters'
      );
    }

    if (!formData.email) {
      validationErrors.email = t(
        'TeacherDetailsModal.emailRequired',
        'Email is required'
      );
    } else if (formData.email.length > 63) {
      validationErrors.email = t(
        'TeacherDetailsModal.emailTooLong',
        'Email cannot exceed 63 characters'
      );
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      validationErrors.email = t(
        'TeacherDetailsModal.emailInvalid',
        'Please enter a valid email address'
      );
    }

    if (!formData.username) {
      validationErrors.username = t(
        'TeacherDetailsModal.usernameRequired',
        'Username is required'
      );
    } else if (formData.username.length > 7) {
      validationErrors.username = t(
        'TeacherDetailsModal.usernameTooLong',
        'Username cannot exceed 7 characters'
      );
    }

    setErrors(validationErrors);
    return Object.keys(validationErrors).length === 0;
  };

  const handleSubmit = async e => {
    e.preventDefault();
    if (!validateForm()) {
      return;
    }

    try {
      await AdminService.updateUser(teacher.username, formData);
      toast.success(
        t(
          'TeacherDetailsModal.updateSuccess',
          'Teacher information updated successfully'
        )
      );
      setIsEditing(false);

      if (onUpdate) {
        onUpdate();
      }
    } catch (error) {
      toast.error(
        error.message ||
          t(
            'TeacherDetailsModal.updateError',
            'Error updating teacher information'
          )
      );
    }
  };

  const handlePromoteToStudent = async () => {
    try {
      await AdminService.promoteToStudent(teacher.username);
      toast.success(
        t(
          'TeacherDetailsModal.demoteSuccess',
          'Teacher successfully demoted to student role'
        )
      );
      setShowPromoteConfirm(false);
      closeModal();

      if (onUpdate) {
        onUpdate();
      }
    } catch (error) {
      toast.error(
        error.message ||
          t('TeacherDetailsModal.demoteError', 'Error demoting teacher')
      );
    }
  };

  const renderList = items => {
    if (!items || items.length === 0) {
      return (
        <span className="text-gray-500 italic">
          {t('TeacherDetailsModal.none', 'None')}
        </span>
      );
    }

    return (
      <div className="flex flex-wrap gap-1">
        {items.map((item, index) => (
          <span
            key={index}
            className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800"
          >
            {item}
          </span>
        ))}
      </div>
    );
  };

  const detailsItems = [
    {
      label: t('TeacherDetailsModal.username', 'Username'),
      value: teacher.username,
    },
    {
      label: t('TeacherDetailsModal.lastLogin', 'Last Login'),
      value: teacher.lastLogin
        ? new Date(teacher.lastLogin).toLocaleString()
        : t('TeacherDetailsModal.neverLoggedIn', 'Never logged in'),
    },
    {
      label: t('TeacherDetailsModal.status', 'Status'),
      value: teacher.registered
        ? t('TeacherDetailsModal.registered', 'Registered')
        : t('TeacherDetailsModal.ldapUser', 'LDAP User'),
    },
    {
      label: t('TeacherDetailsModal.role', 'Role'),
      value: t('TeacherDetailsModal.teacher', 'Teacher'),
    },
  ];

  const getFooter = () => {
    if (isEditing) {
      return (
        <ModalFooter
          primaryButtonText={t(
            'TeacherDetailsModal.saveChanges',
            'Save Changes'
          )}
          secondaryButtonText={t('TeacherDetailsModal.cancel', 'Cancel')}
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
          className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-purple-600 text-base font-medium text-white hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 sm:ml-3 sm:w-auto sm:text-sm"
          onClick={closeModal}
        >
          {t('TeacherDetailsModal.close', 'Close')}
        </button>

        {teacher.registered && (
          <button
            type="button"
            className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:mr-3 sm:w-auto sm:text-sm"
            onClick={() => setIsEditing(true)}
          >
            {t('TeacherDetailsModal.edit', 'Edit')}
          </button>
        )}

        <button
          type="button"
          className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-indigo-600 hover:text-indigo-800 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:w-auto sm:text-sm"
          onClick={() => setShowPromoteConfirm(true)}
        >
          {t('TeacherDetailsModal.demoteToStudent', 'Demote to Student')}
        </button>
      </div>
    );
  };

  const promotionNote = teacher.groupNames && teacher.groupNames.length > 0 && (
    <div className="mt-3 p-3 bg-amber-50 border border-amber-200 rounded-md">
      <p className="text-sm text-amber-700 font-medium">
        {t(
          'TeacherDetailsModal.demoteWarning',
          'Important: This teacher will be removed as an instructor from all groups and subjects they are currently teaching.'
        )}
      </p>
    </div>
  );

  return (
    <>
      <Modal
        isOpen
        onClose={closeModal}
        title={
          isEditing
            ? t('TeacherDetailsModal.editTitle', 'Edit Teacher Details')
            : t('TeacherDetailsModal.viewTitle', 'Teacher Details')
        }
        size="md"
        footer={getFooter()}
      >
        {isEditing ? (
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-2">
              <FormField
                label={t('TeacherDetailsModal.firstName', 'First Name')}
                name="name"
                value={formData.name}
                onChange={handleChange}
                maxLength={63}
                error={errors.name}
                helperText={t(
                  'TeacherDetailsModal.max63chars',
                  '(max 63 chars)'
                )}
              />

              <FormField
                label={t('TeacherDetailsModal.lastName', 'Last Name')}
                name="lastname"
                value={formData.lastname}
                onChange={handleChange}
                maxLength={127}
                error={errors.lastname}
                helperText={t(
                  'TeacherDetailsModal.max127chars',
                  '(max 127 chars)'
                )}
              />
            </div>

            <FormField
              label={t('TeacherDetailsModal.email', 'Email')}
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              maxLength={63}
              error={errors.email}
              helperText={t('TeacherDetailsModal.max63chars', '(max 63 chars)')}
            />

            <FormField
              label={t('TeacherDetailsModal.username', 'Username')}
              name="username"
              value={formData.username}
              onChange={handleChange}
              maxLength={7}
              error={errors.username}
              helperText={t('TeacherDetailsModal.max7chars', '(max 7 chars)')}
            />
          </form>
        ) : (
          <div>
            <div className="flex items-center mb-4">
              <div className="h-16 w-16 rounded-full bg-purple-100 flex items-center justify-center">
                <span className="text-xl font-medium text-purple-600">
                  {`${teacher.firstName.charAt(0)}${teacher.lastName.charAt(0)}`}
                </span>
              </div>
              <div className="ml-4">
                <h4 className="text-xl font-bold">{`${teacher.firstName} ${teacher.lastName}`}</h4>
                <p className="text-gray-500">{teacher.email}</p>
                {!teacher.registered && (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 mt-1">
                    {t('TeacherDetailsModal.ldapUser', 'LDAP User')}
                  </span>
                )}
              </div>
            </div>

            <div className="mt-6 bg-gray-50 p-4 rounded-lg">
              <DetailsList items={detailsItems} columns={2} />
            </div>

            <div className="mt-6">
              <h4 className="text-base font-medium text-gray-900 mb-4">
                {t(
                  'TeacherDetailsModal.teachingAssignments',
                  'Teaching Assignments'
                )}
              </h4>

              <div className="space-y-4">
                <div>
                  <h5 className="text-sm font-medium text-gray-700 mb-2">
                    {t('TeacherDetailsModal.groups', 'Groups')}
                  </h5>
                  {renderList(teacher.groupNames)}
                </div>

                <div>
                  <h5 className="text-sm font-medium text-gray-700 mb-2">
                    {t('TeacherDetailsModal.subjects', 'Subjects')}
                  </h5>
                  {renderList(teacher.subjectShortNames)}
                </div>
              </div>
            </div>
          </div>
        )}
      </Modal>

      <DeleteConfirmationModal
        isOpen={showPromoteConfirm}
        title={t(
          'TeacherDetailsModal.demoteToStudentRole',
          'Demote to Student Role'
        )}
        description={t(
          'TeacherDetailsModal.demoteConfirmation',
          `Are you sure you want to demote ${teacher.firstName} ${teacher.lastName} to a student role? This will change their permissions and access levels.`
        )}
        itemName={null}
        confirmButtonText={t('TeacherDetailsModal.demote', 'Demote')}
        onConfirm={handlePromoteToStudent}
        onCancel={() => setShowPromoteConfirm(false)}
      />
      {showPromoteConfirm && promotionNote}
    </>
  );
};

export default TeacherDetailsModal;
