import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { AttemptService } from '@/services/AttemptService';
import AdminLayout from '@/layouts/AdminLayout';
import { useTranslation } from 'react-i18next';
import AuthenticatedImage from '@/components/questions/AuthenticatedImage';

const AttemptReviewPage = () => {
  const { t } = useTranslation();
  const { attemptId } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [reviewData, setReviewData] = useState(null);
  const [questionFeedback, setQuestionFeedback] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [activeSubmissionId, setActiveSubmissionId] = useState(null);

  useEffect(() => {
    const fetchAttemptReview = async () => {
      try {
        setLoading(true);
        const response = await AttemptService.getAttemptReview(attemptId);
        setReviewData(response);

        const initialFeedback = {};
        response.questions.forEach(question => {
          initialFeedback[question.submissionId] = {
            score: question.score || 0,
            teacherFeedback: question.teacherFeedback || '',
          };
        });
        setQuestionFeedback(initialFeedback);

        if (response.questions && response.questions.length > 0) {
          setActiveSubmissionId(response.questions[0].submissionId);
        }
      } catch {
        toast.error(
          t(
            'adminAttemptReviewPage.loadError',
            'Failed to load attempt review data'
          )
        );
      } finally {
        setLoading(false);
      }
    };

    fetchAttemptReview();
  }, [attemptId, t]);

  const handleScoreChange = (submissionId, score) => {
    setQuestionFeedback(prev => ({
      ...prev,
      [submissionId]: {
        ...prev[submissionId],
        score: Number(score),
      },
    }));
  };

  const handleFeedbackChange = (submissionId, feedback) => {
    setQuestionFeedback(prev => ({
      ...prev,
      [submissionId]: {
        ...prev[submissionId],
        teacherFeedback: feedback,
      },
    }));
  };

  const handleSubmitReview = async () => {
    try {
      setSubmitting(true);

      const questionReviews = Object.keys(questionFeedback).map(
        submissionId => ({
          submissionId: Number(submissionId),
          score: questionFeedback[submissionId].score,
          teacherFeedback: questionFeedback[submissionId].teacherFeedback,
        })
      );

      await AttemptService.submitAttemptReview(attemptId, { questionReviews });

      toast.success(
        t(
          'adminAttemptReviewPage.submitSuccess',
          'Review submitted successfully'
        )
      );
      navigate(-1);
    } catch {
      toast.error(
        t('adminAttemptReviewPage.submitError', 'Failed to submit review')
      );
    } finally {
      setSubmitting(false);
    }
  };

  const navigateQuestion = direction => {
    if (!reviewData || !reviewData.questions) return;

    const currentIndex = reviewData.questions.findIndex(
      q => q.submissionId === activeSubmissionId
    );
    if (currentIndex === -1) return;

    let newIndex;
    if (direction === 'next') {
      newIndex = (currentIndex + 1) % reviewData.questions.length;
    } else {
      newIndex =
        (currentIndex - 1 + reviewData.questions.length) %
        reviewData.questions.length;
    }

    setActiveSubmissionId(reviewData.questions[newIndex].submissionId);
  };

  const getActiveQuestion = () => {
    if (!reviewData || !reviewData.questions) return null;
    return reviewData.questions.find(
      q => q.submissionId === activeSubmissionId
    );
  };

  const currentTotalScore = Object.values(questionFeedback).reduce(
    (sum, item) => sum + (item.score || 0),
    0
  );

  const getOptionStatusClasses = option => {
    if (option.selected && option.correct) {
      return 'bg-green-100 text-green-800 border-green-300';
    } else if (option.selected && !option.correct) {
      return 'bg-red-100 text-red-800 border-red-300';
    } else if (!option.selected && option.correct) {
      return 'bg-yellow-100 text-yellow-800 border-yellow-300';
    }
    return 'bg-gray-100 text-gray-800 border-gray-300';
  };

  const getOptionIconClasses = option => {
    if (option.selected && option.correct) {
      return 'text-green-500';
    } else if (option.selected && !option.correct) {
      return 'text-red-500';
    } else if (!option.selected && option.correct) {
      return 'text-yellow-500';
    }
    return 'text-gray-400';
  };

  const content = () => {
    if (loading) {
      return (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-purple-600" />
          <p className="ml-3 text-gray-600">
            {t('adminAttemptReviewPage.loading', 'Loading attempt review...')}
          </p>
        </div>
      );
    }

    if (!reviewData) {
      return (
        <div className="text-center py-8 bg-gray-50 rounded-lg border border-gray-200">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
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
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            {t('adminAttemptReviewPage.noReviewData', 'No review data found')}
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            {t(
              'adminAttemptReviewPage.attemptNoExist',
              "The attempt may not exist or you don't have permission to view it."
            )}
          </p>
          <button
            onClick={() => navigate(-1)}
            className="mt-4 px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700"
          >
            {t('adminAttemptReviewPage.goBack', 'Go Back')}
          </button>
        </div>
      );
    }

    const activeQuestion = getActiveQuestion();

    return (
      <div className="space-y-6">
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <div className="px-6 py-5 border-b border-gray-200 bg-purple-600">
            <div className="flex justify-between items-center">
              <div>
                <h1 className="text-xl font-bold text-white">
                  {reviewData.testTitle}
                </h1>
                <p className="mt-1 text-sm text-purple-100">
                  {reviewData.testDescription ||
                    t(
                      'adminAttemptReviewPage.noDescription',
                      'No description provided'
                    )}
                </p>
              </div>
              <button
                onClick={() => navigate(-1)}
                className="px-3 py-1 text-sm text-white hover:text-purple-200 flex items-center"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-4 w-4 mr-1"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M10 19l-7-7m0 0l7-7m-7 7h18"
                  />
                </svg>
                {t('adminAttemptReviewPage.backToAttempts', 'Back to Attempts')}
              </button>
            </div>
          </div>

          <div className="px-6 py-4 bg-white">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="bg-purple-50 rounded-lg p-4 border border-purple-100">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      className="h-6 w-6 text-purple-600"
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
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">
                      {t(
                        'adminAttemptReviewPage.teacherScore',
                        'Teacher Score'
                      )}
                    </p>
                    <div className="mt-1 flex items-baseline">
                      <p className="text-xl font-semibold text-purple-800">
                        {currentTotalScore}
                      </p>
                      <span className="ml-1 text-sm text-gray-500">
                        / {reviewData.totalScore}{' '}
                        {t('adminAttemptReviewPage.points', 'points')}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="bg-indigo-50 rounded-lg p-4 border border-indigo-100">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      className="h-6 w-6 text-indigo-600"
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
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">
                      {t('adminAttemptReviewPage.aiScore', 'AI Score')}
                    </p>
                    <div className="mt-1 flex items-baseline">
                      <p className="text-xl font-semibold text-indigo-800">
                        {reviewData.aiScore}
                      </p>
                      <span className="ml-1 text-sm text-gray-500">
                        / {reviewData.totalScore}{' '}
                        {t('adminAttemptReviewPage.points', 'points')}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="bg-blue-50 rounded-lg p-4 border border-blue-100">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      className="h-6 w-6 text-blue-600"
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
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">
                      {t('adminAttemptReviewPage.started', 'Started')}
                    </p>
                    <p className="mt-1 text-sm font-semibold text-gray-900">
                      {new Date(reviewData.startTime).toLocaleString()}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-green-50 rounded-lg p-4 border border-green-100">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      className="h-6 w-6 text-green-600"
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
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">
                      {t('adminAttemptReviewPage.completed', 'Completed')}
                    </p>
                    <p className="mt-1 text-sm font-semibold text-gray-900">
                      {new Date(reviewData.endTime).toLocaleString()}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          <div className="bg-white shadow rounded-lg overflow-hidden lg:col-span-1">
            <div className="px-4 py-3 bg-gray-50 border-b border-gray-200">
              <h3 className="text-sm font-medium text-gray-700">
                {t('adminAttemptReviewPage.questions', 'Questions')}
              </h3>
            </div>
            <div className="divide-y divide-gray-200 max-h-96 overflow-y-auto">
              {reviewData.questions.map((question, index) => {
                const isActive = question.submissionId === activeSubmissionId;
                let statusClass = 'bg-gray-100 text-gray-800';

                if (question.score === question.maxScore) {
                  statusClass = 'bg-green-100 text-green-800';
                } else if (question.score > 0) {
                  statusClass = 'bg-yellow-100 text-yellow-800';
                } else if (
                  question.score === 0 &&
                  (question.studentAnswer ||
                    question.selectedOptionIds?.length > 0)
                ) {
                  statusClass = 'bg-red-100 text-red-800';
                }

                const displayScore =
                  question.score > 0 ? question.score : question.aiScore;
                const isAiScore = question.score === 0 && question.aiScore > 0;

                return (
                  <button
                    key={question.submissionId}
                    onClick={() => setActiveSubmissionId(question.submissionId)}
                    className={`w-full px-4 py-3 flex items-center text-left hover:bg-gray-50 ${isActive ? 'bg-purple-50 border-l-4 border-purple-600' : ''}`}
                  >
                    <div
                      className={`flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center ${isActive ? 'bg-purple-600 text-white' : 'bg-gray-200 text-gray-700'}`}
                    >
                      {index + 1}
                    </div>
                    <div className="ml-3 flex-grow truncate">
                      <div className="text-sm font-medium text-gray-900 truncate">
                        {question.questionText
                          ? question.questionText.length > 30
                            ? `${question.questionText.substring(0, 30)}...`
                            : question.questionText
                          : t(
                              'adminAttemptReviewPage.questionNum',
                              'Question {{num}}',
                              { num: index + 1 }
                            )}
                      </div>
                      <div className="text-xs flex items-center">
                        {isAiScore ? (
                          <span className="text-indigo-600">
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              className="inline-block h-3 w-3 mr-1"
                              viewBox="0 0 20 20"
                              fill="currentColor"
                            >
                              <path
                                fillRule="evenodd"
                                d="M13 10V3L4 14h7v7l9-11h-7z"
                                clipRule="evenodd"
                              />
                            </svg>
                            {displayScore}
                          </span>
                        ) : (
                          <span className="text-gray-600">{displayScore}</span>
                        )}
                        <span className="text-gray-500">
                          {' '}
                          / {question.maxScore}{' '}
                          {t('adminAttemptReviewPage.points', 'points')}
                        </span>
                      </div>
                    </div>
                    <div className="ml-2 flex flex-col items-end">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${statusClass}`}
                      >
                        {question.score === question.maxScore
                          ? t('adminAttemptReviewPage.correct', 'Correct')
                          : question.score > 0
                            ? t('adminAttemptReviewPage.partial', 'Partial')
                            : question.studentAnswer ||
                                question.selectedOptionIds?.length > 0
                              ? t(
                                  'adminAttemptReviewPage.incorrect',
                                  'Incorrect'
                                )
                              : t(
                                  'adminAttemptReviewPage.notAnswered',
                                  'Not answered'
                                )}
                      </span>
                      {question.requested && (
                        <span className="mt-1 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                          {t(
                            'adminAttemptReviewPage.reviewRequested',
                            'Review Requested'
                          )}
                        </span>
                      )}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="bg-white shadow rounded-lg overflow-hidden lg:col-span-3">
            {activeQuestion && (
              <>
                <div className="px-6 py-4 bg-gray-50 border-b border-gray-200 flex justify-between items-center">
                  <div className="flex items-center">
                    <span className="bg-purple-600 text-white h-6 w-6 rounded-full flex items-center justify-center text-sm font-medium">
                      {reviewData.questions.findIndex(
                        q => q.submissionId === activeQuestion.submissionId
                      ) + 1}
                    </span>
                    <h2 className="ml-2 text-lg font-medium text-gray-900">
                      {t(
                        'adminAttemptReviewPage.questionReview',
                        'Question Review'
                      )}
                    </h2>
                    {activeQuestion.requested && (
                      <span className="ml-2 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                        {t(
                          'adminAttemptReviewPage.reviewRequested',
                          'Review Requested'
                        )}
                      </span>
                    )}
                  </div>
                  <div className="flex space-x-2">
                    <button
                      type="button"
                      onClick={() => navigateQuestion('prev')}
                      className="p-1 rounded-md hover:bg-gray-200"
                      title={t(
                        'adminAttemptReviewPage.previousQuestion',
                        'Previous Question'
                      )}
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-5 w-5 text-gray-600"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M15 19l-7-7 7-7"
                        />
                      </svg>
                    </button>
                    <button
                      type="button"
                      onClick={() => navigateQuestion('next')}
                      className="p-1 rounded-md hover:bg-gray-200"
                      title={t(
                        'adminAttemptReviewPage.nextQuestion',
                        'Next Question'
                      )}
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-5 w-5 text-gray-600"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M9 5l7 7-7 7"
                        />
                      </svg>
                    </button>
                  </div>
                </div>

                <div className="p-6">
                  <div className="mb-6">
                    {activeQuestion.questionText && (
                      <div className="text-gray-900 text-lg font-medium mb-4">
                        {activeQuestion.questionText}
                      </div>
                    )}

                    {activeQuestion.imagePath && (
                      <div className="mt-4 flex justify-center">
                        <div className="rounded-lg border border-gray-200 overflow-hidden max-w-lg">
                          <AuthenticatedImage
                            imagePath={activeQuestion.imagePath}
                            alt={t(
                              'adminAttemptReviewPage.questionImage',
                              'Question Image'
                            )}
                            className="max-w-full h-auto"
                          />
                        </div>
                      </div>
                    )}

                    <div className="mt-2 text-sm text-purple-600 font-medium">
                      {t('adminAttemptReviewPage.maxScore', 'Max score')}:{' '}
                      {activeQuestion.maxScore}{' '}
                      {t('adminAttemptReviewPage.points', 'points')}
                    </div>
                  </div>

                  {activeQuestion.options &&
                    activeQuestion.options.length > 0 && (
                      <div className="mb-6">
                        <h3 className="text-sm font-medium text-gray-700 mb-3">
                          {t(
                            'adminAttemptReviewPage.studentsAnswer',
                            "Student's Answer:"
                          )}
                        </h3>
                        <div className="space-y-2">
                          {activeQuestion.options.map(option => (
                            <div
                              key={option.optionId}
                              className={`p-3 rounded-md border flex items-start ${getOptionStatusClasses(option)}`}
                            >
                              <div className="flex-shrink-0 mr-3">
                                {option.selected && option.correct ? (
                                  <svg
                                    className={`h-5 w-5 ${getOptionIconClasses(option)}`}
                                    xmlns="http://www.w3.org/2000/svg"
                                    viewBox="0 0 20 20"
                                    fill="currentColor"
                                  >
                                    <path
                                      fillRule="evenodd"
                                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                                      clipRule="evenodd"
                                    />
                                  </svg>
                                ) : option.selected && !option.correct ? (
                                  <svg
                                    className={`h-5 w-5 ${getOptionIconClasses(option)}`}
                                    xmlns="http://www.w3.org/2000/svg"
                                    viewBox="0 0 20 20"
                                    fill="currentColor"
                                  >
                                    <path
                                      fillRule="evenodd"
                                      d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                                      clipRule="evenodd"
                                    />
                                  </svg>
                                ) : !option.selected && option.correct ? (
                                  <svg
                                    className={`h-5 w-5 ${getOptionIconClasses(option)}`}
                                    xmlns="http://www.w3.org/2000/svg"
                                    viewBox="0 0 20 20"
                                    fill="currentColor"
                                  >
                                    <path
                                      fillRule="evenodd"
                                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                                      clipRule="evenodd"
                                    />
                                  </svg>
                                ) : (
                                  <svg
                                    className={`h-5 w-5 ${getOptionIconClasses(option)}`}
                                    xmlns="http://www.w3.org/2000/svg"
                                    viewBox="0 0 20 20"
                                    fill="currentColor"
                                  >
                                    <path
                                      fillRule="evenodd"
                                      d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z"
                                      clipRule="evenodd"
                                    />
                                  </svg>
                                )}
                              </div>
                              <div>
                                <p className="text-sm font-medium">
                                  {option.text}
                                </p>
                                {option.description && (
                                  <p className="mt-1 text-xs text-gray-600">
                                    {option.description}
                                  </p>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                  {activeQuestion.studentAnswer && (
                    <div className="mb-6">
                      <h3 className="text-sm font-medium text-gray-700 mb-3">
                        {t(
                          'adminAttemptReviewPage.studentsWrittenAnswer',
                          "Student's Written Answer:"
                        )}
                      </h3>
                      <div className="p-4 bg-gray-50 rounded-md border border-gray-200">
                        <p className="text-sm text-gray-800 whitespace-pre-wrap">
                          {activeQuestion.studentAnswer}
                        </p>
                      </div>
                    </div>
                  )}

                  {activeQuestion.aiScore !== undefined && (
                    <div className="mb-6">
                      <div className="bg-indigo-50 rounded-lg p-4 border border-indigo-100">
                        <h3 className="text-sm font-medium text-indigo-800 mb-2">
                          {t(
                            'adminAttemptReviewPage.aiEvaluation',
                            'AI Evaluation'
                          )}
                        </h3>
                        <div className="flex items-center mb-3">
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5 text-indigo-600 mr-2"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M13 10V3L4 14h7v7l9-11h-7z"
                              clipRule="evenodd"
                            />
                          </svg>
                          <span className="text-sm font-medium text-gray-700">
                            {t('adminAttemptReviewPage.score', 'Score')}:{' '}
                            <span className="text-indigo-700 font-semibold">
                              {activeQuestion.aiScore}
                            </span>{' '}
                            / {activeQuestion.maxScore}{' '}
                            {t('adminAttemptReviewPage.points', 'points')}
                          </span>
                        </div>
                        {activeQuestion.aiFeedback && (
                          <div className="mt-2">
                            <h4 className="text-xs font-medium text-gray-600 mb-1">
                              {t(
                                'adminAttemptReviewPage.aiFeedback',
                                'AI Feedback:'
                              )}
                            </h4>
                            <div className="p-3 bg-white rounded border border-indigo-100">
                              <p className="text-sm text-gray-700 whitespace-pre-wrap">
                                {activeQuestion.aiFeedback}
                              </p>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  <div className="mt-8 space-y-4">
                    <h3 className="text-sm font-medium text-gray-700">
                      {t(
                        'adminAttemptReviewPage.gradeQuestion',
                        'Grade Question:'
                      )}
                    </h3>

                    <div className="flex flex-wrap gap-2 mb-4">
                      {[...Array(activeQuestion.maxScore + 1).keys()].map(
                        score => (
                          <button
                            key={score}
                            type="button"
                            onClick={() =>
                              handleScoreChange(
                                activeQuestion.submissionId,
                                score
                              )
                            }
                            className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                              questionFeedback[activeQuestion.submissionId]
                                ?.score === score
                                ? 'bg-purple-600 text-white'
                                : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
                            }`}
                          >
                            {score}
                          </button>
                        )
                      )}
                    </div>

                    <div>
                      <label
                        htmlFor={`feedback-${activeQuestion.submissionId}`}
                        className="block text-sm font-medium text-gray-700 mb-1"
                      >
                        {t(
                          'adminAttemptReviewPage.feedbackToStudent',
                          'Feedback to Student'
                        )}
                      </label>
                      <textarea
                        id={`feedback-${activeQuestion.submissionId}`}
                        rows="3"
                        value={
                          questionFeedback[activeQuestion.submissionId]
                            ?.teacherFeedback || ''
                        }
                        onChange={e =>
                          handleFeedbackChange(
                            activeQuestion.submissionId,
                            e.target.value
                          )
                        }
                        className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-purple-500 focus:border-purple-500 sm:text-sm"
                        placeholder={t(
                          'adminAttemptReviewPage.feedbackPlaceholder',
                          'Provide feedback to the student...'
                        )}
                      />
                    </div>
                  </div>
                </div>
              </>
            )}

            <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 flex justify-between items-center">
              <div>
                <span className="text-sm text-gray-600 font-medium">
                  {t('adminAttemptReviewPage.totalScore', 'Total Score')}:{' '}
                  {currentTotalScore} / {reviewData.totalScore}
                </span>
              </div>
              <div className="flex space-x-3">
                <button
                  type="button"
                  onClick={() => navigate(-1)}
                  className="py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
                >
                  {t('adminAttemptReviewPage.cancel', 'Cancel')}
                </button>
                {submitting ? (
                  <button
                    type="button"
                    disabled
                    className="py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span className="flex items-center">
                      <svg
                        className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
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
                      {t('adminAttemptReviewPage.submitting', 'Submitting...')}
                    </span>
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={handleSubmitReview}
                    className="py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
                  >
                    {t('adminAttemptReviewPage.submitReview', 'Submit Review')}
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return <AdminLayout>{content()}</AdminLayout>;
};

export default AttemptReviewPage;
