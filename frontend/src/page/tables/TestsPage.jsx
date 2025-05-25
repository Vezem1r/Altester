import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { TestService } from '@/services/TestService';
import { AdminService } from '@/services/AdminService';
import { TeacherService } from '@/services/TeacherService';
import { useAuth } from '@/context/AuthContext';
import { toast } from 'react-toastify';
import { ClipboardListIcon, PlusIcon } from '@heroicons/react/outline';
import { useTranslation } from 'react-i18next';

import TablePageLayout from '@/layouts/TablePageLayout';
import DataTable from '@/components/table/DataTable';
import SearchBar from '@/components/search/SearchBar';
import SharedPagination from '@/components/common/SharedPagination';
import StatusBadge from '@/components/ui/StatusBadge';

const TestsPage = () => {
  const { t } = useTranslation();
  const { userRole } = useAuth();

  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalTestsCount, setTotalTestsCount] = useState(0);

  const [searchQuery, setSearchQuery] = useState('');
  const [sortField] = useState('title');
  const [sortDirection] = useState('asc');
  const [activityFilter, setActivityFilter] = useState('all');
  const [editPermissionFilter, setEditPermissionFilter] = useState('all');
  const itemsPerPage = 10;

  const fetchTests = useCallback(async () => {
    try {
      setLoading(true);

      const isActive =
        activityFilter === 'all' ? null : activityFilter === 'open';
      const sortParam = `${sortField},${sortDirection}`;
      const allowTeacherEdit =
        editPermissionFilter === 'all'
          ? null
          : editPermissionFilter === 'editable';

      let response;

      if (userRole === 'ADMIN') {
        response = await TestService.getAllTests(
          currentPage,
          itemsPerPage,
          sortParam,
          searchQuery,
          isActive
        );
      } else if (userRole === 'TEACHER') {
        response = await TestService.getTeacherTests(
          currentPage,
          itemsPerPage,
          sortParam,
          searchQuery,
          isActive,
          allowTeacherEdit
        );
      }

      setTests(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalTestsCount(response.totalElements || 0);
    } catch (error) {
      toast.error(
        error.message || t('testsPage.errorLoadingTests', 'Error loading tests')
      );
    } finally {
      setLoading(false);
    }
  }, [
    userRole,
    currentPage,
    searchQuery,
    sortField,
    sortDirection,
    activityFilter,
    editPermissionFilter,
    itemsPerPage,
    t,
  ]);

  const fetchGroups = useCallback(async () => {
    try {
      if (userRole === 'ADMIN') {
        await AdminService.getAllGroups(0);
      } else if (userRole === 'TEACHER') {
        await TeacherService.getTeacherGroups(0);
      }
    } catch {}
  }, [userRole]);

  useEffect(() => {
    if (userRole) {
      fetchTests();
      fetchGroups();
    }
  }, [userRole, fetchTests, fetchGroups]);

  const handleSearchChange = e => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const handleToggleActivity = async testId => {
    try {
      await TestService.toggleTestActivity(testId);
      toast.success(
        t('testsPage.testStatusToggled', 'Test status toggled successfully')
      );
      fetchTests();
    } catch (error) {
      toast.error(
        error.message ||
          t('testsPage.errorChangingTestStatus', 'Error changing test status')
      );
    }
  };

  const formatDuration = durationMinutes => {
    if (durationMinutes < 60) {
      return t('testsPage.minutesFormat', '{{duration}} min', {
        duration: durationMinutes,
      });
    }

    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;

    if (minutes === 0) {
      return t('testsPage.hoursFormat', '{{hours}} {{hourLabel}}', {
        hours,
        hourLabel:
          hours === 1
            ? t('testsPage.hour', 'hour')
            : t('testsPage.hours', 'hours'),
      });
    }

    return t('testsPage.hoursMinutesFormat', '{{hours}}h {{minutes}}m', {
      hours,
      minutes,
    });
  };

  const formatDateTime = dateTimeString => {
    if (!dateTimeString) return t('testsPage.notSet', 'Not set');

    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  const columns = [
    {
      key: 'title',
      label: t('testsPage.testTitle', 'Test Title'),
    },
    {
      key: 'duration',
      label: t('testsPage.duration', 'Duration'),
    },
    {
      key: 'score',
      label: t('testsPage.score', 'Score'),
    },
    {
      key: 'attempts',
      label: t('testsPage.attempts', 'Attempts'),
    },
    {
      key: 'time',
      label: t('testsPage.timePeriod', 'Time Period'),
    },
    {
      key: 'status',
      label: t('testsPage.status', 'Status'),
    },
  ];

  if (userRole === 'TEACHER') {
    columns.push({
      key: 'edit',
      label: t('testsPage.editAccess', 'Edit Access'),
    });
  }

  columns.push({
    key: 'actions',
    label: t('testsPage.actions', 'Actions'),
  });

  const renderRow = test => (
    <tr key={test.id} className="hover:bg-gray-50">
      <td className="px-6 py-4 max-w-[250px]">
        <div className="flex items-center">
          <div
            className="text-sm font-medium text-gray-900 truncate"
            title={test.title}
          >
            {test.title}
          </div>
          {test.AiEvaluate && (
            <StatusBadge variant="info" className="ml-2">
              AI
            </StatusBadge>
          )}
        </div>
        {test.description && (
          <div
            className="text-xs text-gray-500 truncate"
            title={test.description}
          >
            {test.description}
          </div>
        )}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {formatDuration(test.duration)}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {test.totalScore} {t('testsPage.points', 'pts')}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {test.maxAttempts
          ? test.maxAttempts
          : t('testsPage.unlimited', 'Unlimited')}
      </td>

      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {test.startTime || test.endTime ? (
          <div className="text-xs">
            {test.startTime && (
              <div>
                {t('testsPage.from', 'From')}: {formatDateTime(test.startTime)}
              </div>
            )}
            {test.endTime && (
              <div>
                {t('testsPage.to', 'To')}: {formatDateTime(test.endTime)}
              </div>
            )}
          </div>
        ) : (
          <span className="text-gray-500">
            {t('testsPage.noTimeLimit', 'No time limit')}
          </span>
        )}
      </td>

      <td className="px-6 py-4 whitespace-nowrap">
        <div className="flex flex-col space-y-1">
          <StatusBadge status={test.open ? 'open' : 'closed'} />
          {test.AiEvaluate && (
            <StatusBadge variant="info">
              {t('testsPage.aiEvaluation', 'AI Evaluation')}
            </StatusBadge>
          )}
        </div>
      </td>
      {userRole === 'TEACHER' && (
        <td className="px-6 py-4 whitespace-nowrap">
          {test.allowTeacherEdit === false ? (
            <span className="px-2 inline-flex items-center text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
              <svg
                className="h-3.5 w-3.5 mr-1"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z"
                  clipRule="evenodd"
                />
              </svg>
              {t('testsPage.locked', 'Locked')}
            </span>
          ) : (
            <span className="px-2 inline-flex items-center text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
              <svg
                className="h-3.5 w-3.5 mr-1"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
              </svg>
              {t('testsPage.editable', 'Editable')}
            </span>
          )}
        </td>
      )}
      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
        {userRole === 'ADMIN' ? (
          <div className="flex space-x-3">
            <Link
              to={`/admin/tests/${test.id}`}
              className="text-purple-600 hover:text-purple-900"
            >
              {t('testsPage.view', 'View')}
            </Link>
            <Link
              to={`/admin/tests/${test.id}/edit`}
              className="text-indigo-600 hover:text-indigo-900"
            >
              {t('testsPage.edit', 'Edit')}
            </Link>
            <button
              onClick={() => handleToggleActivity(test.id)}
              className={
                test.open
                  ? 'text-red-600 hover:text-red-900'
                  : 'text-green-600 hover:text-green-900'
              }
            >
              {test.open
                ? t('testsPage.close', 'Close')
                : t('testsPage.open', 'Open')}
            </button>
          </div>
        ) : test.allowTeacherEdit === false ? (
          <div className="flex space-x-3">
            <Link
              to={`/teacher/tests/${test.id}`}
              className="text-purple-600 hover:text-purple-900"
            >
              {t('testsPage.view', 'View')}
            </Link>
          </div>
        ) : (
          <div className="flex space-x-3">
            <Link
              to={`/teacher/tests/${test.id}`}
              className="text-purple-600 hover:text-purple-900"
            >
              {t('testsPage.view', 'View')}
            </Link>
            <Link
              to={`/teacher/tests/${test.id}/edit`}
              className="text-indigo-600 hover:text-indigo-900"
            >
              {t('testsPage.edit', 'Edit')}
            </Link>
            <button
              onClick={() => handleToggleActivity(test.id)}
              className={
                test.open
                  ? 'text-red-600 hover:text-red-900'
                  : 'text-green-600 hover:text-green-900'
              }
            >
              {test.open
                ? t('testsPage.close', 'Close')
                : t('testsPage.open', 'Open')}
            </button>
          </div>
        )}
      </td>
    </tr>
  );

  const filters = [
    {
      value: activityFilter,
      onChange: e => {
        setActivityFilter(e.target.value);
        setCurrentPage(0);
      },
      options: [
        { value: 'all', label: t('testsPage.allTests', 'All Tests') },
        { value: 'open', label: t('testsPage.openTests', 'Open Tests') },
        { value: 'closed', label: t('testsPage.closedTests', 'Closed Tests') },
      ],
    },
  ];

  if (userRole === 'TEACHER') {
    filters.push({
      value: editPermissionFilter,
      onChange: e => {
        setEditPermissionFilter(e.target.value);
        setCurrentPage(0);
      },
      options: [
        {
          value: 'all',
          label: t('testsPage.allPermissions', 'All Permissions'),
        },
        {
          value: 'editable',
          label: t('testsPage.editableTests', 'Editable Tests'),
        },
        { value: 'locked', label: t('testsPage.lockedTests', 'Locked Tests') },
      ],
    });
  }

  const primaryAction = (
    <Link
      to={
        userRole === 'ADMIN' ? '/admin/tests/create' : '/teacher/tests/create'
      }
      className="inline-flex items-center transition-colors duration-200"
    >
      <PlusIcon className="-ml-1 mr-2 h-5 w-5" aria-hidden="true" />
      {t('testsPage.createNewTest', 'Create New Test')}
    </Link>
  );

  return (
    <TablePageLayout
      icon={<ClipboardListIcon />}
      title={t('testsPage.title', 'Tests Management')}
      description={t('testsPage.description', 'Manage all tests in the system')}
      primaryAction={primaryAction}
      variant="purple"
    >
      <SearchBar
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder={t(
          'testsPage.searchPlaceholder',
          'Search tests by title, description...'
        )}
        filters={filters}
        itemCount={totalTestsCount}
        itemName={t('testsPage.tests', 'tests')}
      />

      <DataTable
        columns={columns}
        data={tests}
        loading={loading}
        searchQuery={searchQuery}
        sortField={sortField}
        sortDirection={sortDirection}
        renderRow={renderRow}
        emptyMessage={t('testsPage.noTestsFound', 'No tests found')}
      />

      <div className="mt-6">
        <SharedPagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={totalTestsCount}
          itemsPerPage={itemsPerPage}
          onPageChange={setCurrentPage}
          itemName={t('testsPage.tests', 'tests')}
        />
      </div>
    </TablePageLayout>
  );
};

export default TestsPage;
