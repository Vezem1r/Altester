import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

const OverviewTab = ({
  associatedGroups,
  getGroupInfo,
  apiKeys,
  handleUnassignKey,
  handleToggleAiEvaluation,
  setSelectedGroup,
  setTabView,
  getAiServiceIcon,
  getServiceDisplayName,
}) => {
  const { t } = useTranslation();

  const groupCards = useMemo(() => {
    return associatedGroups.map(group => {
      const groupInfo = getGroupInfo(group.id);
      const assignedKey = apiKeys.find(
        key => key.group && key.group.groupId === group.id
      );

      return (
        <div
          key={group.id}
          className="bg-white rounded-xl shadow-sm hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1 overflow-hidden border border-gray-200"
        >
          <div className="px-6 py-4 bg-gradient-to-r from-purple-500 via-purple-600 to-indigo-600 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-white">{group.name}</h3>
            <p className="mt-1 text-sm text-purple-100">
              {t('overviewTab.groupId', 'Group ID: {{id}}', { id: group.id })}
            </p>
          </div>
          <div className="p-6 space-y-5">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center shadow-inner">
                    {groupInfo.aiServiceName ? (
                      <img
                        src={getAiServiceIcon(groupInfo.aiServiceName)}
                        alt={groupInfo.aiServiceName}
                        className="w-6 h-6"
                      />
                    ) : (
                      <svg
                        className="w-6 h-6 text-purple-600"
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
                    )}
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-700">
                    {t('overviewTab.apiKey', 'API Key')}
                  </p>
                  {groupInfo.hasApiKey ? (
                    <div>
                      <p className="text-sm font-semibold text-gray-900">
                        {groupInfo.apiKeyName}
                      </p>
                      <p className="text-xs text-gray-500">
                        {assignedKey &&
                          getServiceDisplayName(assignedKey.aiServiceName)}
                        {assignedKey && assignedKey.model && (
                          <span className="ml-1">• {assignedKey.model}</span>
                        )}
                      </p>
                    </div>
                  ) : (
                    <p className="text-sm text-gray-500">
                      {t('overviewTab.notAssigned', 'Not assigned')}
                    </p>
                  )}
                </div>
              </div>
              {groupInfo.hasApiKey ? (
                <button
                  onClick={() => handleUnassignKey(group.id)}
                  className="text-sm font-medium text-red-600 hover:text-red-700 transition-all duration-200 hover:bg-red-50 px-3 py-1 rounded-md hover:shadow-sm"
                >
                  {t('overviewTab.remove', 'Remove')}
                </button>
              ) : (
                <button
                  onClick={() => {
                    setSelectedGroup(group.id.toString());
                    setTabView('assign');
                  }}
                  className="text-sm font-medium text-blue-600 hover:text-blue-700 transition-all duration-200 bg-blue-50 hover:bg-blue-100 px-3 py-1 rounded-md hover:shadow-sm"
                >
                  {t('overviewTab.assignKey', 'Assign Key')}
                </button>
              )}
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center shadow-inner">
                    <svg
                      className="w-6 h-6 text-blue-600"
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
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-700">
                    {t('overviewTab.aiEvaluation', 'AI Evaluation')}
                  </p>
                  <span
                    className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      group.aiEvaluationEnabled
                        ? 'bg-green-100 text-green-800'
                        : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {group.aiEvaluationEnabled
                      ? t('overviewTab.enabled', 'Enabled')
                      : t('overviewTab.disabled', 'Disabled')}
                  </span>
                </div>
              </div>
              {groupInfo.hasApiKey && (
                <button
                  onClick={() => handleToggleAiEvaluation(group.id)}
                  className={`text-sm font-medium transition-all duration-200 px-3 py-1 rounded-md hover:shadow-sm ${
                    group.aiEvaluationEnabled
                      ? 'text-red-600 hover:text-red-700 bg-red-50 hover:bg-red-100'
                      : 'text-green-600 hover:text-green-700 bg-green-50 hover:bg-green-100'
                  }`}
                >
                  {group.aiEvaluationEnabled
                    ? t('overviewTab.disable', 'Disable')
                    : t('overviewTab.enable', 'Enable')}
                </button>
              )}
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center shadow-inner">
                    <svg
                      className="w-6 h-6 text-purple-600"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
                      />
                    </svg>
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-700">
                    {t('overviewTab.evaluationPrompt', 'Evaluation Prompt')}
                  </p>
                  <p className="text-sm text-gray-900">
                    {groupInfo.currentPrompt
                      ? groupInfo.currentPrompt.name
                      : t(
                          'overviewTab.defaultGradingPrompt',
                          'Default Grading Prompt'
                        )}
                  </p>
                </div>
              </div>
              <button
                onClick={() => {
                  setSelectedGroup(group.id.toString());
                  setTabView('prompts');
                }}
                className="text-sm font-medium text-blue-600 hover:text-blue-700 transition-all duration-200 bg-blue-50 hover:bg-blue-100 px-3 py-1 rounded-md hover:shadow-sm"
              >
                {t('overviewTab.change', 'Change')}
              </button>
            </div>
          </div>
        </div>
      );
    });
  }, [
    associatedGroups,
    getGroupInfo,
    apiKeys,
    handleUnassignKey,
    handleToggleAiEvaluation,
    setSelectedGroup,
    setTabView,
    getAiServiceIcon,
    getServiceDisplayName,
    t,
  ]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">{groupCards}</div>

      {associatedGroups.length === 0 && (
        <div className="text-center py-16 bg-white rounded-xl border-2 border-dashed border-gray-300 hover:border-purple-300 transition-colors duration-300">
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
            {t('overviewTab.noGroupsAssociated', 'No groups associated')}
          </h3>
          <p className="mt-2 text-sm text-gray-500">
            {t(
              'overviewTab.addGroupsHint',
              'Add groups to this test to configure API keys and AI evaluation settings.'
            )}
          </p>
        </div>
      )}
    </div>
  );
};

export default OverviewTab;
