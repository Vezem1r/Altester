import { useState, useEffect, useCallback } from 'react';
import { AdminService } from '@/services/AdminService';
import { toast } from 'react-toastify';
import { AcademicCapIcon } from '@heroicons/react/outline';
import { useTranslation } from 'react-i18next';

import TablePageLayout from '@/layouts/TablePageLayout';
import DataTable from '@/components/table/DataTable';
import SearchBar from '@/components/search/SearchBar';
import SharedPagination from '@/components/common/SharedPagination';
import StatusBadge from '@/components/ui/StatusBadge';
import TeacherDetailsModal from '@/components/modals/TeacherDetailsModal';

const TeachersPage = () => {
  const { t } = useTranslation();
  const [teachers, setTeachers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedTeacher, setSelectedTeacher] = useState(null);
  const [directEditMode, setDirectEditMode] = useState(false);
  const itemsPerPage = 20;

  const [searchQuery, setSearchQuery] = useState('');
  const [searchField, setSearchField] = useState('all');
  const [registrationFilter, setRegistrationFilter] = useState('all');
  const [sortField, setSortField] = useState('firstName');
  const [sortDirection, setSortDirection] = useState('asc');

  const [totalTeachersCount, setTotalTeachersCount] = useState(0);
  const [teachersCount, setTeachersCount] = useState(0);

  const fetchTeachers = useCallback(async () => {
    try {
      setLoading(true);

      const searchParams = {
        searchQuery,
        searchField,
        registrationFilter,
      };

      const response = await AdminService.getTeachers(
        currentPage,
        searchParams
      );
      setTeachers(response.content);
      setTotalPages(response.totalPages);
      setTeachersCount(response.totalElements);

      if (response.content.length === 0 && currentPage > 0) {
        setCurrentPage(0);
      }
    } catch (error) {
      toast.error(
        error.message ||
          t('teachersPage.errorLoadingTeachers', 'Error loading teachers')
      );
    } finally {
      setLoading(false);
    }
  }, [currentPage, searchQuery, searchField, registrationFilter, t]);

  const fetchTotalCount = useCallback(async () => {
    try {
      const stats = await AdminService.getAdminStats();
      setTotalTeachersCount(stats.teachersCount);
    } catch {}
  }, []);

  useEffect(() => {
    fetchTeachers();
  }, [fetchTeachers]);

  useEffect(() => {
    fetchTotalCount();
  }, [fetchTotalCount]);

  const openTeacherDetails = (teacher, directEdit = false) => {
    setSelectedTeacher(teacher);
    setDirectEditMode(directEdit);
    setShowDetailsModal(true);
  };

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

    const sortedTeachers = [...teachers].sort((a, b) => {
      let valueA, valueB;

      if (field === 'name') {
        valueA = `${a.firstName} ${a.lastName}`.toLowerCase();
        valueB = `${b.firstName} ${b.lastName}`.toLowerCase();
      } else if (field === 'lastLogin') {
        valueA = a.lastLogin ? new Date(a.lastLogin).getTime() : 0;
        valueB = b.lastLogin ? new Date(b.lastLogin).getTime() : 0;
      } else {
        valueA = (a[field] || '').toLowerCase();
        valueB = (b[field] || '').toLowerCase();
      }

      if (sortDirection === 'asc') {
        return valueA > valueB ? 1 : -1;
      }
      return valueA < valueB ? 1 : -1;
    });

    setTeachers(sortedTeachers);
  };

  const columns = [
    {
      key: 'name',
      label: t('teachersPage.name', 'Name'),
      sortable: true,
    },
    {
      key: 'username',
      label: t('teachersPage.username', 'Username'),
      sortable: true,
    },
    {
      key: 'email',
      label: t('teachersPage.email', 'Email'),
      sortable: true,
    },
    {
      key: 'lastLogin',
      label: t('teachersPage.lastLogin', 'Last Login'),
      sortable: true,
    },
    {
      key: 'actions',
      label: t('teachersPage.actions', 'Actions'),
      sortable: false,
    },
  ];

  const renderRow = teacher => (
    <tr key={teacher.username} className="hover:bg-gray-50">
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="flex items-center">
          <div className="h-10 w-10 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0">
            <span className="text-sm font-medium text-indigo-600">
              {`${teacher.firstName.charAt(0)}${teacher.lastName.charAt(0)}`}
            </span>
          </div>
          <div className="ml-4">
            <div className="text-sm font-medium text-gray-900">
              {`${teacher.firstName} ${teacher.lastName}`}
              {!teacher.registered && (
                <StatusBadge status="ldap" className="ml-2" />
              )}
            </div>
          </div>
        </div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {teacher.username}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {teacher.email}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
        {teacher.lastLogin
          ? new Date(teacher.lastLogin).toLocaleString()
          : t('teachersPage.neverLoggedIn', 'Never logged in')}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
        <button
          onClick={() => openTeacherDetails(teacher)}
          className="text-purple-600 hover:text-purple-900 mr-3"
        >
          {t('teachersPage.view', 'View')}
        </button>
        {teacher.registered && (
          <button
            className="text-indigo-600 hover:text-indigo-900 mr-3"
            onClick={() => openTeacherDetails(teacher, true)}
          >
            {t('teachersPage.edit', 'Edit')}
          </button>
        )}
      </td>
    </tr>
  );

  const filters = [
    {
      value: searchField,
      onChange: e => {
        setSearchField(e.target.value);
        setCurrentPage(0);
      },
      options: [
        { value: 'all', label: t('teachersPage.allFields', 'All Fields') },
        { value: 'name', label: t('teachersPage.name', 'Name') },
        { value: 'email', label: t('teachersPage.email', 'Email') },
        { value: 'username', label: t('teachersPage.username', 'Username') },
      ],
    },
    {
      value: registrationFilter,
      onChange: e => {
        setRegistrationFilter(e.target.value);
        setCurrentPage(0);
      },
      options: [
        { value: 'all', label: t('teachersPage.allUsers', 'All Users') },
        {
          value: 'ldap',
          label: t('teachersPage.ldapUsersOnly', 'LDAP Users Only'),
        },
        {
          value: 'registered',
          label: t('teachersPage.registeredUsersOnly', 'Registered Users Only'),
        },
      ],
    },
  ];

  return (
    <TablePageLayout
      icon={<AcademicCapIcon />}
      title={t('teachersPage.title', 'Teachers Management')}
      description={t(
        'teachersPage.description',
        'Manage all teachers in the system'
      )}
      variant="purple-indigo"
    >
      <SearchBar
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder={t('teachersPage.searchPlaceholder', 'Search teachers...')}
        filters={filters}
        itemCount={teachersCount}
        itemName={t('teachersPage.teachers', 'teachers')}
      />

      <DataTable
        columns={columns}
        data={teachers}
        loading={loading}
        searchQuery={searchQuery}
        onSort={handleSortChange}
        sortField={sortField}
        sortDirection={sortDirection}
        renderRow={renderRow}
        emptyMessage={t('teachersPage.noTeachersFound', 'No teachers found')}
      />

      <div className="mt-6">
        <SharedPagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={
            searchQuery || registrationFilter !== 'all'
              ? teachersCount
              : totalTeachersCount
          }
          onPageChange={setCurrentPage}
          itemName={t('teachersPage.teachers', 'teachers')}
          itemsPerPage={itemsPerPage}
        />
      </div>

      {showDetailsModal && selectedTeacher && (
        <TeacherDetailsModal
          teacher={selectedTeacher}
          closeModal={() => {
            setShowDetailsModal(false);
            setDirectEditMode(false);
          }}
          onUpdate={() => {
            setShowDetailsModal(false);
            setDirectEditMode(false);
            fetchTeachers();
          }}
          directEdit={directEditMode}
        />
      )}
    </TablePageLayout>
  );
};

export default TeachersPage;
