import { useState, useCallback } from 'react';
import InfoCard from '@/components/shared/InfoCard';
import InfoItem from '@/components/shared/InfoItem';
import { AdminService } from '@/services/AdminService';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

const GroupDetailsTab = ({ group, onUpdate, isInactive }) => {
  const { t } = useTranslation();
  const [isEditingSemester, setIsEditingSemester] = useState(false);
  const [isEditingYear, setIsEditingYear] = useState(false);
  const [selectedSemester, setSelectedSemester] = useState(
    group?.semester || ''
  );
  const [academicYear, setAcademicYear] = useState(group?.academicYear || '');
  const [academicYearError, setAcademicYearError] = useState('');

  const getSemesterName = semester => {
    if (!semester) return t('groupDetailsTab.unknown', 'Unknown');

    const semesterMap = {
      WINTER: t('groupDetailsTab.winterSemester', 'Winter Semester'),
      SUMMER: t('groupDetailsTab.summerSemester', 'Summer Semester'),
    };

    return semesterMap[semester] || semester;
  };

  const handleUpdate = useCallback(
    async updateData => {
      try {
        await AdminService.updateGroup(group.id, updateData);
        toast.success(
          t('groupDetailsTab.updateSuccess', 'Group updated successfully')
        );
        onUpdate();
      } catch (error) {
        toast.error(
          error.message ||
            t('groupDetailsTab.updateError', 'Error updating group')
        );
      }
    },
    [group?.id, onUpdate, t]
  );

  const handleSemesterSave = useCallback(() => {
    if (!selectedSemester) return;

    const updateData = {
      groupName: group.name,
      teacherId: group.teacher?.userId?.toString() || '',
      studentsIds: group.students ? group.students.map(s => s.userId) : [],
      semester: selectedSemester,
      academicYear: group.academicYear,
      active: group.active,
    };

    handleUpdate(updateData);
    setIsEditingSemester(false);
  }, [group, selectedSemester, handleUpdate]);

  const handleYearSave = useCallback(() => {
    if (!academicYear) {
      setAcademicYearError(
        t('groupDetailsTab.academicYearRequired', 'Academic year is required')
      );
      return;
    }

    const yearNum = Number(academicYear);
    if (isNaN(yearNum) || yearNum < 2000 || yearNum > 2100) {
      setAcademicYearError(
        t(
          'groupDetailsTab.invalidYear',
          'Please enter a valid year (2000-2100)'
        )
      );
      return;
    }

    const updateData = {
      groupName: group.name,
      teacherId: group.teacher?.userId?.toString() || '',
      studentsIds: group.students ? group.students.map(s => s.userId) : [],
      semester: group.semester,
      academicYear: yearNum,
      active: group.active,
    };

    setAcademicYearError('');
    handleUpdate(updateData);
    setIsEditingYear(false);
  }, [group, academicYear, handleUpdate, t]);

  return (
    <>
      {group && !group.active && !group.inFuture && (
        <div className="mb-6 p-4 bg-gray-100 border-l-4 border-gray-400 rounded-md">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg
                className="h-5 w-5 text-gray-400"
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
              <p className="text-sm text-gray-700">
                <span className="font-medium text-gray-800">
                  {t(
                    'groupDetailsTab.pastSemesterGroup',
                    'This group is from a past semester'
                  )}
                </span>{' '}
                -{' '}
                {t(
                  'groupDetailsTab.pastSemesterInfo',
                  'You can view its details but cannot modify it.'
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      {group && group.inFuture && (
        <div className="mb-6 p-4 bg-blue-50 border-l-4 border-blue-400 rounded-md">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg
                className="h-5 w-5 text-blue-400"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-blue-700">
                <span className="font-medium text-blue-800">
                  {t('groupDetailsTab.futureGroup', 'Future Group')}
                </span>{' '}
                -{' '}
                {t(
                  'groupDetailsTab.futureGroupInfo',
                  "This group is scheduled for {{semester}} {{year}}. You can fully configure it now, but it won't be visible to students until the semester begins.",
                  {
                    semester: getSemesterName(group.semester),
                    year: group.academicYear,
                  }
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      <InfoCard
        title={t('groupDetailsTab.groupInformation', 'Group Information')}
        description={t(
          'groupDetailsTab.basicDetails',
          'Basic details about this group'
        )}
      >
        <dl className="grid grid-cols-1 gap-x-4 gap-y-6 sm:grid-cols-2">
          <InfoItem
            label={t('groupDetailsTab.groupName', 'Group Name')}
            value={group?.name || t('groupDetailsTab.noName', 'No name')}
          />
          <InfoItem
            label={t('groupDetailsTab.teacher', 'Teacher')}
            value={
              group?.teacher
                ? `${group.teacher.name} ${group.teacher.surname}`
                : t('groupDetailsTab.notAssigned', 'Not assigned')
            }
          />
          <InfoItem
            label={t('groupDetailsTab.subject', 'Subject')}
            value={
              group?.subject || t('groupDetailsTab.notAssigned', 'Not assigned')
            }
          />
          <InfoItem
            label={t('groupDetailsTab.students', 'Students')}
            value={t('groupDetailsTab.studentsCount', '{{count}} students', {
              count: group?.students?.length || 0,
            })}
          />

          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500 flex items-center">
              {t('groupDetailsTab.semester', 'Semester')}
              {!isInactive && !isEditingSemester && (
                <button
                  onClick={() => setIsEditingSemester(true)}
                  className="ml-2 text-gray-400 hover:text-gray-500 transition-colors"
                >
                  <svg
                    className="h-4 w-4"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                  </svg>
                </button>
              )}
            </dt>
            <dd className="mt-1 text-sm text-gray-900">
              {isEditingSemester ? (
                <div className="flex items-center">
                  <select
                    value={selectedSemester}
                    onChange={e => setSelectedSemester(e.target.value)}
                    className="shadow-sm focus:ring-purple-500 focus:border-purple-500 block w-full sm:text-sm border-gray-300 rounded-md"
                  >
                    <option value="" disabled>
                      {t('groupDetailsTab.selectSemester', 'Select a semester')}
                    </option>
                    <option value="WINTER">
                      {t('groupDetailsTab.winterSemester', 'Winter Semester')}
                    </option>
                    <option value="SUMMER">
                      {t('groupDetailsTab.summerSemester', 'Summer Semester')}
                    </option>
                  </select>
                  <button
                    onClick={handleSemesterSave}
                    className="ml-2 p-1.5 rounded-md text-white bg-purple-600 hover:bg-purple-700 transition-colors shadow-sm"
                    title={t('groupDetailsTab.save', 'Save')}
                  >
                    <svg
                      className="h-4 w-4"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M5 13l4 4L19 7"
                      />
                    </svg>
                  </button>
                  <button
                    onClick={() => {
                      setSelectedSemester(group?.semester || '');
                      setIsEditingSemester(false);
                    }}
                    className="ml-2 p-1.5 rounded-md text-white bg-gray-600 hover:bg-gray-700 transition-colors shadow-sm"
                    title={t('groupDetailsTab.cancel', 'Cancel')}
                  >
                    <svg
                      className="h-4 w-4"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M6 18L18 6M6 6l12 12"
                      />
                    </svg>
                  </button>
                </div>
              ) : (
                getSemesterName(group?.semester) ||
                t('groupDetailsTab.notSet', 'Not set')
              )}
            </dd>
          </div>

          <div className="sm:col-span-1">
            <dt className="text-sm font-medium text-gray-500 flex items-center">
              {t('groupDetailsTab.academicYear', 'Academic Year')}
              {!isInactive && !isEditingYear && (
                <button
                  onClick={() => setIsEditingYear(true)}
                  className="ml-2 text-gray-400 hover:text-gray-500 transition-colors"
                >
                  <svg
                    className="h-4 w-4"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                  </svg>
                </button>
              )}
            </dt>
            <dd className="mt-1 text-sm text-gray-900">
              {isEditingYear ? (
                <div className="flex flex-col">
                  <div className="flex items-center">
                    <input
                      type="number"
                      value={academicYear}
                      onChange={e => setAcademicYear(e.target.value)}
                      min="2000"
                      max="2100"
                      className={`shadow-sm focus:ring-purple-500 focus:border-purple-500 block w-full sm:text-sm border-gray-300 rounded-md ${
                        academicYearError ? 'border-red-300 bg-red-50' : ''
                      }`}
                    />
                    <button
                      onClick={handleYearSave}
                      className="ml-2 p-1.5 rounded-md text-white bg-purple-600 hover:bg-purple-700 transition-colors shadow-sm"
                      title={t('groupDetailsTab.save', 'Save')}
                    >
                      <svg
                        className="h-4 w-4"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                    </button>
                    <button
                      onClick={() => {
                        setAcademicYear(group?.academicYear || '');
                        setAcademicYearError('');
                        setIsEditingYear(false);
                      }}
                      className="ml-2 p-1.5 rounded-md text-white bg-gray-600 hover:bg-gray-700 transition-colors shadow-sm"
                      title={t('groupDetailsTab.cancel', 'Cancel')}
                    >
                      <svg
                        className="h-4 w-4"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </button>
                  </div>
                  {academicYearError && (
                    <p className="mt-1 text-xs text-red-600">
                      {academicYearError}
                    </p>
                  )}
                  <p className="mt-1 text-xs text-gray-500">
                    {t(
                      'groupDetailsTab.yearHint',
                      'Enter first year (e.g., for 2024-2025 academic year, enter 2024)'
                    )}
                  </p>
                </div>
              ) : (
                group?.academicYear || t('groupDetailsTab.notSet', 'Not set')
              )}
            </dd>
          </div>

          <div className="sm:col-span-1">
            <InfoItem
              label={t('groupDetailsTab.status', 'Status')}
              value={
                group?.inFuture ? (
                  <span className="px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800">
                    {t('groupDetailsTab.future', 'Future')}
                  </span>
                ) : group?.active ? (
                  <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800">
                    {t('groupDetailsTab.active', 'Active')}
                  </span>
                ) : (
                  <span className="px-2 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800">
                    {t('groupDetailsTab.inactive', 'Inactive')}
                  </span>
                )
              }
            />
          </div>
        </dl>
      </InfoCard>

      <InfoCard
        className="mt-6"
        title={t('groupDetailsTab.studentList', 'Student List')}
      >
        {group?.students && group.students.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    {t('groupDetailsTab.name', 'Name')}
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    {t('groupDetailsTab.username', 'Username')}
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {group.students.map(student => (
                  <tr key={student.userId}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {student.name} {student.surname}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      @{student.username}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-center text-gray-500">
            {t(
              'groupDetailsTab.noStudents',
              'No students assigned to this group.'
            )}
          </p>
        )}
      </InfoCard>
    </>
  );
};

export default GroupDetailsTab;
