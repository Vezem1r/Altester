import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

const PromptsTab = ({
  selectedPromptGroup,
  setSelectedPromptGroup,
  associatedGroups,
  promptType,
  handlePromptTypeChange,
  prompts,
  selectedPrompt,
  setSelectedPrompt,
  handleAssignPrompt,
  setTabView,
  loading,
  handleViewPrompt,
}) => {
  const { t } = useTranslation();

  const groupOptions = useMemo(() => {
    return associatedGroups.map(group => (
      <option key={group.id} value={group.id}>
        {group.name}
      </option>
    ));
  }, [associatedGroups]);

  const promptsList = useMemo(() => {
    return prompts.map(prompt => (
      <div
        key={prompt.id}
        className={`relative p-5 border-2 rounded-xl cursor-pointer transition-all duration-200 hover:shadow-md ${
          selectedPrompt === prompt.id.toString()
            ? 'border-purple-500 bg-purple-50 shadow-md'
            : 'border-gray-200 hover:border-purple-300 hover:bg-gray-50'
        }`}
        onClick={() => setSelectedPrompt(prompt.id.toString())}
      >
        <div className="pr-8">
          <div className="flex justify-between items-start">
            <h4 className="font-semibold text-gray-900 flex-grow">
              {prompt.title}
            </h4>
            <button
              type="button"
              onClick={e => {
                e.stopPropagation();
                handleViewPrompt(prompt.id);
              }}
              className="p-1 text-gray-400 hover:text-blue-600 transition-colors"
              title={t('promptsTab.viewPrompt', 'View prompt')}
            >
              <svg
                className="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                />
              </svg>
            </button>
          </div>
          <p className="text-sm text-gray-600 mt-1">
            {prompt.description ||
              t('promptsTab.noDescription', 'No description')}
          </p>
          <div className="text-xs text-gray-500 mt-3 flex items-center space-x-3">
            <span>
              {t('promptsTab.byAuthor', 'By {{author}}', {
                author: prompt.authorUsername,
              })}
            </span>
            <span>•</span>
            <span>{new Date(prompt.created).toLocaleDateString()}</span>
          </div>
        </div>
        {selectedPrompt === prompt.id.toString() && (
          <div className="absolute top-5 right-5">
            <div className="w-6 h-6 bg-purple-600 rounded-full flex items-center justify-center pulse-animation">
              <svg
                className="w-4 h-4 text-white"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
          </div>
        )}
      </div>
    ));
  }, [prompts, selectedPrompt, setSelectedPrompt, handleViewPrompt, t]);

  return (
    <div className="space-y-6">
      <div className="bg-purple-50 border-l-4 border-purple-500 p-4 rounded-r-md">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg
              className="h-5 w-5 text-purple-500"
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
            <p className="text-sm text-purple-800">
              {t(
                'promptsTab.infoMessage',
                'Prompts guide the AI in evaluating student responses. Choose from your personal prompts or public prompts.'
              )}
            </p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-lg p-6 space-y-6 border border-gray-200">
        <div className="p-4 bg-gradient-to-br from-purple-50 via-indigo-50 to-blue-50 rounded-lg border border-purple-100 shadow-sm">
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
                d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
              />
            </svg>
            {t('promptsTab.assignEvaluationPrompt', 'Assign Evaluation Prompt')}
          </h3>

          <div className="space-y-4">
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
                {t(
                  'promptsTab.selectGroupForPrompt',
                  'Select Group for Prompt Assignment'
                )}
              </label>
              <div className="relative group">
                <select
                  value={selectedPromptGroup}
                  onChange={e => setSelectedPromptGroup(e.target.value)}
                  className="appearance-none w-full px-4 py-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white shadow-sm transition-all duration-200 group-hover:border-purple-300"
                >
                  <option value="">
                    {t('promptsTab.selectGroup', '-- Select a Group --')}
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
                    'promptsTab.noGroupsAssociated',
                    'No groups associated with this test. Please add groups to the test first.'
                  )}
                </p>
              )}
            </div>
          </div>
        </div>

        <div>
          <div className="flex space-x-4 mb-6">
            <button
              onClick={() => handlePromptTypeChange('my')}
              className={`flex-1 px-4 py-2.5 rounded-lg font-medium transition-all duration-200 ${
                promptType === 'my'
                  ? 'bg-gradient-to-r from-purple-600 to-purple-700 text-white shadow-md transform scale-105'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              <span className="flex items-center justify-center">
                <svg
                  className="w-4 h-4 mr-2"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                  />
                </svg>
                {t('promptsTab.myPrompts', 'My Prompts')}
              </span>
            </button>
            <button
              onClick={() => handlePromptTypeChange('public')}
              className={`flex-1 px-4 py-2.5 rounded-lg font-medium transition-all duration-200 ${
                promptType === 'public'
                  ? 'bg-gradient-to-r from-purple-600 to-purple-700 text-white shadow-md transform scale-105'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              <span className="flex items-center justify-center">
                <svg
                  className="w-4 h-4 mr-2"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"
                  />
                </svg>
                {t('promptsTab.publicPrompts', 'Public Prompts')}
              </span>
            </button>
          </div>

          <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 shadow-inner">
            <div className="mb-3 flex justify-between items-center">
              <h4 className="text-sm font-medium text-gray-700 flex items-center">
                <svg
                  className="w-4 h-4 mr-2 text-purple-600"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                  />
                </svg>
                {t(
                  'promptsTab.availablePrompts',
                  'Available Prompts ({{count}})',
                  { count: prompts.length }
                )}
              </h4>
              <span className="text-xs text-gray-500 bg-white px-2 py-1 rounded-full shadow-sm border border-gray-200">
                {promptType === 'my'
                  ? t(
                      'promptsTab.personalPromptsLabel',
                      'Your personal prompts'
                    )
                  : t(
                      'promptsTab.sharedPromptsLabel',
                      'Prompts shared by other users'
                    )}
              </span>
            </div>

            <div className="space-y-4 max-h-80 overflow-y-auto pr-2 custom-scrollbar">
              {promptsList}

              {prompts.length === 0 && (
                <div className="text-center py-12 text-gray-500 bg-white rounded-lg border border-gray-200">
                  <svg
                    className="mx-auto h-12 w-12 text-gray-400 mb-4"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={1.5}
                      d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
                    />
                  </svg>
                  <h3 className="mt-2 text-sm font-medium text-gray-900">
                    {t(
                      'promptsTab.noPromptsAvailable',
                      'No {{promptType}} prompts available.',
                      { promptType }
                    )}
                  </h3>
                </div>
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
            {t('promptsTab.cancel', 'Cancel')}
          </button>
          <button
            type="button"
            onClick={handleAssignPrompt}
            disabled={!selectedPrompt || !selectedPromptGroup || loading}
            className={`px-6 py-2.5 rounded-lg font-medium text-white transition-all duration-200 shadow-sm ${
              !selectedPrompt || !selectedPromptGroup || loading
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-purple-600 hover:bg-purple-700 hover:shadow transform hover:scale-105'
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
                {t('promptsTab.assigning', 'Assigning...')}
              </span>
            ) : (
              t('promptsTab.assignPrompt', 'Assign Prompt')
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default PromptsTab;
