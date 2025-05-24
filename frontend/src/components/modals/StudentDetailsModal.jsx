import { useState } from 'react';
import { AdminService } from '@/services/AdminService';
import { toast } from 'react-toastify';
import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import FormField from '@/components/ui/FormField';
import { DetailsList } from '@/components/ui/DetailsList';
import DeleteConfirmationModal from './DeleteConfirmationModal';
import StudentAttempts from '@/components/common/StudentAttempts';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';

const StudentDetailsModal = ({
  student,
  closeModal,
  onUpdate,
  directEdit = false,
  readOnly = false,
}) => {
  const { t } = useTranslation();
  const { userRole } = useAuth();
  const isTeacherRole = userRole === 'TEACHER' || readOnly;

  const [isEditing, setIsEditing] = useState(directEdit && !isTeacherRole);
  const [showPromoteConfirm, setShowPromoteConfirm] = useState(false);
  const [activeTab, setActiveTab] = useState('info');
  const [formData, setFormData] = useState({
    name: student.firstName,
    lastname: student.lastName,
    email: student.email,
    username: student.username,
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
        'StudentDetailsModal.firstNameRequired',
        'First name is required'
      );
    } else if (formData.name.length > 63) {
      validationErrors.name = t(
        'StudentDetailsModal.firstNameTooLong',
        'First name cannot exceed 63 characters'
      );
    }

    if (!formData.lastname) {
      validationErrors.lastname = t(
        'StudentDetailsModal.lastNameRequired',
        'Last name is required'
      );
    } else if (formData.lastname.length > 127) {
      validationErrors.lastname = t(
        'StudentDetailsModal.lastNameTooLong',
        'Last name cannot exceed 127 characters'
      );
    }

    if (!formData.email) {
      validationErrors.email = t(
        'StudentDetailsModal.emailRequired',
        'Email is required'
      );
    } else if (formData.email.length > 63) {
      validationErrors.email = t(
        'StudentDetailsModal.emailTooLong',
        'Email cannot exceed 63 characters'
      );
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      validationErrors.email = t(
        'StudentDetailsModal.emailInvalid',
        'Please enter a valid email address'
      );
    }

    if (!formData.username) {
      validationErrors.username = t(
        'StudentDetailsModal.usernameRequired',
        'Username is required'
      );
    } else if (formData.username.length > 7) {
      validationErrors.username = t(
        'StudentDetailsModal.usernameTooLong',
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
      await AdminService.updateUser(student.username, formData);
      toast.success(
        t(
          'StudentDetailsModal.updateSuccess',
          'Student information updated successfully'
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
            'StudentDetailsModal.updateError',
            'Error updating student information'
          )
      );
    }
  };

  const handlePromoteToTeacher = async () => {
    try {
      await AdminService.promoteToTeacher(student.username);
      toast.success(
        t(
          'StudentDetailsModal.promoteSuccess',
          'Student successfully promoted to teacher role'
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
          t('StudentDetailsModal.promoteError', 'Error promoting student')
      );
    }
  };

  const renderStudentGroups = groups => {
    if (!groups || groups.length === 0) {
      return (
        <span className="text-gray-500 italic">
          {t('StudentDetailsModal.none', 'None')}
        </span>
      );
    }

    return (
      <div className="flex flex-wrap gap-1">
        {groups.map((item, index) => (
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

  const renderList = items => {
    if (!items || items.length === 0) {
      return (
        <span className="text-gray-500 italic">
          {t('StudentDetailsModal.none', 'None')}
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
      label: t('StudentDetailsModal.username', 'Username'),
      value: student.username,
    },
    {
      label: t('StudentDetailsModal.lastLogin', 'Last Login'),
      value: student.lastLogin
        ? new Date(student.lastLogin).toLocaleString()
        : t('StudentDetailsModal.neverLoggedIn', 'Never logged in'),
    },
    ...(!isTeacherRole
      ? [
          {
            label: t('StudentDetailsModal.status', 'Status'),
            value: student.registered
              ? t('StudentDetailsModal.statusRegistered', 'Registered')
              : t('StudentDetailsModal.statusLdap', 'LDAP User'),
          },
        ]
      : []),
    {
      label: t('StudentDetailsModal.role', 'Role'),
      value: t('StudentDetailsModal.roleStudent', 'Student'),
    },
  ];

  const getFooter = () => {
    if (isEditing) {
      return (
        <ModalFooter
          primaryButtonText={t(
            'StudentDetailsModal.saveChanges',
            'Save Changes'
          )}
          secondaryButtonText={t('StudentDetailsModal.cancel', 'Cancel')}
          onPrimaryClick={handleSubmit}
          onSecondaryClick={() => setIsEditing(false)}
          reverseOrder
        />
      );
    }

    if (isTeacherRole) {
      return (
        <div className="sm:flex sm:flex-row-reverse sm:justify-start">
          <button
            type="button"
            className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-purple-600 text-base font-medium text-white hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 sm:ml-3 sm:w-auto sm:text-sm"
            onClick={closeModal}
          >
            {t('StudentDetailsModal.close', 'Close')}
          </button>
        </div>
      );
    }

    return (
      <div className="sm:flex sm:flex-row-reverse sm:justify-start">
        <button
          type="button"
          className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-purple-600 text-base font-medium text-white hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 sm:ml-3 sm:w-auto sm:text-sm"
          onClick={closeModal}
        >
          {t('StudentDetailsModal.close', 'Close')}
        </button>

        {student.registered && (
          <button
            type="button"
            className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:mr-3 sm:w-auto sm:text-sm"
            onClick={() => setIsEditing(true)}
          >
            {t('StudentDetailsModal.edit', 'Edit')}
          </button>
        )}

        <button
          type="button"
          className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-indigo-600 hover:text-indigo-800 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:w-auto sm:text-sm"
          onClick={() => setShowPromoteConfirm(true)}
        >
          {t('StudentDetailsModal.promoteToTeacher', 'Promote to Teacher')}
        </button>
      </div>
    );
  };

  const promotionNote = student.groupNames && student.groupNames.length > 0 && (
    <div className="mt-3 p-3 bg-amber-50 border border-amber-200 rounded-md">
      <p className="text-sm text-amber-700 font-medium">
        {t(
          'StudentDetailsModal.promotionWarning',
          'Important: This student will be removed from all student groups they are currently enrolled in.'
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
            ? t('StudentDetailsModal.editTitle', 'Edit Student Details')
            : t('StudentDetailsModal.viewTitle', 'Student Details')
        }
        size="lg"
        footer={getFooter()}
        className="sm:max-w-3xl"
      >
        {isEditing ? (
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-2">
              <FormField
                label={t('StudentDetailsModal.firstName', 'First Name')}
                name="name"
                value={formData.name}
                onChange={handleChange}
                maxLength={63}
                error={errors.name}
                helperText={t(
                  'StudentDetailsModal.max63chars',
                  '(max 63 chars)'
                )}
              />

              <FormField
                label={t('StudentDetailsModal.lastName', 'Last Name')}
                name="lastname"
                value={formData.lastname}
                onChange={handleChange}
                maxLength={127}
                error={errors.lastname}
                helperText={t(
                  'StudentDetailsModal.max127chars',
                  '(max 127 chars)'
                )}
              />
            </div>

            <FormField
              label={t('StudentDetailsModal.email', 'Email')}
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              maxLength={63}
              error={errors.email}
              helperText={t('StudentDetailsModal.max63chars', '(max 63 chars)')}
            />

            <FormField
              label={t('StudentDetailsModal.username', 'Username')}
              name="username"
              value={formData.username}
              onChange={handleChange}
              maxLength={7}
              error={errors.username}
              helperText={t('StudentDetailsModal.max7chars', '(max 7 chars)')}
            />
          </form>
        ) : (
          <div>
            <div className="flex items-center mb-4">
              <div className="h-16 w-16 rounded-full bg-purple-100 flex items-center justify-center">
                <span className="text-xl font-medium text-purple-600">
                  {`${student.firstName.charAt(0)}${student.lastName.charAt(0)}`}
                </span>
              </div>
              <div className="ml-4">
                <h4 className="text-xl font-bold">{`${student.firstName} ${student.lastName}`}</h4>
                <p className="text-gray-500">{student.email}</p>
                {!isTeacherRole && !student.registered && (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 mt-1">
                    {t('StudentDetailsModal.ldapUser', 'LDAP User')}
                  </span>
                )}
              </div>
            </div>

            {!isTeacherRole && (
              <div className="mt-6 border-b border-gray-200">
                <nav className="-mb-px flex space-x-6">
                  <button
                    onClick={() => setActiveTab('info')}
                    className={`${
                      activeTab === 'info'
                        ? 'border-purple-500 text-purple-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    } whitespace-nowrap py-3 px-1 border-b-2 font-medium text-sm`}
                  >
                    {t('StudentDetailsModal.studentInfoTab', 'Student Info')}
                  </button>
                  <button
                    onClick={() => setActiveTab('attempts')}
                    className={`${
                      activeTab === 'attempts'
                        ? 'border-purple-500 text-purple-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    } whitespace-nowrap py-3 px-1 border-b-2 font-medium text-sm`}
                  >
                    {t('StudentDetailsModal.testAttemptsTab', 'Test Attempts')}
                  </button>
                </nav>
              </div>
            )}

            {(activeTab === 'info' || isTeacherRole) && (
              <div className="mt-4">
                <div className="bg-gray-50 p-4 rounded-lg">
                  <DetailsList items={detailsItems} columns={2} />
                </div>

                <div className="mt-6">
                  <h4 className="text-base font-medium text-gray-900 mb-4">
                    {t('StudentDetailsModal.enrollments', 'Enrollments')}
                  </h4>

                  <div className="space-y-4">
                    <div>
                      <h5 className="text-sm font-medium text-gray-700 mb-2">
                        {t('StudentDetailsModal.groups', 'Groups')}
                      </h5>
                      {isTeacherRole && student.subjectGroups
                        ? renderStudentGroups(
                            student.subjectGroups.map(g => g.name)
                          )
                        : renderStudentGroups(student.groupNames)}
                    </div>

                    {!isTeacherRole && (
                      <div>
                        <h5 className="text-sm font-medium text-gray-700 mb-2">
                          {t('StudentDetailsModal.subjects', 'Subjects')}
                        </h5>
                        {renderList(student.subjectShortNames)}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'attempts' && !isTeacherRole && (
              <StudentAttempts student={student} />
            )}
          </div>
        )}
      </Modal>

      {!isTeacherRole && (
        <DeleteConfirmationModal
          isOpen={showPromoteConfirm}
          title={t(
            'StudentDetailsModal.promoteToTeacherRole',
            'Promote to Teacher Role'
          )}
          description={t(
            'StudentDetailsModal.promoteConfirmText',
            `Are you sure you want to promote ${student.firstName} ${student.lastName} to a teacher role? This will change their permissions and access levels.`
          )}
          itemName={null}
          confirmButtonText={t('StudentDetailsModal.promote', 'Promote')}
          onConfirm={handlePromoteToTeacher}
          onCancel={() => setShowPromoteConfirm(false)}
        />
      )}
      {!isTeacherRole && showPromoteConfirm && promotionNote}
    </>
  );
};

export default StudentDetailsModal;
