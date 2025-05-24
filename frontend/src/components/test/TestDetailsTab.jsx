import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import InfoCard from '@/components/shared/InfoCard';
import InfoItem from '@/components/shared/InfoItem';
import StatCard from '@/components/shared/StatCard';

const TestDetailsTab = ({ test, userRole, onEdit }) => {
  const { t } = useTranslation();

  const formatDuration = durationMinutes => {
    if (!durationMinutes) return t('testDetailsTab.notSet', 'Not set');

    if (durationMinutes < 60) {
      return t('testDetailsTab.minutes', '{{duration}} minutes', {
        duration: durationMinutes,
      });
    }

    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;

    if (minutes === 0) {
      return t('testDetailsTab.hours', '{{hours}} {{hourLabel}}', {
        hours,
        hourLabel:
          hours === 1
            ? t('testDetailsTab.hour', 'hour')
            : t('testDetailsTab.hoursPlural', 'hours'),
      });
    }

    return t(
      'testDetailsTab.hoursAndMinutes',
      '{{hours}} {{hourLabel}} {{minutes}} minutes',
      {
        hours,
        hourLabel:
          hours === 1
            ? t('testDetailsTab.hour', 'hour')
            : t('testDetailsTab.hoursPlural', 'hours'),
        minutes,
      }
    );
  };

  const formatDateTime = dateTimeString => {
    if (!dateTimeString) return t('testDetailsTab.notSet', 'Not set');

    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  const difficultyCount = useMemo(() => {
    return {
      EASY: test.easyQuestionsSetup || 0,
      MEDIUM: test.mediumQuestionsSetup || 0,
      HARD: test.hardQuestionsSetup || 0,
    };
  }, [test]);

  const getEditUrl = () => {
    const basePath = userRole === 'ADMIN' ? '/admin/tests/' : '/teacher/tests/';
    return `${basePath}${test.id}/edit`;
  };

  return (
    <>
      <InfoCard
        title={t('testDetailsTab.testDetails', 'Test Details')}
        description={t(
          'testDetailsTab.keyInfo',
          'Key information about this test'
        )}
      >
        <dl className="grid grid-cols-1 gap-x-4 gap-y-6 sm:grid-cols-2 lg:grid-cols-3">
          <InfoItem
            label={t('testDetailsTab.duration', 'Duration')}
            value={formatDuration(test.duration)}
          />
          <InfoItem
            label={t('testDetailsTab.totalScore', 'Total Score')}
            value={t('testDetailsTab.points', '{{score}} points', {
              score: test.totalScore,
            })}
          />
          <InfoItem
            label={t('testDetailsTab.maximumAttempts', 'Maximum Attempts')}
            value={
              test.maxAttempts || t('testDetailsTab.unlimited', 'Unlimited')
            }
          />
          <InfoItem
            label={t('testDetailsTab.startTime', 'Start Time')}
            value={formatDateTime(test.startTime)}
          />
          <InfoItem
            label={t('testDetailsTab.endTime', 'End Time')}
            value={formatDateTime(test.endTime)}
          />
          <InfoItem
            label={t('testDetailsTab.numberOfQuestions', 'Number of Questions')}
            value={test.totalQuestions}
          />

          <div className="sm:col-span-3">
            <InfoItem
              label={t('testDetailsTab.aiEvaluation', 'AI Evaluation')}
              value={
                test.aiEvaluate ? (
                  <span className="inline-flex items-center text-green-600 font-medium">
                    <svg
                      className="mr-1.5 h-4 w-4 text-green-500"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                    >
                      <path
                        fillRule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                        clipRule="evenodd"
                      />
                    </svg>
                    {t(
                      'testDetailsTab.aiEnabled',
                      'Enabled - AI will automatically evaluate open-ended questions'
                    )}
                  </span>
                ) : (
                  <span className="inline-flex items-center text-gray-600 font-medium">
                    <svg
                      className="mr-1.5 h-4 w-4 text-gray-500"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                    >
                      <path
                        fillRule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                        clipRule="evenodd"
                      />
                    </svg>
                    {t(
                      'testDetailsTab.aiDisabled',
                      'Disabled - Open-ended questions require manual grading'
                    )}
                  </span>
                )
              }
            />
          </div>

          {test.createdByAdmin && userRole === 'ADMIN' && (
            <div className="sm:col-span-3 pt-2 mt-2 border-t border-gray-200">
              <InfoItem
                label={t(
                  'testDetailsTab.teacherEditPermission',
                  'Teacher Edit Permission'
                )}
                value={
                  test.allowTeacherEdit ? (
                    <span className="text-green-600 font-medium">
                      {t(
                        'testDetailsTab.teacherEditEnabled',
                        'Enabled - Teachers can modify this test'
                      )}
                    </span>
                  ) : (
                    <span className="text-red-600 font-medium">
                      {t(
                        'testDetailsTab.teacherEditDisabled',
                        'Disabled - Only administrators can modify this test'
                      )}
                    </span>
                  )
                }
              />
            </div>
          )}
        </dl>
      </InfoCard>

      <InfoCard
        className="mt-6"
        title={t(
          'testDetailsTab.questionDistribution',
          'Question Distribution'
        )}
        description={t(
          'testDetailsTab.distributionDescription',
          'Question counts and scoring by difficulty level'
        )}
      >
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
          <StatCard
            icon={<span className="h-3 w-3 bg-green-500 rounded-full" />}
            label={t('testDetailsTab.easyQuestions', 'Easy Questions')}
            value={difficultyCount.EASY}
            bgColor="bg-green-50"
            textColor="text-green-700"
            borderColor="border-green-200"
            description={
              <>
                <span className="block">
                  {test.easyQuestionsCount > 0
                    ? t(
                        'testDetailsTab.questionsConfigured',
                        '{{count}} configured',
                        { count: test.easyQuestionsCount }
                      )
                    : t('testDetailsTab.noneConfigured', 'None configured')}
                </span>
                <span className="block font-semibold mt-1 text-green-600">
                  {t('testDetailsTab.pointsEach', '{{points}} points each', {
                    points: test.easyScore,
                  })}
                </span>
              </>
            }
          />

          <StatCard
            icon={<span className="h-3 w-3 bg-yellow-500 rounded-full" />}
            label={t('testDetailsTab.mediumQuestions', 'Medium Questions')}
            value={difficultyCount.MEDIUM}
            bgColor="bg-yellow-50"
            textColor="text-yellow-700"
            borderColor="border-yellow-200"
            description={
              <>
                <span className="block">
                  {test.mediumQuestionsCount > 0
                    ? t(
                        'testDetailsTab.questionsConfigured',
                        '{{count}} configured',
                        { count: test.mediumQuestionsCount }
                      )
                    : t('testDetailsTab.noneConfigured', 'None configured')}
                </span>
                <span className="block font-semibold mt-1 text-yellow-600">
                  {t('testDetailsTab.pointsEach', '{{points}} points each', {
                    points: test.mediumScore,
                  })}
                </span>
              </>
            }
          />

          <StatCard
            icon={<span className="h-3 w-3 bg-red-500 rounded-full" />}
            label={t('testDetailsTab.hardQuestions', 'Hard Questions')}
            value={difficultyCount.HARD}
            bgColor="bg-red-50"
            textColor="text-red-700"
            borderColor="border-red-200"
            description={
              <>
                <span className="block">
                  {test.hardQuestionsCount > 0
                    ? t(
                        'testDetailsTab.questionsConfigured',
                        '{{count}} configured',
                        { count: test.hardQuestionsCount }
                      )
                    : t('testDetailsTab.noneConfigured', 'None configured')}
                </span>
                <span className="block font-semibold mt-1 text-red-600">
                  {t('testDetailsTab.pointsEach', '{{points}} points each', {
                    points: test.hardScore,
                  })}
                </span>
              </>
            }
          />
        </div>

        <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
          <h4 className="text-sm font-medium text-blue-800 mb-2">
            {t('testDetailsTab.scoreDistribution', 'Score Distribution')}
          </h4>
          <div className="flex items-center justify-between">
            <div className="flex space-x-4">
              <div className="flex items-center">
                <span className="h-3 w-3 bg-green-500 rounded-full mr-1" />
                <span className="text-sm text-gray-600">
                  {t('testDetailsTab.easyPoints', 'Easy: {{points}} pts', {
                    points: test.easyScore,
                  })}
                </span>
              </div>
              <div className="flex items-center">
                <span className="h-3 w-3 bg-yellow-500 rounded-full mr-1" />
                <span className="text-sm text-gray-600">
                  {t('testDetailsTab.mediumPoints', 'Medium: {{points}} pts', {
                    points: test.mediumScore,
                  })}
                </span>
              </div>
              <div className="flex items-center">
                <span className="h-3 w-3 bg-red-500 rounded-full mr-1" />
                <span className="text-sm text-gray-600">
                  {t('testDetailsTab.hardPoints', 'Hard: {{points}} pts', {
                    points: test.hardScore,
                  })}
                </span>
              </div>
            </div>
            <div className="text-sm font-medium text-blue-700">
              {t('testDetailsTab.totalPoints', 'Total: {{points}} points', {
                points: test.totalScore,
              })}
            </div>
          </div>
        </div>

        <div className="mt-6 flex items-center justify-between">
          <p className="text-sm text-gray-600">
            {t('testDetailsTab.totalQuestions', 'Total questions: {{count}}', {
              count: test.totalQuestions || 0,
            })}
          </p>
          {onEdit && (
            <Link
              to={getEditUrl()}
              className="inline-flex items-center px-3 py-1.5 border border-purple-300 text-sm font-medium rounded-lg text-purple-700 bg-white hover:bg-purple-50 transition-colors"
            >
              <svg
                className="mr-1.5 h-4 w-4"
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
              {t('testDetailsTab.configureTest', 'Configure Test')}
            </Link>
          )}
        </div>
      </InfoCard>

      <InfoCard
        className="mt-6"
        title={t('testDetailsTab.associatedGroups', 'Associated Groups')}
        description={t(
          'testDetailsTab.groupsDescription',
          'Groups that have access to this test ({{count}})',
          { count: test.associatedGroups?.length || 0 }
        )}
      >
        {test.associatedGroups && test.associatedGroups.length > 0 ? (
          <ul className="divide-y divide-gray-200">
            {test.associatedGroups.map(group => (
              <li key={group.id} className="py-3">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-purple-600 truncate">
                      {group.name}
                    </p>
                    {group.aiEvaluationEnabled !== undefined && (
                      <p className="mt-1 text-xs text-gray-500">
                        {t('testDetailsTab.aiEvaluation', 'AI Evaluation')}:
                        <span
                          className={`ml-1 font-medium ${group.aiEvaluationEnabled ? 'text-green-600' : 'text-red-600'}`}
                        >
                          {group.aiEvaluationEnabled
                            ? t('testDetailsTab.enabled', 'Enabled')
                            : t('testDetailsTab.disabled', 'Disabled')}
                        </span>
                      </p>
                    )}
                  </div>
                  <Link
                    to={`${userRole === 'ADMIN' ? '/admin' : '/teacher'}/groups/${group.id}`}
                    className="px-3 py-1 text-xs font-medium rounded-lg bg-purple-100 text-purple-800 hover:bg-purple-200 transition-colors"
                  >
                    {t('testDetailsTab.viewGroup', 'View Group')}
                  </Link>
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-center text-gray-500">
            {t(
              'testDetailsTab.noGroupsAssociated',
              'No groups associated with this test'
            )}
          </p>
        )}
      </InfoCard>
    </>
  );
};

export default TestDetailsTab;
