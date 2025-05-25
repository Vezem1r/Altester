import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

const AssignKeyTab = ({
  selectedKey,
  setSelectedKey,
  availableKeys,
  selectedGroup,
  setSelectedGroup,
  associatedGroups,
  handleAssignKey,
  setTabView,
  getServiceDisplayName,
  loading,
}) => {
  const { t } = useTranslation();

  const groupOptions = useMemo(() => {
    return associatedGroups.map(group => (
      <option key={group.id} value={group.id}>
        {group.name}
      </option>
    ));
  }, [associatedGroups]);

  const keyOptions = useMemo(() => {
    return availableKeys.map(key => (
      <option key={key.id} value={key.id}>
        {key.name} ({getServiceDisplayName(key.aiServiceName)} • {key.model})
      </option>
    ));
  }, [availableKeys, getServiceDisplayName]);

  return (
    <div className="space-y-6">
      <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded-r-md">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg
              className="h-5 w-5 text-blue-500"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-blue-800">
              {t(
                'assignKeyTab.infoMessage',
                'Assign API keys to enable AI evaluation for specific groups.'
              )}
            </p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-lg p-6 space-y-6 border border-gray-200">
        <div className="bg-gradient-to-br from-purple-50 via-indigo-50 to-blue-50 p-6 rounded-lg border border-purple-100 shadow-sm">
          <h3 className="text-lg font-semibold text-purple-800 mb-4 flex items-center">
            <svg
              className="w-5 h-5 mr-2 text-purple-600"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
              />
            </svg>
            {t('assignKeyTab.assignApiKey', 'Assign an API Key')}
          </h3>

          <div className="space-y-6">
            <div className="transition-all duration-200 hover:translate-y-[-2px]">
              <label className="text-sm font-medium text-gray-700 mb-2 flex items-center">
                <svg
                  className="w-4 h-4 mr-1 text-purple-600"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
                  />
                </svg>
                {t('assignKeyTab.selectApiKey', 'Select API Key')}
              </label>
              <div className="relative group">
                <select
                  value={selectedKey}
                  onChange={e => setSelectedKey(e.target.value)}
                  className="appearance-none w-full px-4 py-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white shadow-sm transition-all duration-200 group-hover:border-purple-300"
                >
                  <option value="">
                    {t(
                      'assignKeyTab.selectApiKeyOption',
                      '-- Select an API Key --'
                    )}
                  </option>
                  {keyOptions}
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
                  <svg
                    className="fill-current h-4 w-4 text-purple-500"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 20 20"
                  >
                    <path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z" />
                  </svg>
                </div>
              </div>
              {availableKeys.length === 0 && (
                <p className="mt-2 text-sm text-yellow-600 bg-yellow-50 p-2 rounded border border-yellow-200">
                  {t(
                    'assignKeyTab.noApiKeysAvailable',
                    'No active API keys available. Please create one first in API Key Management.'
                  )}
                </p>
              )}
            </div>

            <div className="transition-all duration-200 hover:translate-y-[-2px]">
              <label className="text-sm font-medium text-gray-700 mb-2 flex items-center">
                <svg
                  className="w-4 h-4 mr-1 text-purple-600"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
                {t('assignKeyTab.selectGroup', 'Select Group')}{' '}
                <span className="text-red-500">*</span>
              </label>
              <div className="relative group">
                <select
                  value={selectedGroup}
                  onChange={e => setSelectedGroup(e.target.value)}
                  className="appearance-none w-full px-4 py-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white shadow-sm transition-all duration-200 group-hover:border-purple-300"
                  required
                >
                  <option value="">
                    {t(
                      'assignKeyTab.selectGroupOption',
                      '-- Select a Group --'
                    )}
                  </option>
                  {groupOptions}
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
                  <svg
                    className="fill-current h-4 w-4 text-purple-500"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 20 20"
                  >
                    <path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z" />
                  </svg>
                </div>
              </div>

              {associatedGroups.length === 0 && (
                <p className="mt-2 text-sm text-yellow-600 bg-yellow-50 p-2 rounded border border-yellow-200">
                  {t(
                    'assignKeyTab.noGroupsAssociated',
                    'No groups associated with this test. Please add groups to the test first.'
                  )}
                </p>
              )}
            </div>
          </div>
        </div>

        <div className="flex justify-end space-x-3 pt-6 border-t">
          <button
            type="button"
            onClick={() => setTabView('overview')}
            className="px-6 py-2.5 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors duration-150 shadow-sm hover:shadow"
          >
            {t('assignKeyTab.cancel', 'Cancel')}
          </button>
          <button
            type="button"
            onClick={handleAssignKey}
            disabled={!selectedKey || !selectedGroup || loading}
            className={`px-6 py-2.5 rounded-lg font-medium text-white transition-all duration-200 shadow-sm ${
              !selectedKey || !selectedGroup || loading
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-purple-600 hover:bg-purple-700 hover:shadow'
            }`}
          >
            {loading ? (
              <span className="flex items-center">
                <svg
                  className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                {t('assignKeyTab.assigning', 'Assigning...')}
              </span>
            ) : (
              t('assignKeyTab.assignApiKey', 'Assign an API Key')
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AssignKeyTab;
