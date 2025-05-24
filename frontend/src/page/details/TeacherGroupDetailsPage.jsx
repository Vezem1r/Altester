import { useState, useEffect, useMemo, useCallback } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { TeacherService } from '@/services/TeacherService';
import DetailsLayout from '@/layouts/DetailsLayout';
import PageHeader from '@/components/shared/PageHeader';
import InfoCard from '@/components/shared/InfoCard';
import { toast } from 'react-toastify';
import InfoItem from '@/components/shared/InfoItem';
import { useTranslation } from 'react-i18next';

const TeacherGroupDetailsPage = () => {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [group, setGroup] = useState(null);
  const [loading, setLoading] = useState(true);
  const [movingStudent, setMovingStudent] = useState(null);
  const [selectedTargetGroup, setSelectedTargetGroup] = useState('');
  const [showMoveDialog, setShowMoveDialog] = useState(false);

  const highlightedStudent = useMemo(() => {
    const params = new URLSearchParams(location.search);
    return params.get('highlight');
  }, [location.search]);

  const fetchGroupDetails = useCallback(async () => {
    try {
      setLoading(true);
      const data = await TeacherService.getTeacherGroup(id);
      setGroup(data);
    } catch {
      toast.error(
        t(
          'teacherGroupDetailsPage.failedToLoad',
          'Failed to load group details'
        )
      );
      navigate('/teacher/groups');
    } finally {
      setLoading(false);
    }
  }, [id, navigate, t]);

  useEffect(() => {
    fetchGroupDetails();
  }, [fetchGroupDetails]);

  const handleMoveClick = student => {
    setMovingStudent(student);
    setSelectedTargetGroup('');
    setShowMoveDialog(true);
  };

  const handleMoveStudent = async () => {
    if (!selectedTargetGroup) {
      toast.error(
        t(
          'teacherGroupDetailsPage.selectTargetGroup',
          'Please select a target group'
        )
      );
      return;
    }

    try {
      await TeacherService.moveStudent(
        movingStudent.username,
        parseInt(id),
        parseInt(selectedTargetGroup)
      );
      toast.success(
        t(
          'teacherGroupDetailsPage.studentMoved',
          'Student {{name}} {{surname}} moved successfully',
          {
            name: movingStudent.name,
            surname: movingStudent.surname,
          }
        )
      );
      setShowMoveDialog(false);
      fetchGroupDetails();
    } catch (error) {
      toast.error(
        error.message ||
          t(
            'teacherGroupDetailsPage.errorMovingStudent',
            'Error moving student'
          )
      );
    }
  };

  const getSemesterName = semester => {
    if (!semester) return t('teacherGroupDetailsPage.unknown', 'Unknown');

    const semesterMap = {
      WINTER: t('teacherGroupDetailsPage.winterSemester', 'Winter Semester'),
      SUMMER: t('teacherGroupDetailsPage.summerSemester', 'Summer Semester'),
    };

    return semesterMap[semester] || semester;
  };

  const badges = useMemo(() => {
    if (!group) return [];

    const badges = [];

    if (group.inFuture) {
      badges.push({
        text: t('teacherGroupDetailsPage.future', 'Future'),
        className: 'bg-blue-100 text-blue-800',
      });
    } else if (group.active) {
      badges.push({
        text: t('teacherGroupDetailsPage.active', 'Active'),
        className: 'bg-green-100 text-green-800',
      });
    } else {
      badges.push({
        text: t('teacherGroupDetailsPage.inactive', 'Inactive'),
        className: 'bg-gray-100 text-gray-800',
      });
    }

    return badges;
  }, [group, t]);

  const breadcrumbs = [
    {
      name: t('teacherGroupDetailsPage.groups', 'Groups'),
      href: '/teacher/groups',
    },
    {
      name:
        group?.name ||
        t('teacherGroupDetailsPage.groupDetails', 'Group Details'),
    },
  ];

  if (loading && !group) {
    return (
      <DetailsLayout>
        <div className="flex justify-center items-center py-24">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600" />
        </div>
      </DetailsLayout>
    );
  }

  return (
    <DetailsLayout breadcrumbs={breadcrumbs}>
      <PageHeader
        title={group?.name}
        badges={badges}
        backUrl="/teacher/groups"
      />

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
                  {t('teacherGroupDetailsPage.futureGroup', 'Future Group')}
                </span>{' '}
                -{' '}
                {t(
                  'teacherGroupDetailsPage.futureGroupDescription',
                  'This group is scheduled for {{semester}} {{year}}. You can move students between your groups now.',
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
        title={t(
          'teacherGroupDetailsPage.groupInformation',
          'Group Information'
        )}
        description={t(
          'teacherGroupDetailsPage.basicDetails',
          'Basic details about this group'
        )}
      >
        <dl className="grid grid-cols-1 gap-x-4 gap-y-6 sm:grid-cols-2">
          <InfoItem
            label={t('teacherGroupDetailsPage.groupName', 'Group Name')}
            value={
              group?.name || t('teacherGroupDetailsPage.noName', 'No name')
            }
          />
          <InfoItem
            label={t('teacherGroupDetailsPage.subject', 'Subject')}
            value={
              group?.subject ||
              t('teacherGroupDetailsPage.notAssigned', 'Not assigned')
            }
          />
          <InfoItem
            label={t('teacherGroupDetailsPage.semester', 'Semester')}
            value={
              getSemesterName(group?.semester) ||
              t('teacherGroupDetailsPage.notSet', 'Not set')
            }
          />
          <InfoItem
            label={t('teacherGroupDetailsPage.academicYear', 'Academic Year')}
            value={
              group?.academicYear ||
              t('teacherGroupDetailsPage.notSet', 'Not set')
            }
          />
          <InfoItem
            label={t('teacherGroupDetailsPage.students', 'Students')}
            value={t(
              'teacherGroupDetailsPage.studentsCount',
              '{{count}} students',
              { count: group?.students?.length || 0 }
            )}
          />
          <InfoItem
            label={t('teacherGroupDetailsPage.status', 'Status')}
            value={
              group?.inFuture ? (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-blue-100 text-blue-800">
                  {t('teacherGroupDetailsPage.future', 'Future')}
                </span>
              ) : group?.active ? (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800">
                  {t('teacherGroupDetailsPage.active', 'Active')}
                </span>
              ) : (
                <span className="px-2 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-800">
                  {t('teacherGroupDetailsPage.inactive', 'Inactive')}
                </span>
              )
            }
          />
        </dl>
      </InfoCard>

      <InfoCard
        className="mt-6"
        title={t('teacherGroupDetailsPage.studentList', 'Student List')}
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
                    {t('teacherGroupDetailsPage.name', 'Name')}
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    {t('teacherGroupDetailsPage.username', 'Username')}
                  </th>
                  <th
                    scope="col"
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                  >
                    {t('teacherGroupDetailsPage.actions', 'Actions')}
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {group.students.map(student => (
                  <tr
                    key={student.userId}
                    className={
                      highlightedStudent === student.username
                        ? 'bg-yellow-50'
                        : ''
                    }
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {student.name} {student.surname}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      @{student.username}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      {group.inFuture === false &&
                      group.otherTeacherGroups &&
                      group.otherTeacherGroups.length > 0 ? (
                        <button
                          onClick={() => handleMoveClick(student)}
                          className="text-indigo-600 hover:text-indigo-900"
                        >
                          {t('teacherGroupDetailsPage.move', 'Move')}
                        </button>
                      ) : (
                        <span className="text-gray-400">
                          {t('teacherGroupDetailsPage.move', 'Move')}
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-center text-gray-500">
            {t(
              'teacherGroupDetailsPage.noStudents',
              'No students assigned to this group.'
            )}
          </p>
        )}
      </InfoCard>

      {showMoveDialog && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h3 className="text-lg font-medium text-gray-900 mb-4">
              {t('teacherGroupDetailsPage.moveStudent', 'Move Student')}
            </h3>
            <p className="mb-4 text-sm text-gray-600">
              {t(
                'teacherGroupDetailsPage.movingFromTo',
                'Moving {{name}} {{surname}} from {{groupName}} to another group.',
                {
                  name: movingStudent?.name,
                  surname: movingStudent?.surname,
                  groupName: group?.name,
                }
              )}
            </p>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                {t(
                  'teacherGroupDetailsPage.selectTargetGroupLabel',
                  'Select Target Group'
                )}
              </label>
              <select
                value={selectedTargetGroup}
                onChange={e => setSelectedTargetGroup(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-purple-500 focus:border-purple-500"
              >
                <option value="">
                  {t(
                    'teacherGroupDetailsPage.selectGroup',
                    '-- Select a group --'
                  )}
                </option>
                {group?.otherTeacherGroups.map(targetGroup => (
                  <option key={targetGroup.id} value={targetGroup.id}>
                    {targetGroup.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex justify-end space-x-2">
              <button
                onClick={() => setShowMoveDialog(false)}
                className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                {t('teacherGroupDetailsPage.cancel', 'Cancel')}
              </button>
              <button
                onClick={handleMoveStudent}
                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-600 hover:bg-purple-700"
              >
                {t('teacherGroupDetailsPage.moveStudent', 'Move Student')}
              </button>
            </div>
          </div>
        </div>
      )}
    </DetailsLayout>
  );
};

export default TeacherGroupDetailsPage;
