import { useState, useEffect, useCallback } from 'react';
import InfoCard from '@/components/shared/InfoCard';
import { AdminService } from '@/services/AdminService';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

const GroupStudentsTab = ({ group, onUpdate }) => {
  const { t } = useTranslation();
  const [currentGroupMembers, setCurrentGroupMembers] = useState([]);
  const [availableStudents, setAvailableStudents] = useState([]);
  const [selectedStudents, setSelectedStudents] = useState(new Set());
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [isCurrentGroupExpanded, setIsCurrentGroupExpanded] = useState(true);

  useEffect(() => {
    const handler = window.setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
      setCurrentPage(0);
    }, 300);

    return () => window.clearTimeout(handler);
  }, [searchQuery]);

  useEffect(() => {
    if (group?.students) {
      setCurrentGroupMembers(group.students);
      const initialStudents = new Set(
        group.students.map(student => student.userId)
      );
      setSelectedStudents(initialStudents);
    }
  }, [group]);

  useEffect(() => {
    if (group?.id) {
      fetchStudents();
    }
  }, [group?.id, debouncedSearchQuery, currentPage]);

  const fetchStudents = useCallback(async () => {
    try {
      setIsLoading(true);

      const response = await AdminService.getGroupStudents(
        currentPage,
        group.id,
        debouncedSearchQuery,
        false
      );

      if (response.currentMembers) {
        setCurrentGroupMembers(response.currentMembers);
      }

      if (response.availableStudents) {
        setAvailableStudents(response.availableStudents.content || []);
        setTotalPages(response.availableStudents.totalPages || 0);
      }
    } catch {
      toast.error(t('groupStudentsTab.fetchError', 'Failed to load students'));
    } finally {
      setIsLoading(false);
    }
  }, [group?.id, currentPage, debouncedSearchQuery, t]);

  const toggleStudent = useCallback(studentId => {
    setSelectedStudents(prev => {
      const newSet = new Set(prev);
      if (newSet.has(studentId)) {
        newSet.delete(studentId);
      } else {
        newSet.add(studentId);
      }
      return newSet;
    });
  }, []);

  const handleSave = useCallback(async () => {
    if (selectedStudents.size === 0) {
      toast.error(
        t(
          'groupStudentsTab.selectStudentError',
          'Please select at least one student'
        )
      );
      return;
    }

    try {
      const updateData = {
        groupName: group.name,
        teacherId: group.teacher?.userId?.toString() || '',
        studentsIds: Array.from(selectedStudents),
        semester: group.semester,
        academicYear: group.academicYear,
        active: group.active,
      };

      await AdminService.updateGroup(group.id, updateData);
      toast.success(
        t('groupStudentsTab.updateSuccess', 'Students updated successfully')
      );
      onUpdate();
    } catch (error) {
      toast.error(
        error.message ||
          t('groupStudentsTab.updateError', 'Error updating students')
      );
    }
  }, [group, selectedStudents, onUpdate, t]);

  const isStudentSelected = studentId => selectedStudents.has(studentId);

  const renderStudentRow = student => {
    const isSelected = isStudentSelected(student.userId);

    return (
      <li
        key={student.userId}
        className={`px-4 py-3 flex items-center hover:bg-gray-50 cursor-pointer ${
          isSelected ? 'bg-purple-100' : ''
        }`}
        onClick={() => toggleStudent(student.userId)}
      >
        <div className="min-w-0 flex-1">
          <div className="flex items-center">
            <p className="text-sm font-medium text-gray-900 truncate">
              {student.name} {student.surname}
            </p>
            <p className="ml-2 text-sm text-gray-500 truncate">
              @{student.username}
            </p>
          </div>
        </div>
        <div className="ml-4">
          <input
            type="checkbox"
            checked={isSelected}
            onChange={() => toggleStudent(student.userId)}
            className="h-4 w-4 focus:ring-purple-500 border-gray-300 rounded text-purple-600"
            onClick={e => e.stopPropagation()}
          />
        </div>
      </li>
    );
  };

  return (
    <InfoCard title={t('groupStudentsTab.manageStudents', 'Manage Students')}>
      <div className="flex justify-between items-center mb-4">
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
          {t('groupStudentsTab.selectedCount', '{{count}} selected', {
            count: selectedStudents.size,
          })}
        </span>
      </div>

      <div className="mb-6">
        <div
          className="bg-purple-50 px-4 py-3 border border-purple-200 rounded-t-lg flex justify-between items-center cursor-pointer"
          onClick={() => setIsCurrentGroupExpanded(!isCurrentGroupExpanded)}
        >
          <h4 className="text-sm font-medium text-purple-700">
            {t(
              'groupStudentsTab.currentMembers',
              'Current Group Members ({{count}})',
              { count: currentGroupMembers.length }
            )}
          </h4>
          <svg
            className={`h-5 w-5 text-purple-700 transform transition-transform ${isCurrentGroupExpanded ? 'rotate-180' : ''}`}
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
              clipRule="evenodd"
            />
          </svg>
        </div>

        {isCurrentGroupExpanded && (
          <div className="bg-white border border-t-0 border-gray-200 rounded-b-lg overflow-hidden">
            <ul className="divide-y divide-gray-200 max-h-96 overflow-y-auto">
              {currentGroupMembers.length > 0 ? (
                currentGroupMembers.map(renderStudentRow)
              ) : (
                <li className="px-4 py-6 text-center text-sm text-gray-500">
                  {t(
                    'groupStudentsTab.noStudentsInGroup',
                    'No students in this group.'
                  )}
                </li>
              )}
            </ul>
          </div>
        )}
      </div>

      <div className="mb-4">
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg
              className="h-5 w-5 text-gray-400"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="focus:ring-purple-500 focus:border-purple-500 block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md"
            placeholder={t(
              'groupStudentsTab.searchStudents',
              'Search students to add to this group...'
            )}
          />
        </div>
      </div>

      <div className="mb-6">
        <div className="bg-gray-50 px-4 py-3 border border-gray-200 rounded-t-lg">
          <h4 className="text-sm font-medium text-gray-700">
            {t(
              'groupStudentsTab.availableStudents',
              'Students Available to Add'
            )}
          </h4>
        </div>
        <div className="bg-white border border-t-0 border-gray-200 rounded-b-lg overflow-hidden">
          <ul className="divide-y divide-gray-200 max-h-[32rem] overflow-y-auto">
            {isLoading ? (
              <li className="px-4 py-12 flex justify-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600" />
              </li>
            ) : availableStudents.length > 0 ? (
              availableStudents.map(renderStudentRow)
            ) : (
              <li className="px-4 py-6 text-center text-sm text-gray-500">
                {searchQuery
                  ? t(
                      'groupStudentsTab.noMatchingStudents',
                      'No students matching your search.'
                    )
                  : t(
                      'groupStudentsTab.noAvailableStudents',
                      'No students available.'
                    )}
              </li>
            )}
          </ul>
        </div>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between border-t border-gray-200 px-4 py-3">
          <div>
            <p className="text-sm text-gray-700">
              {t('groupStudentsTab.pageInfo', 'Page {{current}} of {{total}}', {
                current: currentPage + 1,
                total: totalPages,
              })}
            </p>
          </div>
          <div>
            <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
              <button
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
                className={`relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 ${
                  currentPage === 0 ? 'cursor-not-allowed' : ''
                }`}
              >
                <svg
                  className="h-5 w-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>
              <button
                onClick={() =>
                  setCurrentPage(Math.min(totalPages - 1, currentPage + 1))
                }
                disabled={currentPage >= totalPages - 1}
                className={`relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 ${
                  currentPage >= totalPages - 1 ? 'cursor-not-allowed' : ''
                }`}
              >
                <svg
                  className="h-5 w-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>
            </nav>
          </div>
        </div>
      )}

      <div className="mt-6">
        <button
          type="button"
          onClick={handleSave}
          className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700"
        >
          {t('groupStudentsTab.saveChanges', 'Save Changes')}
        </button>
      </div>
    </InfoCard>
  );
};

export default GroupStudentsTab;
