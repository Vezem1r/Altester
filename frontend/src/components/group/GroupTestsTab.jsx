import InfoCard from '@/components/shared/InfoCard';
import { TestService } from '@/services/TestService';
import { toast } from 'react-toastify';
import { Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

const GroupTestsTab = ({ group }) => {
  const { t } = useTranslation();
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalTests, setTotalTests] = useState(0);

  useEffect(() => {
    if (group && group.id) {
      fetchGroupTests();
    }
  }, [group, searchQuery, activeFilter, currentPage]);

  const fetchGroupTests = async () => {
    try {
      setLoading(true);
      const response = await TestService.getTestsByGroup(
        group.id,
        searchQuery,
        activeFilter,
        currentPage,
        10
      );
      setTests(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalTests(response.totalElements || 0);
    } catch {
      toast.error(
        t('groupTestsTab.fetchError', 'Failed to load tests for this group')
      );
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = dateTimeString => {
    if (!dateTimeString) return t('groupTestsTab.notSet', 'Not set');

    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  const formatDuration = durationMinutes => {
    if (!durationMinutes) return t('groupTestsTab.notSet', 'Not set');

    if (durationMinutes < 60) {
      return t('groupTestsTab.minutesFormat', '{{minutes}} minutes', {
        minutes: durationMinutes,
      });
    }

    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;

    if (minutes === 0) {
      return t('groupTestsTab.hoursFormat', '{{hours}} {{hourLabel}}', {
        hours,
        hourLabel:
          hours === 1
            ? t('groupTestsTab.hour', 'hour')
            : t('groupTestsTab.hours', 'hours'),
      });
    }

    return t('groupTestsTab.hoursMinutesFormat', '{{hours}}h {{minutes}}m', {
      hours,
      minutes,
    });
  };

  const handleSearchChange = e => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const handleFilterChange = e => {
    const value = e.target.value;
    setActiveFilter(value === 'all' ? null : value === 'open');
    setCurrentPage(0);
  };

  const handlePreviousPage = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(currentPage + 1);
    }
  };

  return (
    <InfoCard
      title={t('groupTestsTab.testsForGroup', 'Tests for {{groupName}}', {
        groupName: group?.name,
      })}
    >
      {/* Search and filter controls */}
      <div className="mb-4 flex flex-col md:flex-row gap-2">
        <div className="relative flex-grow">
          <input
            type="text"
            value={searchQuery}
            onChange={handleSearchChange}
            placeholder={t('groupTestsTab.searchTests', 'Search tests...')}
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-purple-500 focus:border-purple-500"
          />
          <div className="absolute right-3 top-2.5 text-gray-400">
            <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path
                fillRule="evenodd"
                d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
                clipRule="evenodd"
              />
            </svg>
          </div>
        </div>

        <select
          value={
            activeFilter === null ? 'all' : activeFilter ? 'open' : 'closed'
          }
          onChange={handleFilterChange}
          className="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-purple-500 focus:border-purple-500"
        >
          <option value="all">
            {t('groupTestsTab.allTests', 'All Tests')}
          </option>
          <option value="open">
            {t('groupTestsTab.openTests', 'Open Tests')}
          </option>
          <option value="closed">
            {t('groupTestsTab.closedTests', 'Closed Tests')}
          </option>
        </select>
      </div>

      {/* Results info */}
      <div className="mb-4">
        <p className="text-sm text-gray-500">
          {t('groupTestsTab.foundTests', 'Found {{count}} tests', {
            count: totalTests,
          })}
          {activeFilter !== null
            ? t('groupTestsTab.filterStatus', ' ({{status}})', {
                status: activeFilter
                  ? t('groupTestsTab.open', 'open')
                  : t('groupTestsTab.closed', 'closed'),
              })
            : ''}
          {searchQuery
            ? t('groupTestsTab.matchingQuery', ' matching "{{query}}"', {
                query: searchQuery,
              })
            : ''}
          {tests.length > 0
            ? t(
                'groupTestsTab.showingRange',
                ' - Showing {{start}} to {{end}}',
                {
                  start: currentPage * 10 + 1,
                  end: Math.min((currentPage + 1) * 10, totalTests),
                }
              )
            : ''}
        </p>
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-10">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600" />
        </div>
      ) : (
        <div className="bg-white overflow-hidden shadow rounded-lg">
          {tests.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      {t('groupTestsTab.testTitle', 'Test Title')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      {t('groupTestsTab.duration', 'Duration')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      {t('groupTestsTab.status', 'Status')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      {t('groupTestsTab.startTime', 'Start Time')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      {t('groupTestsTab.endTime', 'End Time')}
                    </th>
                    <th
                      scope="col"
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      {t('groupTestsTab.actions', 'Actions')}
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {tests.map(test => (
                    <tr key={test.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">
                          {test.title}
                        </div>
                        {test.description && (
                          <div className="text-xs text-gray-500 truncate max-w-xs">
                            {test.description}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {formatDuration(test.duration)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {test.open ? (
                          <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                            {t('groupTestsTab.open', 'Open')}
                          </span>
                        ) : (
                          <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">
                            {t('groupTestsTab.closed', 'Closed')}
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {formatDateTime(test.startTime)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {formatDateTime(test.endTime)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <Link
                          to={`/admin/tests/${test.id}`}
                          className="text-purple-600 hover:text-purple-900"
                        >
                          {t('groupTestsTab.viewDetails', 'View Details')}
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Pagination controls */}
              {totalPages > 1 && (
                <div className="px-4 py-3 flex items-center justify-between border-t border-gray-200">
                  <div className="flex-1 flex justify-between">
                    <button
                      onClick={handlePreviousPage}
                      disabled={currentPage === 0}
                      className={`relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md 
                        ${
                          currentPage === 0
                            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                            : 'bg-white text-gray-700 hover:bg-gray-50'
                        }`}
                    >
                      {t('groupTestsTab.previous', 'Previous')}
                    </button>
                    <span className="text-sm text-gray-700">
                      {t(
                        'groupTestsTab.pageInfo',
                        'Page {{current}} of {{total}}',
                        { current: currentPage + 1, total: totalPages }
                      )}
                    </span>
                    <button
                      onClick={handleNextPage}
                      disabled={currentPage >= totalPages - 1}
                      className={`relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md
                        ${
                          currentPage >= totalPages - 1
                            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                            : 'bg-white text-gray-700 hover:bg-gray-50'
                        }`}
                    >
                      {t('groupTestsTab.next', 'Next')}
                    </button>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="text-center py-8">
              <svg
                className="mx-auto h-12 w-12 text-gray-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1}
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">
                {t('groupTestsTab.noTestsFound', 'No tests found')}
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                {searchQuery
                  ? t(
                      'groupTestsTab.noMatchingTests',
                      'No tests matching "{{query}}" found',
                      { query: searchQuery }
                    )
                  : t(
                      'groupTestsTab.noAssociatedTests',
                      'There are no tests associated with this group.'
                    )}
              </p>
            </div>
          )}
        </div>
      )}
    </InfoCard>
  );
};

export default GroupTestsTab;
