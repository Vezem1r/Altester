import TestItem from './TestItem';
import { useTranslation } from 'react-i18next';

const GroupsList = ({
  groups = [],
  loading = false,
  onStartTest,
  onViewAttempts,
  isCurrentSemester = true,
  searchQuery = '',
}) => {
  const { t } = useTranslation();

  const filteredGroups =
    searchQuery && searchQuery.trim() !== ''
      ? groups.filter(group => group.tests && group.tests.length > 0)
      : groups;

  if (loading) {
    return (
      <div className="space-y-8">
        {[1, 2, 3].map(index => (
          <div
            key={index}
            className="animate-pulse rounded-xl overflow-hidden shadow-md bg-white border border-gray-200"
          >
            <div className="h-14 bg-gradient-to-r from-purple-50 to-purple-100 relative overflow-hidden">
              <div className="absolute top-0 right-0 bg-purple-200 opacity-50 rounded-full w-16 h-16 -mt-4 -mr-4" />
            </div>
            <div className="p-5">
              <div className="h-14 bg-gray-100 rounded-lg mb-3" />
              <div className="h-14 bg-gray-50 rounded-lg mb-3" />
              <div className="h-14 bg-gray-50 rounded-lg" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (!filteredGroups || filteredGroups.length === 0) {
    return (
      <div className="bg-white shadow-lg rounded-xl p-8 text-center border border-gray-200">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-purple-100 text-purple-600 mb-4">
          <svg
            className="h-8 w-8"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            />
          </svg>
        </div>
        <h3 className="text-xl font-bold text-gray-900 mb-2">
          {searchQuery && searchQuery.trim() !== ''
            ? t('groupsList.noSearchResults', 'No search results found')
            : t('groupsList.noGroupsFound', 'No Groups Found')}
        </h3>
        <p className="text-gray-600 max-w-md mx-auto">
          {searchQuery && searchQuery.trim() !== ''
            ? t(
                'groupsList.noSearchDescription',
                'No groups with matching tests were found. Try adjusting your search terms.'
              )
            : t(
                'groupsList.noGroupsDescription',
                "You don't have any active groups for this period. Groups will appear here once you're enrolled."
              )}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {filteredGroups.map(group => (
        <div
          key={group.id}
          className="bg-white rounded-xl overflow-hidden shadow-md border border-gray-200 transform transition duration-300 hover:shadow-lg"
        >
          <div className="bg-gradient-to-r from-purple-500 to-purple-400 p-5 relative">
            <div className="absolute top-0 right-0 bg-white opacity-10 rounded-full w-24 h-24 -mt-8 -mr-8" />
            <div className="absolute bottom-0 left-0 bg-white opacity-10 rounded-full w-16 h-16 -mb-8 -ml-8" />

            <div className="relative flex items-center">
              <div className="bg-white/20 p-2 rounded-lg mr-3 backdrop-blur-sm">
                <svg
                  className="h-6 w-6 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                  />
                </svg>
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">{group.name}</h2>
                {group.tests && (
                  <p className="text-purple-100 text-sm mt-1">
                    {t(
                      'groupsList.testsAvailable',
                      '{{count}} {{testLabel}} available',
                      {
                        count: group.tests.length,
                        testLabel:
                          group.tests.length === 1
                            ? t('groupsList.test', 'test')
                            : t('groupsList.tests', 'tests'),
                      }
                    )}
                  </p>
                )}
              </div>
            </div>
          </div>

          <div className="p-5">
            {group.tests && group.tests.length > 0 ? (
              <div className="space-y-3">
                {group.tests.map(test => (
                  <TestItem
                    key={test.id}
                    test={test}
                    onStartTest={onStartTest}
                    onViewAttempts={onViewAttempts}
                    isCurrentSemester={isCurrentSemester}
                  />
                ))}
              </div>
            ) : (
              <div className="bg-gray-50 p-6 rounded-xl text-center">
                <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-gray-100 text-gray-500 mb-3">
                  <svg
                    className="h-6 w-6"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                    />
                  </svg>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-1">
                  {t('groupsList.noTestsAvailable', 'No Tests Available')}
                </h3>
                <p className="text-gray-500 text-sm">
                  {t(
                    'groupsList.noTestsDescription',
                    'No tests are currently available for this group.'
                  )}
                </p>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

export default GroupsList;
