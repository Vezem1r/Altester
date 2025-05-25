import { useState, useEffect, useMemo, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AdminService } from '@/services/AdminService';
import DetailsLayout from '@/layouts/DetailsLayout';
import PageHeader from '@/components/shared/PageHeader';
import TabNavigation from '@/components/shared/TabNavigation';
import GroupDetailsTab from '@/components/group/GroupDetailsTab';
import GroupTestsTab from '@/components/group/GroupTestsTab';
import GroupTeacherTab from '@/components/group/GroupTeacherTab';
import GroupStudentsTab from '@/components/group/GroupStudentsTab';
import DeleteConfirmationModal from '@/components/modals/DeleteConfirmationModal';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

const GroupDetailsPage = () => {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();

  const [group, setGroup] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('details');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [isEditingName, setIsEditingName] = useState(false);
  const [newGroupName, setNewGroupName] = useState('');
  const [groupNameError, setGroupNameError] = useState('');

  const fetchGroupDetails = useCallback(async () => {
    try {
      setLoading(true);
      const data = await AdminService.getGroup(id);
      setGroup(data);
      setNewGroupName(data.name);
    } catch {
      toast.error(
        t('groupDetailsPage.failedToLoad', 'Failed to load group details')
      );
      navigate('/admin/groups');
    } finally {
      setLoading(false);
    }
  }, [id, navigate, t]);

  useEffect(() => {
    fetchGroupDetails();
  }, [fetchGroupDetails]);

  useEffect(() => {
    if (group) {
      setNewGroupName(group.name);
    }
  }, [group]);

  const handleGroupNameChange = useCallback(async () => {
    if (group && !group.active && !group.inFuture) {
      toast.error(
        t(
          'groupDetailsPage.cannotModifyInactive',
          'Cannot modify inactive groups'
        )
      );
      setIsEditingName(false);
      return;
    }

    if (!newGroupName || newGroupName.trim() === '') {
      setGroupNameError(
        t('groupDetailsPage.emptyGroupName', 'Group name cannot be empty')
      );
      return;
    }

    try {
      const updateData = {
        groupName: newGroupName.trim(),
        teacherId: group.teacher?.userId?.toString() || '',
        studentsIds: group.students ? group.students.map(s => s.userId) : [],
        semester: group.semester,
        academicYear: group.academicYear,
        active: group.active,
      };

      await AdminService.updateGroup(id, updateData);
      toast.success(
        t(
          'groupDetailsPage.groupNameUpdated',
          'Group name updated successfully'
        )
      );
      fetchGroupDetails();
      setIsEditingName(false);
      setGroupNameError('');
    } catch (error) {
      toast.error(
        error.message ||
          t('groupDetailsPage.errorUpdatingName', 'Error updating group name')
      );
    }
  }, [group, newGroupName, id, fetchGroupDetails, t]);

  const handleDelete = useCallback(async () => {
    try {
      await AdminService.deleteGroup(id);
      toast.success(
        t('groupDetailsPage.groupDeleted', 'Group deleted successfully')
      );
      navigate('/admin/groups');
    } catch (error) {
      toast.error(
        error.message ||
          t('groupDetailsPage.errorDeleting', 'Error deleting group')
      );
      setShowDeleteConfirm(false);
    }
  }, [id, navigate, t]);

  const canModifyGroup = useMemo(() => {
    return group && (group.active || group.inFuture);
  }, [group]);

  const badges = useMemo(() => {
    if (!group) return [];

    const badges = [];

    if (group.inFuture) {
      badges.push({
        text: t('groupDetailsPage.future', 'Future'),
        className: 'bg-blue-100 text-blue-800',
      });
    } else if (group.active) {
      badges.push({
        text: t('groupDetailsPage.active', 'Active'),
        className: 'bg-green-100 text-green-800',
      });
    } else {
      badges.push({
        text: t('groupDetailsPage.inactive', 'Inactive'),
        className: 'bg-gray-100 text-gray-800',
      });
    }

    return badges;
  }, [group, t]);

  const tabs = useMemo(() => {
    const isPastInactiveGroup = group && !group.active && !group.inFuture;

    return [
      {
        id: 'details',
        label: t('groupDetailsPage.groupDetails', 'Group Details'),
      },
      { id: 'tests', label: t('groupDetailsPage.tests', 'Tests') },
      {
        id: 'teacher',
        label: t('groupDetailsPage.assignTeacher', 'Assign Teacher'),
        disabled: isPastInactiveGroup,
      },
      {
        id: 'students',
        label: t('groupDetailsPage.manageStudents', 'Manage Students'),
        disabled: isPastInactiveGroup,
      },
    ];
  }, [group, t]);

  const breadcrumbs = [
    { name: t('groupDetailsPage.groups', 'Groups'), href: '/admin/groups' },
    {
      name: group?.name || t('groupDetailsPage.groupDetails', 'Group Details'),
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
        title={
          isEditingName ? (
            <div className="flex items-center gap-2 w-full max-w-md">
              <input
                type="text"
                value={newGroupName}
                onChange={e => setNewGroupName(e.target.value)}
                className="px-3 py-2 border border-purple-300 rounded-md text-gray-700 focus:outline-none focus:ring-2 focus:ring-purple-500 w-full"
              />
              <button
                onClick={handleGroupNameChange}
                className="p-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 transition-colors shadow-sm flex items-center justify-center"
                title={t('groupDetailsPage.save', 'Save')}
              >
                <svg
                  className="h-5 w-5"
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
                  setIsEditingName(false);
                  setNewGroupName(group?.name || '');
                  setGroupNameError('');
                }}
                className="p-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors shadow-sm flex items-center justify-center"
                title={t('groupDetailsPage.cancel', 'Cancel')}
              >
                <svg
                  className="h-5 w-5"
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
            <div className="flex items-center gap-2">
              {group?.name}
              {canModifyGroup && (
                <button
                  onClick={() => setIsEditingName(true)}
                  className="p-1 text-purple-300 hover:text-white transition-colors"
                >
                  <svg
                    className="h-5 w-5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                    />
                  </svg>
                </button>
              )}
            </div>
          )
        }
        badges={badges}
        backUrl="/admin/groups"
        onDeleteClick={canModifyGroup ? () => setShowDeleteConfirm(true) : null}
      />

      {groupNameError && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg">
          {groupNameError}
        </div>
      )}

      <TabNavigation
        tabs={tabs}
        activeTab={activeTab}
        onChange={tab => {
          if (tabs.find(t => t.id === tab && t.disabled)) {
            toast.info(
              t(
                'groupDetailsPage.inactiveGroupsViewOnly',
                'Inactive groups can only be viewed, not modified'
              )
            );
            return;
          }
          setActiveTab(tab);
        }}
      />

      {activeTab === 'details' && (
        <GroupDetailsTab
          group={group}
          onUpdate={fetchGroupDetails}
          isInactive={group && !group.active && !group.inFuture}
        />
      )}

      {activeTab === 'tests' && <GroupTestsTab group={group} />}

      {activeTab === 'teacher' && (
        <GroupTeacherTab group={group} onUpdate={fetchGroupDetails} />
      )}

      {activeTab === 'students' && (
        <GroupStudentsTab group={group} onUpdate={fetchGroupDetails} />
      )}

      <DeleteConfirmationModal
        isOpen={showDeleteConfirm}
        title={t('groupDetailsPage.deleteGroup', 'Delete Group')}
        description={t(
          'groupDetailsPage.deleteConfirmation',
          'Are you sure you want to delete this group? This action cannot be undone and will remove all students from the group.'
        )}
        itemName={group?.name}
        confirmButtonText={t('groupDetailsPage.deleteGroup', 'Delete Group')}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteConfirm(false)}
      />
    </DetailsLayout>
  );
};

export default GroupDetailsPage;
