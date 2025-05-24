import { useState, useEffect, useMemo, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { TestService } from '@/services/TestService';
import DetailsLayout from '@/layouts/DetailsLayout';
import DeleteConfirmationModal from '@/components/modals/DeleteConfirmationModal';
import AiEvaluationConfirmationModal from '@/components/modals/AiEvaluationConfirmationModal';
import ApiKeyManagementModal from '@/components/modals/ApiKeyManagementModal';
import TestSettingsModal from '@/components/modals/TestSettingsModal';
import PageHeader from '@/components/shared/PageHeader';
import TabNavigation from '@/components/shared/TabNavigation';
import TestDetailsTab from '@/components/test/TestDetailsTab';
import TestQuestionsTab from '@/components/test/TestQuestionsTab';
import TestPreviewTab from '@/components/test/TestPreviewTab';
import TestAttemptsTab from '@/components/test/TestAttemptsTab';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

const TestDetailsPage = () => {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();

  const [test, setTest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showAiEvalConfirm, setShowAiEvalConfirm] = useState(false);
  const [showApiKeyModal, setShowApiKeyModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [activeTab, setActiveTab] = useState('details');

  const userRole = useMemo(() => {
    return localStorage.getItem('userRole') || 'ADMIN';
  }, []);

  const isTeacher = useMemo(() => userRole === 'TEACHER', [userRole]);
  const isAdmin = useMemo(() => userRole === 'ADMIN', [userRole]);

  const baseRoute = useMemo(
    () => (isTeacher ? '/teacher/tests' : '/admin/tests'),
    [isTeacher]
  );

  const fetchTestDetails = useCallback(async () => {
    try {
      setLoading(true);
      const testData = await TestService.getTestPreview(id);
      setTest(testData);
    } catch {
      toast.error(
        t('testDetailsPage.failedToLoad', 'Failed to load test details')
      );
      navigate(baseRoute);
    } finally {
      setLoading(false);
    }
  }, [id, navigate, baseRoute, t]);

  useEffect(() => {
    fetchTestDetails();
  }, [fetchTestDetails]);

  const handleDelete = useCallback(async () => {
    try {
      await TestService.deleteTest(id);
      toast.success(
        t('testDetailsPage.deleteSuccess', 'Test deleted successfully')
      );
      navigate(baseRoute);
    } catch (error) {
      toast.error(
        error.message || t('testDetailsPage.deleteError', 'Error deleting test')
      );
      setShowDeleteConfirm(false);
    }
  }, [id, navigate, baseRoute, t]);

  const handleToggleActivity = useCallback(async () => {
    try {
      await TestService.toggleTestActivity(id);
      toast.success(
        t('testDetailsPage.statusToggled', 'Test status toggled successfully')
      );
      fetchTestDetails();
      setShowSettingsModal(false);
    } catch (error) {
      toast.error(
        error.message ||
          t('testDetailsPage.statusToggleError', 'Error changing test status')
      );
    }
  }, [id, fetchTestDetails, t]);

  const handleToggleTeacherEditPermission = useCallback(async () => {
    try {
      await TestService.toggleTeacherEditPermission(id);
      toast.success(
        t(
          'testDetailsPage.permissionToggled',
          'Teacher edit permission toggled successfully'
        )
      );
      fetchTestDetails();
      setShowSettingsModal(false);
    } catch (error) {
      toast.error(
        error.message ||
          t(
            'testDetailsPage.permissionToggleError',
            'Error changing teacher edit permission'
          )
      );
    }
  }, [id, fetchTestDetails, t]);

  const handleToggleAiEvaluation = useCallback(async () => {
    try {
      await TestService.toggleTestAiEvaluation(id);
      toast.success(
        t(
          'testDetailsPage.aiEvaluationUpdated',
          'AI evaluation setting updated successfully'
        )
      );
      setShowAiEvalConfirm(false);
      setShowSettingsModal(false);
      fetchTestDetails();
    } catch (error) {
      toast.error(
        error.message ||
          t(
            'testDetailsPage.aiEvaluationUpdateError',
            'Error changing AI evaluation setting'
          )
      );
      setShowAiEvalConfirm(false);
    }
  }, [id, fetchTestDetails, t]);

  const canEditTest = useMemo(() => {
    if (!test) return false;

    if (isAdmin) return true;

    if (isTeacher) {
      if (test.createdByAdmin) {
        return test.allowTeacherEdit;
      }
      return true;
    }

    return false;
  }, [test, isAdmin, isTeacher]);

  const badges = useMemo(() => {
    if (!test) return [];

    const badges = [];

    badges.push({
      text: test.open
        ? t('testDetailsPage.open', 'Open')
        : t('testDetailsPage.closed', 'Closed'),
      className: test.open
        ? 'bg-green-100 text-green-800'
        : 'bg-red-100 text-red-800',
    });

    if (test.createdByAdmin) {
      badges.push({
        text: t('testDetailsPage.adminCreated', 'Admin-created'),
        className: 'bg-purple-100 text-purple-800',
      });
    }

    if (test.aiEvaluate) {
      badges.push({
        text: t('testDetailsPage.aiEvaluation', 'AI Evaluation'),
        className: 'bg-blue-100 text-blue-800',
      });
    }

    if (test.createdByAdmin && test.allowTeacherEdit) {
      badges.push({
        text: t('testDetailsPage.teacherEditable', 'Teacher Editable'),
        className: 'bg-green-100 text-green-800',
      });
    }

    return badges;
  }, [test, t]);

  const actions = useMemo(() => {
    const actionsList = [
      {
        text: t('testDetailsPage.manageApiKeys', 'Manage API Keys'),
        onClick: () => setShowApiKeyModal(true),
        icon: (
          <svg
            className="h-4 w-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
            />
          </svg>
        ),
      },
    ];

    if (canEditTest) {
      actionsList.push({
        text: t('testDetailsPage.editTest', 'Edit Test'),
        onClick: () => navigate(`${baseRoute}/${id}/edit`),
        primary: true,
        icon: (
          <svg
            className="h-4 w-4"
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
        ),
      });
    }

    return actionsList;
  }, [id, navigate, canEditTest, baseRoute, t]);

  const tabs = useMemo(() => {
    return [
      {
        id: 'details',
        label: t('testDetailsPage.testDetails', 'Test Details'),
      },
      {
        id: 'questions',
        label: t('testDetailsPage.questionsManagement', 'Questions Management'),
        count: test?.totalQuestions || 0,
      },
      {
        id: 'preview',
        label: t('testDetailsPage.studentPreview', 'Student Preview'),
        icon: (
          <svg
            className="h-4 w-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            />
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
            />
          </svg>
        ),
      },
      {
        id: 'attempts',
        label: t('testDetailsPage.attempts', 'Attempts'),
        icon: (
          <svg
            className="h-4 w-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            />
          </svg>
        ),
      },
    ];
  }, [test, t]);

  const breadcrumbs = [
    { name: t('testDetailsPage.tests', 'Tests'), href: baseRoute },
    { name: test?.title || t('testDetailsPage.testDetails', 'Test Details') },
  ];

  if (loading) {
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
        title={test?.title}
        description={test?.description}
        badges={badges}
        actions={actions}
        onSettingsClick={canEditTest ? () => setShowSettingsModal(true) : null}
        onDeleteClick={canEditTest ? () => setShowDeleteConfirm(true) : null}
      />

      <TabNavigation
        tabs={tabs}
        activeTab={activeTab}
        onChange={setActiveTab}
      />

      {activeTab === 'details' && (
        <TestDetailsTab
          test={test}
          userRole={userRole}
          onEdit={
            canEditTest ? () => navigate(`${baseRoute}/${id}/edit`) : null
          }
        />
      )}

      {activeTab === 'questions' && (
        <TestQuestionsTab
          testId={id}
          onQuestionsUpdate={fetchTestDetails}
          canEditTest={canEditTest}
          aiEvaluationEnabled={test?.aiEvaluate}
        />
      )}

      {activeTab === 'preview' && <TestPreviewTab test={test} />}

      {activeTab === 'attempts' && <TestAttemptsTab testId={id} />}

      <TestSettingsModal
        isOpen={showSettingsModal}
        onClose={() => setShowSettingsModal(false)}
        test={test}
        userRole={userRole}
        onToggleActivity={handleToggleActivity}
        onToggleTeacherEditPermission={handleToggleTeacherEditPermission}
        setShowAiEvalConfirm={setShowAiEvalConfirm}
      />

      <DeleteConfirmationModal
        isOpen={showDeleteConfirm}
        title={t('testDetailsPage.deleteTest', 'Delete Test')}
        description={t(
          'testDetailsPage.deleteConfirmation',
          'Are you sure you want to delete this test? This action cannot be undone, and all associated data will be permanently removed.'
        )}
        itemName={test?.title}
        confirmButtonText={t('testDetailsPage.deleteTest', 'Delete Test')}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteConfirm(false)}
      />

      <AiEvaluationConfirmationModal
        isOpen={showAiEvalConfirm}
        isEnabling={!test?.aiEvaluate}
        onConfirm={handleToggleAiEvaluation}
        onCancel={() => {
          setShowAiEvalConfirm(false);
          setShowSettingsModal(false);
        }}
      />

      <ApiKeyManagementModal
        isOpen={showApiKeyModal}
        testId={id}
        onClose={() => setShowApiKeyModal(false)}
        onKeysUpdated={fetchTestDetails}
        associatedGroups={test?.associatedGroups || []}
      />
    </DetailsLayout>
  );
};

export default TestDetailsPage;
