import Modal from '@/components/ui/Modal';
import ModalFooter from '@/components/ui/ModalFooter';
import { useTranslation } from 'react-i18next';

const TestSettingsModal = ({
  isOpen,
  onClose,
  test,
  userRole,
  onToggleActivity,
  onToggleTeacherEditPermission,
}) => {
  const { t } = useTranslation();

  if (!test) return null;

  const modalHeader = (
    <div className="flex items-center">
      <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-purple-100 sm:mx-0 sm:h-10 sm:w-10">
        <svg
          className="h-6 w-6 text-purple-600"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
          />
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
          />
        </svg>
      </div>
      <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
        <h3 className="text-lg leading-6 font-medium text-gray-900">
          {t('TestSettingsModal.title', 'Test Settings')}
        </h3>
        <p className="text-sm text-gray-500 mt-1">
          {t(
            'TestSettingsModal.subtitle',
            'Manage test settings and permissions'
          )}
        </p>
      </div>
    </div>
  );

  const footer = (
    <ModalFooter
      primaryButtonText={t('TestSettingsModal.close', 'Close')}
      onPrimaryClick={onClose}
      primaryButtonClass="bg-gray-600 hover:bg-gray-700 text-white"
      secondaryButtonText={null}
    />
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      size="md"
      footer={footer}
      showCloseButton={false}
    >
      {modalHeader}

      <div className="mt-4 space-y-4">
        <div className="pt-2 border-t border-gray-200">
          <div className="flex items-center justify-between py-3">
            <div>
              <h4 className="text-sm font-medium text-gray-900">
                {t('TestSettingsModal.testStatus', 'Test Status')}
              </h4>
              <p className="text-xs text-gray-500">
                {test.open
                  ? t(
                      'TestSettingsModal.testOpenStatus',
                      'Test is currently open and available to students'
                    )
                  : t(
                      'TestSettingsModal.testClosedStatus',
                      'Test is currently closed and not available to students'
                    )}
              </p>
            </div>
            <button
              onClick={onToggleActivity}
              className={`inline-flex items-center px-3 py-1.5 border border-transparent rounded-md text-sm font-medium ${
                test.open
                  ? 'bg-red-100 text-red-700 hover:bg-red-200'
                  : 'bg-green-100 text-green-700 hover:bg-green-200'
              }`}
            >
              {test.open
                ? t('TestSettingsModal.closeTest', 'Close Test')
                : t('TestSettingsModal.openTest', 'Open Test')}
            </button>
          </div>
        </div>

        {test.createdByAdmin && userRole === 'ADMIN' && (
          <div className="py-3 border-t border-gray-200">
            <div className="flex items-center justify-between">
              <div>
                <h4 className="text-sm font-medium text-gray-900">
                  {t(
                    'TestSettingsModal.teacherEditPermission',
                    'Teacher Edit Permission'
                  )}
                </h4>
                <p className="text-xs text-gray-500">
                  {test.allowTeacherEdit
                    ? t(
                        'TestSettingsModal.teachersCanEdit',
                        'Teachers can currently edit this admin-created test'
                      )
                    : t(
                        'TestSettingsModal.onlyAdminCanEdit',
                        'Only administrators can edit this test'
                      )}
                </p>
              </div>
              <button
                onClick={onToggleTeacherEditPermission}
                className={`inline-flex items-center px-3 py-1.5 border border-transparent rounded-md text-sm font-medium ${
                  test.allowTeacherEdit
                    ? 'bg-red-100 text-red-700 hover:bg-red-200'
                    : 'bg-green-100 text-green-700 hover:bg-green-200'
                }`}
              >
                {test.allowTeacherEdit
                  ? t(
                      'TestSettingsModal.disableTeacherEdit',
                      'Disable Teacher Edit'
                    )
                  : t(
                      'TestSettingsModal.enableTeacherEdit',
                      'Enable Teacher Edit'
                    )}
              </button>
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
};

export default TestSettingsModal;
