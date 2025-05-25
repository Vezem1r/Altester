import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

const ManageAiTab = ({
  associatedGroups,
  getGroupInfo,
  apiKeys,
  handleToggleAiEvaluation,
  setSelectedGroup,
  setTabView,
  getAiServiceIcon,
  getServiceDisplayName,
}) => {
  const { t } = useTranslation();

  const groupRows = useMemo(() => {
    return associatedGroups.map((group, index) => {
      const groupInfo = getGroupInfo(group.id);
      const assignedKey = apiKeys.find(
        key => key.group && key.group.groupId === group.id
      );

      return (
        <tr
          key={index}
          className="hover:bg-gray-50 transition-colors duration-150"
        >
          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
            <span className="flex items-center">
              <span className="h-8 w-8 bg-purple-100 rounded-full flex items-center justify-center mr-3">
                <svg
                  className="h-4 w-4 text-purple-500"
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
              </span>
              {group.name}
            </span>
          </td>
          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
            {groupInfo.hasApiKey ? (
              <div className="flex items-center">
                {groupInfo.aiServiceName && (
                  <img
                    src={getAiServiceIcon(groupInfo.aiServiceName)}
                    alt={groupInfo.aiServiceName}
                    className="w-5 h-5 mr-2"
                  />
                )}
                <div>
                  <p className="font-medium">{groupInfo.apiKeyName}</p>
                  {assignedKey && (
                    <p className="text-xs text-gray-500">
                      {getServiceDisplayName(assignedKey.aiServiceName)}
                      {assignedKey.model && (
                        <span className="ml-1">• {assignedKey.model}</span>
                      )}
                    </p>
                  )}
                </div>
              </div>
            ) : (
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                <svg
                  className="mr-1.5 h-3 w-3 text-yellow-700"
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
                {t('manageAiTab.noApiKey', 'No API key')}
              </span>
            )}
          </td>
          <td className="px-6 py-4 whitespace-nowrap text-center">
            <label className="inline-flex items-center cursor-pointer">
              <div className="relative">
                <input
                  type="checkbox"
                  className="sr-only"
                  checked={group.aiEvaluationEnabled}
                  disabled={!groupInfo.hasApiKey}
                  onChange={() => handleToggleAiEvaluation(group.id)}
                />
                <div
                  className={`block w-14 h-8 rounded-full transition-colors duration-200 ease-in-out ${
                    !groupInfo.hasApiKey
                      ? 'bg-gray-200 opacity-50'
                      : group.aiEvaluationEnabled
                        ? 'bg-green-300'
                        : 'bg-gray-300'
                  }`}
                />
                <div
                  className={`absolute left-1 top-1 bg-white w-6 h-6 rounded-full transition-transform duration-300 ease-spring transform ${
                    group.aiEvaluationEnabled
                      ? 'translate-x-6 bg-green-500'
                      : ''
                  } ${!groupInfo.hasApiKey && 'opacity-75'}`}
                />
              </div>
              <span
                className={`ml-3 text-sm font-medium ${
                  group.aiEvaluationEnabled ? 'text-green-600' : 'text-gray-700'
                }`}
              >
                {group.aiEvaluationEnabled
                  ? t('manageAiTab.enabled', 'Enabled')
                  : t('manageAiTab.disabled', 'Disabled')}
              </span>
            </label>
          </td>
          <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
            {groupInfo.hasApiKey ? (
              <button
                onClick={() => handleToggleAiEvaluation(group.id)}
                className={`px-3 py-1 rounded-md font-medium transition-all duration-200 ${
                  group.aiEvaluationEnabled
                    ? 'text-red-600 hover:text-red-700 bg-red-50 hover:bg-red-100 hover:shadow-sm'
                    : 'text-green-600 hover:text-green-700 bg-green-50 hover:bg-green-100 hover:shadow-sm'
                }`}
              >
                {group.aiEvaluationEnabled
                  ? t('manageAiTab.disableAi', 'Disable AI')
                  : t('manageAiTab.enableAi', 'Enable AI')}
              </button>
            ) : (
              <button
                onClick={() => {
                  setSelectedGroup(group.id.toString());
                  setTabView('assign');
                }}
                className="px-3 py-1 rounded-md font-medium text-blue-600 hover:text-blue-700 bg-blue-50 hover:bg-blue-100 transition-all duration-200 hover:shadow-sm"
              >
                {t('manageAiTab.assignApiKey', 'Assign API Key')}
              </button>
            )}
          </td>
        </tr>
      );
    });
  }, [
    associatedGroups,
    getGroupInfo,
    apiKeys,
    getAiServiceIcon,
    getServiceDisplayName,
    handleToggleAiEvaluation,
    setSelectedGroup,
    setTabView,
    t,
  ]);

  return (
    <div className="space-y-6">
      <div className="bg-yellow-50 border-l-4 border-yellow-500 p-4 rounded-r-md">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg
              className="h-5 w-5 text-yellow-500"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-yellow-800">
              {t(
                'manageAiTab.infoMessage',
                'Groups must have an API key assigned before enabling AI evaluation.'
              )}
            </p>
          </div>
        </div>
      </div>

      {associatedGroups && associatedGroups.length > 0 ? (
        <div className="bg-white rounded-xl shadow-lg overflow-hidden border border-gray-200">
          <div className="bg-gradient-to-r from-purple-500 via-purple-600 to-indigo-600 px-6 py-4 text-white">
            <h3 className="text-lg font-semibold">
              {t('manageAiTab.aiEvaluationSettings', 'AI Evaluation Settings')}
            </h3>
            <p className="text-sm text-purple-100">
              {t(
                'manageAiTab.settingsDescription',
                'Enable or disable AI evaluation for each group'
              )}
            </p>
          </div>

          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {t('manageAiTab.group', 'Group')}
                </th>
                <th className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {t('manageAiTab.apiKey', 'API Key')}
                </th>
                <th className="px-6 py-4 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {t('manageAiTab.aiEvaluation', 'AI Evaluation')}
                </th>
                <th className="px-6 py-4 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {t('manageAiTab.actions', 'Actions')}
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">{groupRows}</tbody>
          </table>
        </div>
      ) : (
        <div className="text-center py-16 bg-white rounded-xl border-2 border-dashed border-gray-300">
          <svg
            className="mx-auto h-16 w-16 text-gray-400"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
            />
          </svg>
          <h3 className="mt-4 text-lg font-medium text-gray-900">
            {t('manageAiTab.noGroupsAssociated', 'No groups associated')}
          </h3>
          <p className="mt-2 text-sm text-gray-500">
            {t(
              'manageAiTab.addGroupsHint',
              'Add groups to this test to manage AI evaluation settings.'
            )}
          </p>
        </div>
      )}
    </div>
  );
};

export default ManageAiTab;
