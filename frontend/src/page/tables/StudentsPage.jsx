import { useState, useEffect, useCallback } from 'react';
import { AdminService } from '@/services/AdminService';
import { TeacherService } from '@/services/TeacherService';
import { toast } from 'react-toastify';
import { UserIcon } from '@heroicons/react/outline';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';

import TablePageLayout from '@/layouts/TablePageLayout';
import DataTable from '@/components/table/DataTable';
import SearchBar from '@/components/search/SearchBar';
import SharedPagination from '@/components/common/SharedPagination';
import StatusBadge from '@/components/ui/StatusBadge';
import StudentDetailsModal from '@/components/modals/StudentDetailsModal';

const StudentsPage = () => {
  const { t } = useTranslation();
  const { userRole } = useAuth();
  const isTeacherRole = userRole === 'TEACHER';

  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const itemsPerPage = 20;
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [directEditMode, setDirectEditMode] = useState(false);

  const [searchQuery, setSearchQuery] = useState('');
  const [searchField, setSearchField] = useState('all');
  const [registrationFilter, setRegistrationFilter] = useState('all');
  const [sortField, setSortField] = useState('firstName');
  const [sortDirection, setSortDirection] = useState('asc');

  const [totalStudentsCount, setTotalStudentsCount] = useState(0);
  const [studentsCount, setStudentsCount] = useState(0);

  const fetchStudents = useCallback(async () => {
    try {
      setLoading(true);

      if (isTeacherRole) {
        const response = await TeacherService.getTeacherStudents(
          currentPage,
          itemsPerPage,
          searchQuery
        );
        setStudents(response.content);
        setTotalPages(response.totalPages);
        setStudentsCount(response.totalElements);
        setTotalStudentsCount(response.totalElements);
      } else {
        const searchParams = {
          searchQuery,
          searchField,
          registrationFilter,
        };

        const response = await AdminService.getStudents(
          currentPage,
          searchParams
        );
        setStudents(response.content);
        setTotalPages(response.totalPages);
        setStudentsCount(response.totalElements);
      }

      if (students.length === 0 && currentPage > 0) {
        setCurrentPage(0);
      }
    } catch (error) {
      toast.error(
        error.message ||
          t('studentsPage.errorLoadingStudents', 'Error loading students')
      );
    } finally {
      setLoading(false);
    }
  }, [
    isTeacherRole,
    currentPage,
    itemsPerPage,
    searchQuery,
    searchField,
    registrationFilter,
    students.length,
    t,
  ]);

  const fetchTotalCount = useCallback(async () => {
    try {
      const stats = await AdminService.getAdminStats();
      setTotalStudentsCount(stats.studentsCount);
    } catch {}
  }, []);

  useEffect(() => {
    fetchStudents();
  }, [fetchStudents]);

  useEffect(() => {
    if (!isTeacherRole) {
      fetchTotalCount();
    }
  }, [isTeacherRole, fetchTotalCount]);

  const openStudentDetails = (student, directEdit = false) => {
    setSelectedStudent(student);
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

    const sortedStudents = [...students].sort((a, b) => {
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

    setStudents(sortedStudents);
  };

  const handleGroupClick = (groupId, studentUsername) => {
    if (isTeacherRole) {
      window.location.href = `/teacher/groups?groupId=${groupId}&studentUsername=${studentUsername}`;
    }
  };

  const getStudentGroups = student => {
    if (
      isTeacherRole &&
      student.subjectGroups &&
      student.subjectGroups.length > 0
    ) {
      const groups = student.subjectGroups;

      if (groups.length > 2) {
        return (
          <div className="flex flex-wrap gap-1">
            {groups.slice(0, 2).map(group => {
              const isActive = group.active !== undefined ? group.active : true;
              const isFuture =
                group.future !== undefined ? group.future : false;

              let buttonStyle = '';

              if (isActive) {
                buttonStyle =
                  'bg-purple-100 text-purple-800 hover:bg-purple-200';
              } else if (isFuture) {
                buttonStyle = 'bg-blue-100 text-blue-800 hover:bg-blue-200';
              } else {
                buttonStyle = 'bg-gray-100 text-gray-600 hover:bg-gray-200';
              }

              return (
                <button
                  key={`${student.username}-group-${group.id}`}
                  onClick={() => handleGroupClick(group.id, student.username)}
                  className={`px-2 py-0.5 inline-flex text-xs leading-5 font-semibold rounded-full 
                    ${buttonStyle} cursor-pointer transition-all`}
                  title={t(
                    'studentsPage.viewStudentInGroup',
                    'View {{firstName}} {{lastName}} in {{groupName}} group',
                    {
                      firstName: student.firstName,
                      lastName: student.lastName,
                      groupName: group.name,
                    }
                  )}
                >
                  {group.name}
                </button>
              );
            })}
            <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800">
              +{groups.length - 2} {t('studentsPage.more', 'more...')}
            </span>
          </div>
        );
      }

      return (
        <div className="flex flex-wrap gap-1">
          {groups.map(group => {
            const isActive = group.active !== undefined ? group.active : true;
            const isFuture = group.future !== undefined ? group.future : false;

            let buttonStyle = '';

            if (isActive) {
              buttonStyle = 'bg-purple-100 text-purple-800 hover:bg-purple-200';
            } else if (isFuture) {
              buttonStyle = 'bg-blue-100 text-blue-800 hover:bg-blue-200';
            } else {
              buttonStyle = 'bg-gray-100 text-gray-600 hover:bg-gray-200';
            }

            return (
              <button
                key={`${student.username}-group-${group.id}`}
                onClick={() => handleGroupClick(group.id, student.username)}
                className={`px-2 py-0.5 inline-flex text-xs leading-5 font-semibold rounded-full 
                  ${buttonStyle} cursor-pointer transition-all`}
                title={t(
                  'studentsPage.viewStudentInGroup',
                  'View {{firstName}} {{lastName}} in {{groupName}} group',
                  {
                    firstName: student.firstName,
                    lastName: student.lastName,
                    groupName: group.name,
                  }
                )}
              >
                {group.name}
              </button>
            );
          })}
        </div>
      );
    }
    return null;
  };

  const columns = [
    {
      key: 'name',
      label: t('studentsPage.name', 'Name'),
      sortable: true,
    },
    {
      key: 'username',
      label: t('studentsPage.username', 'Username'),
      sortable: true,
    },
    {
      key: 'email',
      label: t('studentsPage.email', 'Email'),
      sortable: true,
    },
    ...(isTeacherRole
      ? [
          {
            key: 'groups',
            label: t('studentsPage.groups', 'Groups'),
            sortable: false,
          },
        ]
      : []),
    {
      key: 'lastLogin',
      label: t('studentsPage.lastLogin', 'Last Login'),
      sortable: true,
    },
    {
      key: 'actions',
      label: t('studentsPage.actions', 'Actions'),
      sortable: false,
    },
  ];

  const renderRow = student => (
    <tr key={student.username} className="hover:bg-gray-50">
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="flex items-center">
          <div className="h-10 w-10 rounded-full bg-purple-100 flex items-center justify-center flex-shrink-0">
            <span className="text-sm font-medium text-purple-600">
              {`${student.firstName.charAt(0)}${student.lastName.charAt(0)}`}
            </span>
          </div>
          <div className="ml-4">
            <div className="text-sm font-medium text-gray-900">
              {`${student.firstName} ${student.lastName}`}
              {!isTeacherRole && !student.registered && (
                <StatusBadge status="ldap" className="ml-2" />
              )}
            </div>
          </div>
        </div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {student.username}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {student.email}
      </td>
      {isTeacherRole && (
        <td className="px-6 py-4">{getStudentGroups(student)}</td>
      )}
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
        {student.lastLogin
          ? new Date(student.lastLogin).toLocaleString()
          : t('studentsPage.neverLoggedIn', 'Never logged in')}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
        <button
          onClick={() => openStudentDetails(student)}
          className="text-purple-600 hover:text-purple-900 mr-3"
        >
          {t('studentsPage.view', 'View')}
        </button>
        {!isTeacherRole && student.registered && (
          <button
            className="text-indigo-600 hover:text-indigo-900 mr-3"
            onClick={() => openStudentDetails(student, true)}
          >
            {t('studentsPage.edit', 'Edit')}
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
        { value: 'all', label: t('studentsPage.allFields', 'All Fields') },
        { value: 'name', label: t('studentsPage.name', 'Name') },
        { value: 'email', label: t('studentsPage.email', 'Email') },
        { value: 'username', label: t('studentsPage.username', 'Username') },
      ],
    },
    ...(isTeacherRole
      ? []
      : [
          {
            value: registrationFilter,
            onChange: e => {
              setRegistrationFilter(e.target.value);
              setCurrentPage(0);
            },
            options: [
              { value: 'all', label: t('studentsPage.allUsers', 'All Users') },
              {
                value: 'ldap',
                label: t('studentsPage.ldapUsersOnly', 'LDAP Users Only'),
              },
              {
                value: 'registered',
                label: t(
                  'studentsPage.registeredUsersOnly',
                  'Registered Users Only'
                ),
              },
            ],
          },
        ]),
  ];

  return (
    <TablePageLayout
      icon={<UserIcon />}
      title={t('studentsPage.title', 'Students Management')}
      description={
        isTeacherRole
          ? t(
              'studentsPage.teacherDescription',
              'View all your students information'
            )
          : t(
              'studentsPage.adminDescription',
              'Manage all students in the system'
            )
      }
      variant="purple-light"
    >
      <SearchBar
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder={t('studentsPage.searchPlaceholder', 'Search students...')}
        filters={filters}
        itemCount={studentsCount}
        itemName={t('studentsPage.students', 'students')}
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
                  'studentsPage.groupBadgeTip',
                  'Click on any group badge to navigate to that group and highlight the student.'
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      <DataTable
        columns={columns}
        data={students}
        loading={loading}
        searchQuery={searchQuery}
        onSort={handleSortChange}
        sortField={sortField}
        sortDirection={sortDirection}
        renderRow={renderRow}
        emptyMessage={t('studentsPage.noStudentsFound', 'No students found')}
      />

      <div className="mt-6">
        <SharedPagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={
            searchQuery || (!isTeacherRole && registrationFilter !== 'all')
              ? studentsCount
              : totalStudentsCount
          }
          onPageChange={setCurrentPage}
          itemName={t('studentsPage.students', 'students')}
          itemsPerPage={itemsPerPage}
        />
      </div>

      {showDetailsModal && selectedStudent && (
        <StudentDetailsModal
          student={selectedStudent}
          closeModal={() => {
            setShowDetailsModal(false);
            setDirectEditMode(false);
          }}
          onUpdate={() => {
            setShowDetailsModal(false);
            setDirectEditMode(false);
            fetchStudents();
          }}
          directEdit={directEditMode}
          readOnly={isTeacherRole}
        />
      )}
    </TablePageLayout>
  );
};

export default StudentsPage;
