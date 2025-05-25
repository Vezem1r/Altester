import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { AttemptService } from '@/services/AttemptService';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';

const STATUS_TRANSLATION_KEYS = {
  completed: 'testAttemptsTab.status.completed',
  in_progress: 'testAttemptsTab.status.in_progress',
  timeout: 'testAttemptsTab.status.timeout',
  abandoned: 'testAttemptsTab.status.abandoned',
  reviewed: 'testAttemptsTab.status.reviewed',
  ai_reviewed: 'testAttemptsTab.status.ai_reviewed',
};

const TestAttemptsTab = ({ testId }) => {
  const { t } = useTranslation();
  const [attempts, setAttempts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');
  const [userRole, setUserRole] = useState(null);
  const [expandedGroups, setExpandedGroups] = useState({});
  const [expandedStudents, setExpandedStudents] = useState({});
  const [studentAttemptsMap, setStudentAttemptsMap] = useState({});
  const [loadingStudentAttempts, setLoadingStudentAttempts] = useState({});

  const navigate = useNavigate();

  const roundToOneDecimal = value => {
    if (value === null || value === undefined) return 0;
    if (Number.isInteger(value)) return value;
    return Math.round(value * 10) / 10;
  };

  useEffect(() => {
    const role = localStorage.getItem('userRole') || 'ADMIN';
    setUserRole(role);
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
    }, 500);

    return () => clearTimeout(timer);
  }, [searchQuery]);

  const fetchAttempts = useCallback(async () => {
    if (!userRole) return;

    try {
      setLoading(true);

      let attemptsData;

      const normalizedRole = userRole.toUpperCase();

      if (normalizedRole === 'ADMIN') {
        attemptsData = await AttemptService.getTestAttemptsForAdmin(
          testId,
          debouncedSearchQuery
        );
      } else if (normalizedRole === 'TEACHER') {
        attemptsData = await AttemptService.getTestAttemptsForTeacher(
          testId,
          debouncedSearchQuery
        );
      } else {
        toast.error(
          t(
            'testAttemptsTab.noPermission',
            "You don't have permission to view test attempts"
          )
        );
        setAttempts([]);
        setLoading(false);
        return;
      }

      setAttempts(attemptsData);
    } catch (error) {
      toast.error(
        error.message ||
          t('testAttemptsTab.failedToLoad', 'Failed to load test attempts')
      );
      setAttempts([]);
    } finally {
      setLoading(false);
    }
  }, [testId, userRole, debouncedSearchQuery, t]);

  useEffect(() => {
    if (userRole) {
      fetchAttempts();
    }
  }, [userRole, debouncedSearchQuery, fetchAttempts]);

  const fetchStudentAttempts = useCallback(
    async (username, groupId) => {
      if (!userRole || !username) return;

      if (
        loadingStudentAttempts[username] ||
        (studentAttemptsMap[username] && expandedStudents[username])
      ) {
        return;
      }

      setLoadingStudentAttempts(prev => ({ ...prev, [username]: true }));

      try {
        let studentAttemptsData;

        if (userRole.toUpperCase() === 'ADMIN') {
          studentAttemptsData =
            await AttemptService.getStudentTestAttemptsForAdmin(
              testId,
              username
            );
        } else {
          studentAttemptsData =
            await AttemptService.getStudentTestAttemptsForTeacher(
              testId,
              username
            );
        }

        setStudentAttemptsMap(prev => ({
          ...prev,
          [username]: studentAttemptsData,
        }));

        setExpandedStudents(prev => ({ ...prev, [username]: true }));

        if (groupId) {
          setExpandedGroups(prev => ({ ...prev, [groupId]: true }));
        }
      } catch (error) {
        toast.error(
          t(
            'testAttemptsTab.failedToLoadStudentAttempts',
            'Failed to load attempts for {{username}}',
            { username }
          )
        );
      } finally {
        setLoadingStudentAttempts(prev => ({ ...prev, [username]: false }));
      }
    },
    [
      testId,
      userRole,
      expandedStudents,
      loadingStudentAttempts,
      studentAttemptsMap,
      t,
    ]
  );

  const handleSearchChange = e => {
    setSearchQuery(e.target.value);
  };

  const toggleGroupExpanded = groupId => {
    setExpandedGroups(prev => ({
      ...prev,
      [groupId]: !prev[groupId],
    }));
  };

  const toggleStudentExpanded = (username, groupId) => {
    if (!studentAttemptsMap[username] && !expandedStudents[username]) {
      fetchStudentAttempts(username, groupId);
    } else {
      setExpandedStudents(prev => ({
        ...prev,
        [username]: !prev[username],
      }));
    }
  };

  const calculateStats = () => {
    if (!attempts || attempts.length === 0) {
      return {
        totalAttempts: 0,
        totalStudents: 0,
        averageScore: 0,
        averageAiScore: 0,
        aiAccuracy: 0,
        aiAccuracyPercent: 0,
      };
    }

    let totalAttempts = 0;
    let totalStudents = 0;
    let totalScore = 0;
    let totalAiScore = 0;
    let totalAccuracyPercent = 0;
    let accuracyCount = 0;

    attempts.forEach(group => {
      group.students.forEach(student => {
        totalStudents++;
        totalAttempts += student.attemptCount || 0;

        if (
          student.averageScore !== null &&
          student.averageScore !== undefined
        ) {
          totalScore += student.averageScore * (student.attemptCount || 1);
        }

        if (
          student.averageAiScore !== null &&
          student.averageAiScore !== undefined
        ) {
          totalAiScore += student.averageAiScore * (student.attemptCount || 1);

          if (
            student.averageScore !== null &&
            student.averageScore !== undefined
          ) {
            if (student.averageScore === 0 && student.averageAiScore !== 0) {
              totalAccuracyPercent += 0;
            } else if (
              student.averageScore === 0 &&
              student.averageAiScore === 0
            ) {
              totalAccuracyPercent += 100;
            } else {
              const diff = Math.abs(
                student.averageScore - student.averageAiScore
              );
              const accuracyPercent = Math.max(
                0,
                100 - (diff / student.averageScore) * 100
              );
              totalAccuracyPercent += accuracyPercent;
            }
            accuracyCount++;
          }
        }
      });
    });

    const avgScore = totalAttempts > 0 ? totalScore / totalAttempts : 0;
    const avgAiScore = totalAttempts > 0 ? totalAiScore / totalAttempts : 0;

    const avgAccuracy = Math.abs(avgScore - avgAiScore);

    const avgAccuracyPercent =
      accuracyCount > 0 ? totalAccuracyPercent / accuracyCount : 0;

    return {
      totalAttempts,
      totalStudents,
      averageScore: roundToOneDecimal(avgScore),
      averageAiScore: roundToOneDecimal(avgAiScore),
      aiAccuracy: roundToOneDecimal(avgAccuracy),
      aiAccuracyPercent: roundToOneDecimal(avgAccuracyPercent),
    };
  };

  const stats = calculateStats();

  const formatDate = dateString => {
    if (!dateString) return t('testAttemptsTab.notAvailable', 'N/A');
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  const calculateDuration = (startTime, endTime) => {
    if (!startTime || !endTime) return t('testAttemptsTab.notAvailable', 'N/A');

    const start = new Date(startTime);
    const end = new Date(endTime);
    const durationMs = end - start;

    if (durationMs < 0)
      return t('testAttemptsTab.invalidDuration', 'Invalid duration');

    const minutes = Math.floor(durationMs / 60000);
    const seconds = Math.floor((durationMs % 60000) / 1000);

    if (minutes < 60) {
      return t('testAttemptsTab.minutesSeconds', '{{minutes}}m {{seconds}}s', {
        minutes,
        seconds,
      });
    }

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    return t(
      'testAttemptsTab.hoursMinutesSeconds',
      '{{hours}}h {{minutes}}m {{seconds}}s',
      {
        hours,
        minutes: remainingMinutes,
        seconds,
      }
    );
  };

  const getStatusColor = status => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800';
      case 'TIMEOUT':
        return 'bg-yellow-100 text-yellow-800';
      case 'ABANDONED':
        return 'bg-red-100 text-red-800';
      case 'REVIEWED':
        return 'bg-purple-100 text-purple-800';
      case 'AI_REVIEWED':
        return 'bg-cyan-100 text-cyan-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = status => {
    switch (status) {
      case 'COMPLETED':
        return (
          <svg
            className="w-4 h-4 text-green-600"
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
        );
      case 'IN_PROGRESS':
        return (
          <svg
            className="w-4 h-4 text-blue-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        );
      case 'TIMEOUT':
        return (
          <svg
            className="w-4 h-4 text-yellow-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        );
      case 'ABANDONED':
        return (
          <svg
            className="w-4 h-4 text-red-600"
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
        );
      case 'REVIEWED':
        return (
          <svg
            className="w-4 h-4 text-purple-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        );
      case 'AI_REVIEWED':
        return (
          <svg
            className="w-4 h-4 text-cyan-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M13 10V3L4 14h7v7l9-11h-7z"
            />
          </svg>
        );
      default:
        return (
          <svg
            className="w-4 h-4 text-gray-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        );
    }
  };

  const getStatusTranslation = status => {
    const key = status.toLowerCase();
    const translationKey = STATUS_TRANSLATION_KEYS[key];
    return translationKey ? t(translationKey, status) : status;
  };

  const StatisticCard = ({ icon, title, value, subtitle, className }) => {
    return (
      <div
        className={`relative rounded-lg p-4 flex items-center ${className || 'bg-white'}`}
      >
        <div className="flex-shrink-0 rounded-full p-3 mr-4 bg-opacity-20">
          {icon}
        </div>
        <div>
          <h4 className="text-lg font-semibold">{value}</h4>
          <p className="text-sm font-medium">{title}</p>
          {subtitle && <p className="text-xs text-gray-500">{subtitle}</p>}
        </div>
      </div>
    );
  };

  const StudentAttemptsList = ({ attempts, loading }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    if (loading) {
      return (
        <div className="flex justify-center items-center py-6">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-purple-600" />
          <p className="ml-3 text-gray-600 text-sm">
            {t('testAttemptsTab.loadingAttempts', 'Loading attempts...')}
          </p>
        </div>
      );
    }

    if (!attempts || attempts.length === 0) {
      return (
        <div className="text-center py-6 bg-gray-50 rounded-lg">
          <p className="text-sm text-gray-500">
            {t(
              'testAttemptsTab.noAttemptsFoundForStudent',
              'No attempts found for this student.'
            )}
          </p>
        </div>
      );
    }

    return (
      <div className="mt-4 overflow-hidden">
        {attempts.map(attempt => (
          <div
            key={`attempt-${attempt.attemptId}`}
            className="mb-3 bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
          >
            <div className="px-4 py-3 bg-gray-50 border-b border-gray-200 flex items-center justify-between">
              <div className="flex items-center">
                <span className="h-6 w-6 rounded-full bg-purple-100 flex items-center justify-center mr-2">
                  <span className="text-xs font-medium text-purple-800">
                    {attempt.attemptNumber}
                  </span>
                </span>
                <h5 className="text-sm font-medium text-gray-900">
                  {t('testAttemptsTab.attemptNumber', 'Attempt #{{number}}', {
                    number: attempt.attemptNumber,
                  })}
                </h5>
                <span
                  className={`ml-3 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(attempt.status)}`}
                >
                  {getStatusIcon(attempt.status)}
                  <span className="ml-1">
                    {getStatusTranslation(attempt.status)}
                  </span>
                </span>
              </div>
              <button
                onClick={() => {
                  const rolePrefix = userRole.toLowerCase();
                  navigate(
                    `/${rolePrefix}/attempts/review/${attempt.attemptId}`
                  );
                }}
                className="inline-flex items-center px-2.5 py-1.5 border border-transparent text-xs font-medium rounded text-purple-700 bg-purple-100 hover:bg-purple-200 transition-colors"
              >
                {attempt.status === 'REVIEWED'
                  ? t('testAttemptsTab.viewReview', 'View Review')
                  : attempt.status === 'AI_REVIEWED'
                    ? t('testAttemptsTab.viewAiReview', 'View AI Review')
                    : t('testAttemptsTab.review', 'Review')}
              </button>
            </div>
            <div className="px-4 py-3">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div>
                  <p className="text-xs font-medium text-gray-500">
                    {t('testAttemptsTab.started', 'Started')}
                  </p>
                  <p className="text-sm text-gray-900">
                    {formatDate(attempt.startTime)}
                  </p>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500">
                    {t('testAttemptsTab.duration', 'Duration')}
                  </p>
                  <p className="text-sm text-gray-900">
                    {calculateDuration(attempt.startTime, attempt.endTime)}
                  </p>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500">
                    {t('testAttemptsTab.score', 'Score')}
                  </p>
                  <p className="text-sm font-semibold text-gray-900">
                    {attempt.score !== null
                      ? t('testAttemptsTab.points', '{{score}} pts', {
                          score: roundToOneDecimal(attempt.score),
                        })
                      : '-'}
                  </p>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500">
                    {t('testAttemptsTab.aiScore', 'AI Score')}
                  </p>
                  <p className="text-sm font-semibold text-gray-900">
                    {attempt.aiScore !== null
                      ? t('testAttemptsTab.points', '{{score}} pts', {
                          score: roundToOneDecimal(attempt.aiScore),
                        })
                      : '-'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  };

  const StudentCard = ({ student, groupId }) => {
    const { t } = useTranslation();

    const isExpanded = expandedStudents[student.username] || false;
    const studentAttempts = studentAttemptsMap[student.username] || [];
    const isLoading = loadingStudentAttempts[student.username] || false;

    const scoreDifference =
      student.averageScore !== null && student.averageAiScore !== null
        ? roundToOneDecimal(student.averageScore - student.averageAiScore)
        : null;

    const scoreColor =
      scoreDifference === null
        ? 'text-gray-600'
        : scoreDifference > 0
          ? 'text-red-600'
          : scoreDifference < 0
            ? 'text-green-600'
            : 'text-gray-600';

    const scoreIcon =
      scoreDifference === null ? null : scoreDifference > 0 ? (
        <svg
          className="h-3 w-3 text-red-600"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"
          />
        </svg>
      ) : scoreDifference < 0 ? (
        <svg
          className="h-3 w-3 text-green-600"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6"
          />
        </svg>
      ) : null;

    return (
      <div className="border border-gray-200 rounded-lg mb-3 overflow-hidden bg-white shadow-sm hover:shadow-md transition-shadow">
        <div
          className="px-4 py-3 cursor-pointer hover:bg-gray-50 transition-colors flex justify-between items-center"
          onClick={() => toggleStudentExpanded(student.username, groupId)}
        >
          <div className="flex items-center">
            <div className="h-10 w-10 rounded-full bg-gradient-to-br from-purple-500 to-indigo-600 flex items-center justify-center mr-3 text-white shadow-sm">
              <span className="font-medium text-sm">
                {student.firstName?.charAt(0)}
                {student.lastName?.charAt(0)}
              </span>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-900">
                {student.firstName} {student.lastName}
              </h4>
              <p className="text-xs text-gray-500">{student.username}</p>
            </div>
          </div>

          <div className="flex items-center space-x-6">
            <div className="flex flex-col items-center px-3 py-1 rounded-md bg-gray-50">
              <span className="text-xs font-medium text-gray-500">
                {t('testAttemptsTab.attempts', 'Attempts')}
              </span>
              <span className="text-sm font-semibold text-gray-900">
                {student.attemptCount || 0}
              </span>
            </div>

            <div className="flex flex-col items-center px-3 py-1 rounded-md bg-gray-50">
              <span className="text-xs font-medium text-gray-500">
                {t('testAttemptsTab.avgScore', 'Avg Score')}
              </span>
              <span className="text-sm font-semibold text-gray-900">
                {roundToOneDecimal(student.averageScore || 0)}
              </span>
            </div>

            {student.averageAiScore !== null &&
              student.averageAiScore !== undefined && (
                <div className="flex flex-col items-center px-3 py-1 rounded-md bg-gray-50">
                  <span className="text-xs font-medium text-gray-500">
                    {t('testAttemptsTab.aiScore', 'AI Score')}
                  </span>
                  <div className="flex items-center">
                    <span className="text-sm font-semibold text-gray-900">
                      {roundToOneDecimal(student.averageAiScore)}
                    </span>
                    {scoreDifference !== null && scoreDifference !== 0 && (
                      <span
                        className={`ml-1 text-xs flex items-center ${scoreColor}`}
                      >
                        {scoreIcon}
                        <span className="ml-0.5">
                          {Math.abs(scoreDifference)}
                        </span>
                      </span>
                    )}
                  </div>
                </div>
              )}

            <div className="flex items-center">
              <button
                onClick={e => {
                  e.stopPropagation();
                  fetchStudentAttempts(student.username, groupId);
                }}
                className="mr-2 px-2 py-1 text-xs font-medium rounded-md bg-purple-100 text-purple-700 hover:bg-purple-200 transition-colors"
              >
                {isLoading ? (
                  <span className="flex items-center">
                    <svg
                      className="animate-spin h-3 w-3 mr-1"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      />
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      />
                    </svg>
                    {t('testAttemptsTab.loading', 'Loading...')}
                  </span>
                ) : (
                  t('testAttemptsTab.viewAttempts', 'View Attempts')
                )}
              </button>
              <svg
                className={`h-5 w-5 text-gray-400 transform transition-transform duration-150 ${isExpanded ? 'rotate-180' : ''}`}
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
          </div>
        </div>

        {isExpanded && (
          <div className="px-4 py-3 bg-gray-50 border-t border-gray-200">
            <StudentAttemptsList
              attempts={studentAttempts}
              username={student.username}
              loading={isLoading}
            />
          </div>
        )}
      </div>
    );
  };

  const GroupCard = ({ group }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const isExpanded = expandedGroups[group.groupId] || false;

    const groupStats = group.students.reduce(
      (stats, student) => {
        const attemptCount = student.attemptCount || 0;
        stats.totalAttempts += attemptCount;

        if (
          student.averageScore !== null &&
          student.averageScore !== undefined
        ) {
          stats.totalScore += student.averageScore * attemptCount;
          stats.scoreCount += attemptCount;
        }

        if (
          student.averageAiScore !== null &&
          student.averageAiScore !== undefined
        ) {
          stats.totalAiScore += student.averageAiScore * attemptCount;
          stats.aiScoreCount += attemptCount;
        }

        return stats;
      },
      {
        totalAttempts: 0,
        totalScore: 0,
        totalAiScore: 0,
        scoreCount: 0,
        aiScoreCount: 0,
      }
    );

    const averageScore =
      groupStats.scoreCount > 0
        ? roundToOneDecimal(groupStats.totalScore / groupStats.scoreCount)
        : 0;

    const averageAiScore =
      groupStats.aiScoreCount > 0
        ? roundToOneDecimal(groupStats.totalAiScore / groupStats.aiScoreCount)
        : 0;

    return (
      <div className="bg-white shadow rounded-lg overflow-hidden mb-4 border border-gray-200">
        <div className="px-4 py-4 border-b border-gray-200 bg-gradient-to-r from-purple-50 to-indigo-50">
          <div className="flex justify-between items-center">
            <div
              className="flex-grow cursor-pointer hover:bg-white/50 px-3 py-1.5 rounded-lg transition-colors"
              onClick={() => toggleGroupExpanded(group.groupId)}
            >
              <div className="flex items-center">
                <svg
                  className="h-5 w-5 text-purple-600 mr-2"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
                <h3 className="text-lg font-semibold text-gray-900">
                  {group.groupName}
                </h3>
                <span className="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                  {t('testAttemptsTab.studentsCount', '{{count}} students', {
                    count: group.students.length,
                  })}
                </span>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4 px-4">
              <div className="text-center">
                <span className="text-xs font-medium text-gray-500 block">
                  {t('testAttemptsTab.totalAttempts', 'Total Attempts')}
                </span>
                <span className="text-sm font-bold text-gray-900">
                  {groupStats.totalAttempts}
                </span>
              </div>
              <div className="text-center">
                <span className="text-xs font-medium text-gray-500 block">
                  {t('testAttemptsTab.avgScore', 'Avg Score')}
                </span>
                <span className="text-sm font-bold text-gray-900">
                  {averageScore}
                </span>
              </div>
              <div className="text-center">
                <span className="text-xs font-medium text-gray-500 block">
                  {t('testAttemptsTab.avgAiScore', 'Avg AI Score')}
                </span>
                <span className="text-sm font-bold text-gray-900">
                  {averageAiScore}
                </span>
              </div>
            </div>

            <div className="flex items-center ml-4">
              <button
                onClick={() => navigate(`/admin/groups/${group.groupId}`)}
                className="mr-4 px-3 py-1.5 text-sm font-medium rounded-md bg-purple-100 text-purple-700 hover:bg-purple-200 transition-colors"
              >
                {t('testAttemptsTab.groupDetails', 'Group Details')}
              </button>
              <button
                onClick={() => toggleGroupExpanded(group.groupId)}
                className="p-2 rounded-full hover:bg-white/80 transition-colors"
              >
                <svg
                  className={`h-5 w-5 text-purple-600 transform transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>

        {isExpanded && (
          <div className="p-4 space-y-3 max-h-[600px] overflow-y-auto">
            {group.students.length === 0 ? (
              <div className="text-center py-6 bg-gray-50 rounded-lg">
                <p className="text-sm text-gray-500">
                  {t(
                    'testAttemptsTab.noStudentsInGroup',
                    'No students in this group.'
                  )}
                </p>
              </div>
            ) : (
              group.students.map(student => (
                <StudentCard
                  key={student.username}
                  student={student}
                  groupId={group.groupId}
                />
              ))
            )}
          </div>
        )}
      </div>
    );
  };

  return (
    <div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
        <StatisticCard
          icon={
            <svg
              className="h-6 w-6 text-purple-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
              />
            </svg>
          }
          title={t('testAttemptsTab.students', 'Students')}
          value={stats.totalStudents}
          className="bg-gradient-to-br from-purple-50 to-indigo-50 border border-purple-100"
        />

        <StatisticCard
          icon={
            <svg
              className="h-6 w-6 text-blue-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
              />
            </svg>
          }
          title={t('testAttemptsTab.totalAttempts', 'Total Attempts')}
          value={stats.totalAttempts}
          className="bg-gradient-to-br from-blue-50 to-cyan-50 border border-blue-100"
        />

        <StatisticCard
          icon={
            <svg
              className="h-6 w-6 text-green-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
              />
            </svg>
          }
          title={t('testAttemptsTab.averageScore', 'Average Score')}
          value={t('testAttemptsTab.pointsValue', '{{score}} pts', {
            score: stats.averageScore,
          })}
          className="bg-gradient-to-br from-green-50 to-emerald-50 border border-green-100"
        />

        <StatisticCard
          icon={
            <svg
              className="h-6 w-6 text-blue-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M13 10V3L4 14h7v7l9-11h-7z"
              />
            </svg>
          }
          title={t('testAttemptsTab.averageAiScore', 'Average AI Score')}
          value={t('testAttemptsTab.pointsValue', '{{score}} pts', {
            score: stats.averageAiScore,
          })}
          className="bg-gradient-to-br from-indigo-50 to-sky-50 border border-indigo-100"
        />

        <StatisticCard
          icon={
            <svg
              className="h-6 w-6 text-amber-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5"
              />
            </svg>
          }
          title={t('testAttemptsTab.aiAccuracy', 'AI Accuracy')}
          value={t('testAttemptsTab.percentage', '{{percent}}%', {
            percent: stats.aiAccuracyPercent,
          })}
          subtitle={t(
            'testAttemptsTab.averageDeviation',
            '±{{score}} pts average deviation',
            { score: stats.aiAccuracy }
          )}
          className="bg-gradient-to-br from-amber-50 to-yellow-50 border border-amber-100"
        />
      </div>

      <div className="mb-6 relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <svg
            className="h-5 w-5 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </div>
        <input
          type="text"
          className="block w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 sm:text-sm transition-colors"
          placeholder={t(
            'testAttemptsTab.searchPlaceholder',
            'Search students by name or username...'
          )}
          value={searchQuery}
          onChange={handleSearchChange}
        />
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center py-12 bg-white rounded-lg shadow">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-purple-600 mb-4" />
          <p className="text-gray-600 text-lg">
            {t(
              'testAttemptsTab.loadingAttemptsData',
              'Loading attempts data...'
            )}
          </p>
        </div>
      ) : !attempts || attempts.length === 0 ? (
        <div className="text-center py-10 bg-white rounded-lg shadow border border-gray-200">
          <svg
            className="mx-auto h-16 w-16 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1}
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            />
          </svg>
          <h3 className="mt-4 text-lg font-medium text-gray-900">
            {t('testAttemptsTab.noAttemptsFound', 'No attempts found')}
          </h3>
          <p className="mt-2 text-sm text-gray-500 max-w-md mx-auto">
            {searchQuery
              ? t(
                  'testAttemptsTab.noMatchingAttempts',
                  'No attempts match your search criteria. Try a different search term.'
                )
              : t(
                  'testAttemptsTab.noAttemptsYet',
                  'There are no test attempts for this test yet.'
                )}
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {attempts.map(group => (
            <GroupCard key={`group-${group.groupId}`} group={group} />
          ))}
        </div>
      )}
    </div>
  );
};

export default TestAttemptsTab;
