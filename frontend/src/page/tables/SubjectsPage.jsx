import { useState, useEffect, useCallback } from 'react';
import { AdminService } from '@/services/AdminService';
import { toast } from 'react-toastify';
import { BookOpenIcon, PlusIcon } from '@heroicons/react/outline';
import { useTranslation } from 'react-i18next';

import TablePageLayout from '@/layouts/TablePageLayout';
import DataTable from '@/components/table/DataTable';
import SearchBar from '@/components/search/SearchBar';
import SharedPagination from '@/components/common/SharedPagination';
import SubjectDetailsModal from '@/components/modals/SubjectDetailsModal';
import CreateSubjectModal from '@/components/modals/CreateSubjectModal';
import { s } from 'framer-motion/client';

const SubjectsPage = () => {
  const { t } = useTranslation();
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const itemsPerPage = 12;

  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedSubject, setSelectedSubject] = useState(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [sortField, setSortField] = useState('name');
  const [sortDirection, setSortDirection] = useState('asc');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');
  const [totalSubjectsCount, setTotalSubjectsCount] = useState(0);

  const extractErrorMessage = useCallback(
    error => {
      if (error.response && error.response.data) {
        if (typeof error.response.data === 'string') {
          return error.response.data;
        }
        if (error.response.data.message) {
          return error.response.data.message;
        }
      }

      if (error.message) {
        if (error.message.includes('short name')) {
          return t(
            'subjectsPage.shortNameInUse',
            'The subject short name is already in use. Please choose a different one.'
          );
        }
        return error.message;
      }

      return t('subjectsPage.unexpectedError', 'An unexpected error occurred');
    },
    [t]
  );

  const handleApiError = useCallback(
    (error, defaultMessage) => {
      const errorMessage = extractErrorMessage(error);
      toast.error(errorMessage || defaultMessage);
    },
    [extractErrorMessage]
  );

  const fetchSubjects = useCallback(async () => {
    try {
      setLoading(true);
      const response = await AdminService.getAllSubjects(
        currentPage,
        debouncedSearchQuery
      );
      setSubjects(response.content);
      setTotalPages(response.totalPages);
      setTotalSubjectsCount(response.totalElements);

      if (response.totalElements === 0 && currentPage > 0) {
        setCurrentPage(0);
      }
    } catch (error) {
      handleApiError(
        error,
        t('subjectsPage.errorLoadingSubjects', 'Error loading subjects')
      );
    } finally {
      setLoading(false);
    }
  }, [currentPage, debouncedSearchQuery, t, handleApiError]);

  const fetchTotalCount = useCallback(async () => {
    try {
      await AdminService.getAdminStats();
    } catch {}
  }, []);

  useEffect(() => {
    const handler = window.setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
    }, 300);

    return () => {
      window.clearTimeout(handler);
    };
  }, [searchQuery]);

  useEffect(() => {
    fetchSubjects();
  }, [fetchSubjects]);

  useEffect(() => {
    fetchTotalCount();
  }, [fetchTotalCount]);

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

    const sortedSubjects = [...subjects].sort((a, b) => {
      const valueA = (a[field] || '').toLowerCase();
      const valueB = (b[field] || '').toLowerCase();

      if (sortDirection === 'asc') {
        return valueA > valueB ? 1 : -1;
      }
      return valueA < valueB ? 1 : -1;
    });

    setSubjects(sortedSubjects);
  };

  const openSubjectDetails = subject => {
    setSelectedSubject(subject);
    setShowDetailsModal(true);
  };

  const handleCreateSubject = async subjectData => {
    try {
      await AdminService.createSubject(subjectData);
      toast.success(
        t('subjectsPage.subjectCreated', 'Subject created successfully')
      );
      setShowCreateModal(false);
      fetchSubjects();
    } catch (error) {
      handleApiError(
        error,
        t('subjectsPage.errorCreatingSubject', 'Error creating subject')
      );
    }
  };

  const handleUpdateSubject = async (subjectId, subjectData) => {
    try {
      await AdminService.updateSubject(subjectId, subjectData);
      toast.success(
        t('subjectsPage.subjectUpdated', 'Subject updated successfully')
      );
      setShowDetailsModal(false);
      fetchSubjects();
    } catch (error) {
      handleApiError(
        error,
        t('subjectsPage.errorUpdatingSubject', 'Error updating subject')
      );
    }
  };

  const handleDeleteSubject = async subjectId => {
    try {
      await AdminService.deleteSubject(subjectId);
      toast.success(
        t('subjectsPage.subjectDeleted', 'Subject deleted successfully')
      );
      setShowDetailsModal(false);
      fetchSubjects();
    } catch (error) {
      handleApiError(
        error,
        t('subjectsPage.errorDeletingSubject', 'Error deleting subject')
      );
    }
  };

  const columns = [
    {
      key: 'name',
      label: t('subjectsPage.name', 'Name'),
      sortable: true,
    },
    {
      key: 'shortName',
      label: t('subjectsPage.shortName', 'Short Name'),
      sortable: true,
    },
    {
      key: 'description',
      label: t('subjectsPage.description', 'Description'),
      sortable: false,
    },
    {
      key: 'modified',
      label: t('subjectsPage.modified', 'Modified'),
      sortable: true,
    },
    {
      key: 'groups',
      label: t('subjectsPage.groups', 'Groups'),
      sortable: false,
    },
    {
      key: 'actions',
      label: t('subjectsPage.actions', 'Actions'),
      sortable: false,
    },
  ];

  const formatDate = dateString => {
    if (!dateString) return null;

    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        return null;
      }

      const options = {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      };

      return date.toLocaleDateString(undefined, options);
    } catch {
      return null;
    }
  };

  const renderRow = subject => (
    <tr key={subject.id} className="hover:bg-gray-50">
      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
        {subject.name}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {subject.shortName}
      </td>
      <td className="px-6 py-4">
        <div className="text-sm text-gray-900 truncate max-w-xs">
          {subject.description ||
            t('subjectsPage.noDescription', 'No description')}
        </div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
        {formatDate(subject.modified) ||
          t('subjectsPage.notAvailable', 'Not available')}
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        {subject.groups ? (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            {subject.groups.length} {t('subjectsPage.groupsCount', 'groups')}
          </span>
        ) : (
          <span className="text-sm text-gray-500">
            0 {t('subjectsPage.groupsCount', 'groups')}
          </span>
        )}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
        <button
          onClick={() => openSubjectDetails(subject)}
          className="text-purple-600 hover:text-purple-900"
        >
          {t('subjectsPage.view', 'View')}
        </button>
      </td>
    </tr>
  );

  const primaryAction = (
    <button
      onClick={() => setShowCreateModal(true)}
      className="inline-flex items-center transition-colors duration-200"
    >
      <PlusIcon className="-ml-1 mr-2 h-5 w-5" aria-hidden="true" />
      {t('subjectsPage.addSubject', 'Add Subject')}
    </button>
  );

  return (
    <TablePageLayout
      icon={<BookOpenIcon />}
      title={t('subjectsPage.title', 'Subjects Management')}
      description={t(
        'subjectsPage.pageDescription',
        'Manage all subjects in the educational system'
      )}
      primaryAction={primaryAction}
      variant="purple-violet"
    >
      <SearchBar
        value={searchQuery}
        onChange={handleSearchChange}
        placeholder={t('subjectsPage.searchPlaceholder', 'Search subjects...')}
        itemCount={subjects.length}
        itemName={t('subjectsPage.subjects', 'subjects')}
      />

      <DataTable
        columns={columns}
        data={subjects}
        loading={loading}
        searchQuery={searchQuery}
        onSort={handleSortChange}
        sortField={sortField}
        sortDirection={sortDirection}
        renderRow={renderRow}
        emptyMessage={t('subjectsPage.noSubjectsFound', 'No subjects found')}
      />

      <div className="mt-6">
        <SharedPagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={searchQuery ? subjects.length : totalSubjectsCount}
          onPageChange={setCurrentPage}
          itemName={t('subjectsPage.subjects', 'subjects')}
          itemsPerPage={itemsPerPage}
        />
      </div>

      {showDetailsModal && selectedSubject && (
        <SubjectDetailsModal
          subject={selectedSubject}
          closeModal={() => setShowDetailsModal(false)}
          onUpdate={handleUpdateSubject}
          onDelete={handleDeleteSubject}
        />
      )}

      {showCreateModal && (
        <CreateSubjectModal
          closeModal={() => setShowCreateModal(false)}
          onSubmit={handleCreateSubject}
        />
      )}
    </TablePageLayout>
  );
};

export default SubjectsPage;
