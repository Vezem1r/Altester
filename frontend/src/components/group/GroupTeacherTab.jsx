import { useState, useEffect, useCallback } from 'react';
import InfoCard from '@/components/shared/InfoCard';
import { AdminService } from '@/services/AdminService';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

const GroupTeacherTab = ({ group, onUpdate }) => {
  const { t } = useTranslation();
  const [teachers, setTeachers] = useState([]);
  const [selectedTeacher, setSelectedTeacher] = useState('');
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
      setCurrentPage(0);
    }, 300);

    return () => clearTimeout(handler);
  }, [searchQuery]);

  useEffect(() => {
    fetchTeachers();
  }, [currentPage, debouncedSearchQuery]);

  useEffect(() => {
    if (group?.teacher) {
      setSelectedTeacher(group.teacher.userId.toString());
    }
  }, [group]);

  const fetchTeachers = useCallback(async () => {
    try {
      setLoading(true);
      const response = await AdminService.getGroupTeachers(
        currentPage,
        debouncedSearchQuery
      );
      setTeachers(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      toast.error(t('groupTeacherTab.fetchError', 'Failed to load teachers'));
    } finally {
      setLoading(false);
    }
  }, [currentPage, debouncedSearchQuery, t]);

  const handleSave = useCallback(async () => {
    if (!selectedTeacher) {
      toast.error(
        t('groupTeacherTab.selectTeacherError', 'Please select a teacher')
      );
      return;
    }

    try {
      const updateData = {
        groupName: group.name,
        teacherId: selectedTeacher,
        studentsIds: group.students ? group.students.map(s => s.userId) : [],
        semester: group.semester,
        academicYear: group.academicYear,
        active: group.active,
      };

      await AdminService.updateGroup(group.id, updateData);
      toast.success(
        t('groupTeacherTab.assignSuccess', 'Teacher assigned successfully')
      );
      onUpdate();
    } catch (error) {
      toast.error(
        error.message ||
          t('groupTeacherTab.assignError', 'Error assigning teacher')
      );
    }
  }, [group, selectedTeacher, onUpdate, t]);

  return (
    <InfoCard
      title={t('groupTeacherTab.teacherAssignment', 'Teacher Assignment')}
      description={t(
        'groupTeacherTab.description',
        'Select a teacher who will be responsible for this group'
      )}
    >
      <div className="bg-purple-50 p-4 rounded-lg border border-purple-200 mb-6">
        <p className="text-sm text-purple-700">
          {t(
            'groupTeacherTab.teacherCapabilities',
            'The teacher will be able to manage tests and view student results.'
          )}
        </p>
      </div>

      <div className="mb-4">
        <div className="relative rounded-md shadow-sm">
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
            className="focus:ring-purple-500 focus:border-purple-500 block w-full pl-10 pr-3 sm:text-sm border-gray-300 rounded-md"
            placeholder={t(
              'groupTeacherTab.searchTeachers',
              'Search teachers...'
            )}
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600" />
        </div>
      ) : (
        <>
          <div className="bg-white overflow-hidden border border-gray-200 rounded-md">
            <ul className="divide-y divide-gray-200">
              {teachers.length > 0 ? (
                teachers.map(teacher => (
                  <li
                    key={teacher.userId}
                    className={`px-4 py-4 hover:bg-gray-50 cursor-pointer transition-colors ${
                      selectedTeacher === teacher.userId.toString()
                        ? 'bg-purple-50'
                        : ''
                    }`}
                    onClick={() =>
                      setSelectedTeacher(teacher.userId.toString())
                    }
                  >
                    <div className="flex items-center">
                      <div className="flex-shrink-0">
                        <div className="h-10 w-10 rounded-full bg-purple-100 flex items-center justify-center text-purple-500 font-medium">
                          {teacher.name[0]}
                          {teacher.surname[0]}
                        </div>
                      </div>
                      <div className="ml-4 flex-grow">
                        <div className="text-sm font-medium text-gray-900">
                          {teacher.name} {teacher.surname}
                        </div>
                        <div className="text-sm text-gray-500">
                          @{teacher.username}
                        </div>
                      </div>
                      <div className="ml-4">
                        <input
                          type="radio"
                          name="teacher"
                          value={teacher.userId}
                          checked={
                            selectedTeacher === teacher.userId.toString()
                          }
                          onChange={e => e.stopPropagation()}
                          onClick={e => e.stopPropagation()}
                          className="focus:ring-purple-500 h-4 w-4 text-purple-600 border-gray-300 pointer-events-none"
                        />
                      </div>
                    </div>
                  </li>
                ))
              ) : (
                <li className="px-4 py-6 text-center text-gray-500">
                  {searchQuery
                    ? t(
                        'groupTeacherTab.noMatchingTeachers',
                        'No teachers found matching your search.'
                      )
                    : t('groupTeacherTab.noTeachers', 'No teachers available.')}
                </li>
              )}
            </ul>
          </div>

          {totalPages > 1 && (
            <div className="flex justify-between items-center mt-4">
              <button
                type="button"
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
                className={`px-4 py-2 border rounded-md text-sm ${
                  currentPage === 0
                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                    : 'bg-white text-gray-700 hover:bg-gray-50'
                }`}
              >
                {t('groupTeacherTab.previous', 'Previous')}
              </button>
              <span className="text-sm text-gray-500">
                {t(
                  'groupTeacherTab.pageInfo',
                  'Page {{current}} of {{total}}',
                  { current: currentPage + 1, total: totalPages }
                )}
              </span>
              <button
                type="button"
                onClick={() =>
                  setCurrentPage(Math.min(totalPages - 1, currentPage + 1))
                }
                disabled={currentPage >= totalPages - 1}
                className={`px-4 py-2 border rounded-md text-sm ${
                  currentPage >= totalPages - 1
                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                    : 'bg-white text-gray-700 hover:bg-gray-50'
                }`}
              >
                {t('groupTeacherTab.next', 'Next')}
              </button>
            </div>
          )}

          <div className="mt-6">
            <button
              type="button"
              onClick={handleSave}
              className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700"
            >
              {t('groupTeacherTab.saveAssignment', 'Save Teacher Assignment')}
            </button>
          </div>
        </>
      )}
    </InfoCard>
  );
};

export default GroupTeacherTab;
