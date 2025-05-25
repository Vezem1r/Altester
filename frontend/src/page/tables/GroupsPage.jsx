import { useState, useEffect, useCallback } from 'react';
import { AdminService } from '@/services/AdminService';
import { TeacherService } from '@/services/TeacherService';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import { UserGroupIcon, PlusIcon } from '@heroicons/react/outline';
import { useTranslation } from 'react-i18next';

import TablePageLayout from '@/layouts/TablePageLayout';
import DataTable from '@/components/table/DataTable';
import SearchBar from '@/components/search/SearchBar';
import SharedPagination from '@/components/common/SharedPagination';
import StatusBadge from '@/components/ui/StatusBadge';

const GroupsPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const isTeacherRole = location.pathname.includes('/teacher');

  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const itemsPerPage = 10;

  const [searchQuery, setSearchQuery] = useState('');
  const [sortField, setSortField] = useState('name');
  const [sortDirection, setSortDirection] = useState('asc');
  const [activityFilter, setActivityFilter] = useState('all');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');

  const [_totalGroupsCount, setTotalGroupsCount] = useState(0);
  const [totalFilteredCount, setTotalFilteredCount] = useState(0);

  const fetchGroups = useCallback(async () => {
    try {
      setLoading(true);

      if (isTeacherRole) {
        const response = await TeacherService.getTeacherGroups(
          currentPage,
          itemsPerPage,
          debouncedSearchQuery,
          activityFilter
        );
        setGroups(response.content);
        setTotalPages(response.totalPages);
        setTotalFilteredCount(response.totalElements);
        setTotalGroupsCount(response.totalElements);
      } else {
        const response = await AdminService.getAllGroups(
          currentPage,
          debouncedSearchQuery,
          activityFilter
        );
        setGroups(response.content);
        setTotalPages(response.totalPages);
        setTotalFilteredCount(response.totalElements);
      }

      if (groups.length === 0 && currentPage > 0) {
        setCurrentPage(0);
      }
    } catch (error) {
      toast.error(
        error.message ||
          t('groupsPage.errorLoadingGroups', 'Error loading groups')
      );
    } finally {
      setLoading(false);
    }
  }, [
    isTeacherRole,
    currentPage,
    itemsPerPage,
    debouncedSearchQuery,
    activityFilter,
    groups.length,
    t,
  ]);

  const fetchTotalCount = useCallback(async () => {
    try {
      const stats = await AdminService.getAdminStats();
      setTotalGroupsCount(stats.groupsCount);
    } catch {}
  }, []);

  useEffect(() => {
    if (isTeacherRole) {
      const params = new URLSearchParams(window.location.search);
      const groupId = params.get('groupId');
      const studentUsername = params.get('studentUsername');

      if (groupId && studentUsername) {
        navigate(`/teacher/groups/${groupId}?highlight=${studentUsername}`, {
          replace: true,
        });
      }
    }
  }, [isTeacherRole, navigate]);

  useEffect(() => {
    const handler = window.setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
    }, 300);

    return () => {
      window.clearTimeout(handler);
    };
  }, [searchQuery]);

  useEffect(() => {
    fetchGroups();
  }, [fetchGroups]);

  useEffect(() => {
    if (!isTeacherRole) {
      fetchTotalCount();
    }
  }, [isTeacherRole, fetchTotalCount]);

  const handleSearchChange = e => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const handleSortChange = field => {
    if (field === sortField) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }

    const sortedGroups = [...groups].sort((a, b) => {
      let valueA, valueB;

      if (field === 'studentCount') {
        valueA = a.studentCount || 0;
        valueB = b.studentCount || 0;
      } else if (field === 'active') {
        valueA = a.active ? 1 : 0;
        valueB = b.active ? 1 : 0;
      } else if (field === 'academicYear') {
        valueA = a.academicYear || 0;
        valueB = b.academicYear || 0;
      } else {
        valueA = (a[field] || '').toLowerCase();
        valueB = (b[field] || '').toLowerCase();
      }

      if (sortDirection === 'asc') {
        return valueA > valueB ? 1 : -1;
      }
      return valueA < valueB ? 1 : -1;
    });

    setGroups(sortedGroups);
  };

  const handleCreateGroupClick = () => {
    navigate(isTeacherRole ? '/teacher/groups/create' : '/admin/groups/create');
  };

  const getSemesterName = semester => {
    if (!semester) return t('groupsPage.unknown', 'Unknown');

    const semesterMap = {
      WINTER: t('groupsPage.winterSemester', 'Winter Semester'),
      SUMMER: t('groupsPage.summerSemester', 'Summer Semester'),
    };

    return semesterMap[semester] || semester;
  };

  const getStatusBadge = group => {
    if (group.inFuture) {
      return <StatusBadge status="future" />;
    } else if (group.active) {
      return <StatusBadge status="active" />;
    }
    return <StatusBadge status="inactive" />;
  };

  const columns = [
    {
      key: 'name',
      label: t('groupsPage.groupName', 'Group Name'),
      sortable: true,
    },
    {
      key: 'semester',
      label: t('groupsPage.semester', 'Semester'),
      sortable: true,
    },
    {
      key: 'academicYear',
      label: t('groupsPage.year', 'Year'),
      sortable: true,
    },
    ...(!isTeacherRole
      ? [
          {
            key: 'teacherUsername',
            label: t('groupsPage.teacher', 'Teacher'),
            sortable: true,
          },
        ]
      : []),
    {
      key: 'studentCount',
      label: t('groupsPage.students', 'Students'),
      sortable: true,
    },
    {
      key: 'subjectShortName',
      label: t('groupsPage.subject', 'Subject'),
      sortable: true,
    },
    {
      key: 'active',
      label: t('groupsPage.status', 'Status'),
      sortable: true,
    },
    {
      key: 'actions',
      label: t('groupsPage.actions', 'Actions'),
      sortable: false,
    },
  ];

  const renderRow = group => (
    <tr
      key={group.id}
      className={`hover:bg-gray-50 ${
        !group.active && !group.inFuture
          ? 'bg-gray-50'
          : group.inFuture
            ? 'bg-blue-50'
            : ''
      }`}
    >
      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
        {group.name}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {getSemesterName(group.semester)}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {group.academicYear || t('groupsPage.notApplicable', 'N/A')}
      </td>
      {!isTeacherRole && (
        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
          {group.teacherUsername || t('groupsPage.notAssigned', 'Not assigned')}
        </td>
      )}
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {group.studentCount || 0}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {group.subjectShortName || t('groupsPage.notAssigned', 'Not assigned')}
      </td>
      <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(group)}</td>
      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
        <Link
          to={`${isTeacherRole ? '/teacher' : '/admin'}/groups/${group.id}`}
          className="text-purple-600 hover:text-purple-900"
        >
          {group.active || group.inFuture
            ? t('groupsPage.manage', 'Manage')
            : t('groupsPage.view', 'View')}
        </Link>
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
        { value: 'all', label: t('groupsPage.allGroups', 'All Groups') },
        {
          value: 'active',
          label: t('groupsPage.activeGroups', 'Active Groups'),
        },
        {
          value: 'inactive',
          label: t('groupsPage.inactiveGroups', 'Inactive Groups'),
        },
        {
          value: 'future',
          label: t('groupsPage.futureGroups', 'Future Groups'),
        },
      ],
    },
  ];

  const primaryAction = !isTeacherRole ? (
    <button
      onClick={handleCreateGroupClick}
      className="inline-flex items-center transition-colors duration-200"
    >
      <PlusIcon className="-ml-1 mr-2 h-5 w-5" aria-hidden="true" />
      {t('groupsPage.createGroup', 'Create Group')}
    </button>
  ) : null;

  return (
    <TablePageLayout
      icon={<UserGroupIcon />}
      title={t('groupsPage.title', 'Groups Management')}
      description={
        isTeacherRole
          ? t(
              'groupsPage.teacherDescription',
              'View and manage your teaching groups'
            )
          : t(
              'groupsPage.adminDescription',
              'Manage student groups, their teachers, and semester assignments'
            )
      }
      primaryAction={primaryAction}
      variant="violet-purple"
    >
      <SearchBar
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder={
          isTeacherRole
            ? t(
                'groupsPage.teacherSearchPlaceholder',
                'Search groups or students...'
              )
            : t('groupsPage.adminSearchPlaceholder', 'Search groups...')
        }
        filters={filters}
        itemCount={totalFilteredCount}
        itemName={t('groupsPage.groups', 'groups')}
      />

      {isTeacherRole && (
        <div className="bg-blue-50 border-l-4 border-blue-400 p-4 mb-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg
                className="h-5 w-5 text-blue-400"
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
              <p className="text-sm text-blue-700">
                {t(
                  'groupsPage.clickManageTip',
                  'Click on "Manage" to access group details and student management options.'
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      <DataTable
        columns={columns}
        data={groups}
        loading={loading}
        searchQuery={searchQuery}
        onSort={handleSortChange}
        sortField={sortField}
        sortDirection={sortDirection}
        renderRow={renderRow}
        emptyMessage={t('groupsPage.noGroupsFound', 'No groups found')}
      />

      <div className="mt-6">
        <SharedPagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={totalFilteredCount}
          onPageChange={setCurrentPage}
          itemName={t('groupsPage.groups', 'groups')}
          itemsPerPage={itemsPerPage}
        />
      </div>
    </TablePageLayout>
  );
};

export default GroupsPage;
